package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import org.joml.Vector2d;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Mining Event", id = "mining-event", category = Category.OTHER)
public class MiningEvent extends Module {
    @SuppressWarnings("unused")
    private static final MiningEvent instance = new MiningEvent();

    private final MultiBoolSetting events = new MultiBoolSetting("Events", List.of("Mithril Gourmand", "Raffle", "2x Powder", "Goblin Raid"), List.of());
    private final StringSetting sound = new StringSetting("Sound", "block.note_block.pling");
    private final NumberSetting<Float> volume = new NumberSetting<>("Volume", 0f, 1f, 1f, 0.01f);
    private final NumberSetting<Float> pitch = new NumberSetting<>("Pitch", 0f, 1f, 1f, 0.01f);
    private final NumberSetting<Integer> titleDuration = new NumberSetting<>("Title Duration", 0, 5000, 2500, 100);

    private final NumberSetting<Integer> amount = new NumberSetting<>("Amount", 1, 50, 20, 1);
    private final NumberSetting<Integer> increment = new NumberSetting<>("Increment", 1, 20, 2, 1);

    private final BooleanSetting gourmandTimer = new BooleanSetting("Gourmand Timer", false);
    private final BooleanSetting endingSoon = new BooleanSetting("Ending Soon", false);

    private String content;
    private Color colour;
    private boolean warned = false;

    public final HudSetting hud = new HudSetting("Gourmand Timer Hud", new Vector2d(50, 50), new Vector2d(50, 10)) {
        @Override
        protected void draw(GuiGraphicsExtractor gfx) {
            hud.renderScaledGFX(gfx, () -> hud.text(gfx, content, Align.CENTER, 0, 0, colour, false));
        }
    }.shouldRender(() -> Location.getArea().is(Island.DwarvenMines) && gourmandTimer.getValue() && content != null);

    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "⚑ The (.*) event starts in 20 seconds!\n" +
                    " (Click here to teleport to Garry and prepare!|This is a passive event! It's happening everywhere in the Dwarven Mines!)"),
            GOURMAND = Pattern.compile("^EVENT MITHRIL GOURMAND ACTIVE IN .+ for (\\d{2}:\\d{2})$");

    @Override
    protected void reset() {
        content = null;
        colour = null;
        warned = false;
    }

    @SubscribeEvent
    public void onBossEvent(PacketEvent.MainReceivePost event, ClientboundBossEventPacket packet) {
        if (!Location.getArea().is(Island.DwarvenMines) || !gourmandTimer.getValue()) return;
        var bossEvent = mc.gui.getBossOverlay().events.get(packet.id);
        if (bossEvent == null) return;

        Matcher matcher = GOURMAND.matcher(bossEvent.name.getString().stripFormatting());
        if (!matcher.find()) return;

        var time = matcher.group(1);
        var split = time.split(":");
        long left = (Integer.parseInt(split[0]) * 60L + Integer.parseInt(split[1])) * 1000L;

        content = NumberUtils.millisToOptMSS(left);
        colour = getColour(left);

        if (left <= 0) {
            reset();
            return;
        }

        if (left <= 20_000 && !warned && endingSoon.getValue()) {
            warned = true;
            playSounds();
            ChatUtils.chat(Component.literal("Gourmand ends soon! ")
                    .append(Component.literal("CLICK")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent.RunCommand("tptodonexpresso"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to teleport")
                                    .withStyle(ChatFormatting.YELLOW))))
            );
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.DwarvenMines) && !Location.getArea().is(Island.CrystalHollows) || mc.player == null) return;
        Matcher matcher = EVENT_PATTERN.matcher(event.getString());
        if (matcher.find()) {
            String theEvent = matcher.group(1);

            if (this.getEvents().get(theEvent)) {
                if (this.getTitleDuration().getValue().longValue() != 0) Hud.showTitle(theEvent + "!", Color.MINECRAFT_AQUA, this.getTitleDuration().getValue());
                playSounds();
            }
        }
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        this.hud.render(event.getGfx());
    }

    private Color getColour(long millis) {
        if (millis > 60_000) return Color.MINECRAFT_GREEN;
        if (millis > 30_000) return Color.MINECRAFT_YELLOW;
        return Color.MINECRAFT_RED;
    }

    public void playSounds() {
        Identifier sound = Identifier.tryParse(this.sound.getValue());
        if (sound == null) return;
        SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
        for (int i = 0; i < this.getAmount().getValue(); i += this.getIncrement().getValue()) {
            Scheduler.tick(i, () -> PlayerUtils.playSound(event, this.pitch.getValue(), this.volume.getValue()));
        }
    }
}
