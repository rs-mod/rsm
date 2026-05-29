package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.HudSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Mask Status", id = "MaskStatus", category = Category.DUNGEONS)
public class MaskStatus extends Module {

    private final BooleanSetting dungeonOnly = new BooleanSetting("Dungeon Only", true);
    private final BooleanSetting p3Only = new BooleanSetting("P3 Only", true);
    private final BooleanSetting shadow = new BooleanSetting("Shadow", false);
    private final HudSetting hud = new HudSetting("Masks", new Vector2d(50, 50), new Vector2d(100, 25), () -> mc.player != null && (!dungeonOnly.getValue() || Location.getArea().is(Island.Dungeon) && (!p3Only.getValue() || DungeonUtils.isPhase(Phase7.P3)))) {
        @Override
        protected void draw(GuiGraphicsExtractor gfx) {
            this.renderScaledGFX(gfx, () -> {
                this.text(gfx, "Bonzo > " + getRemaining(Mask.BONZO, trackedHelmet == Mask.BONZO, false), Align.LEFT, 0, 0, Colour.WHITE, shadow.getValue());
                this.text(gfx, "Spirit > " + getRemaining(Mask.SPIRIT, trackedHelmet == Mask.SPIRIT, false), Align.LEFT, 0, 9, Colour.WHITE, shadow.getValue());
                this.text(gfx, "Phoenix > " + getRemaining(Mask.PHOENIX, trackedPet.equals("Phoenix"), trackedHelmet != null && isOffCooldown(trackedHelmet)), Align.LEFT, 0, 18, Colour.WHITE, shadow.getValue());
            });
        }
    };

    private String trackedPet = "";
    private Mask trackedHelmet = null;

    private static final Map<Mask, Long> COOLDOWNS = new HashMap<>(Map.of(
            Mask.BONZO, 0L,
            Mask.SPIRIT, 0L,
            Mask.PHOENIX, 0L
    ));

    private static final Map<Mask, Long> DURATIONS = Map.of(
            Mask.BONZO, 180_000L,
            Mask.SPIRIT, 30_000L,
            Mask.PHOENIX, 60_000L
    );

    private static final Map<String, Mask> MESSAGES = Map.of(
            "Your Bonzo's Mask saved your life!", Mask.BONZO,
            "Your ⚚ Bonzo's Mask saved your life!", Mask.BONZO,
            "Second Wind Activated! Your Spirit Mask saved your life!", Mask.SPIRIT,
            "Your Phoenix Pet saved you from certain death!", Mask.PHOENIX
    );

    private static final Pattern petSummonedPattern = Pattern.compile("^You summoned your ([a-zA-Z ]{1,32})(?: ✦)?!$");
    private static final Pattern autoPetPattern = Pattern.compile("^Autopet equipped your \\[Lvl \\d{1,3}] ([a-zA-Z ]{1,32})(?: ✦)?! VIEW RULE$");

    public MaskStatus() {
        this.registerProperty(
                dungeonOnly,
                p3Only,
                shadow,
                hud
        );
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        Mask mask = MESSAGES.get(event.getString());
        if (mask != null) {
            COOLDOWNS.put(mask, System.currentTimeMillis());
        }

        Matcher matcher;
        if ((matcher = petSummonedPattern.matcher(event.getString())).find()) {
            trackedPet = matcher.group(1).trim();
        } else if ((matcher = autoPetPattern.matcher(event.getString())).find()) {
            trackedPet = matcher.group(1).trim();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (mc.player == null) return;
        this.trackedHelmet = switch (ItemUtils.getID(mc.player.getItemBySlot(EquipmentSlot.HEAD))) {
            case "BONZO_MASK", "STARRED_BONZO_MASK" -> Mask.BONZO;
            case "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> Mask.SPIRIT;
            case null, default -> null;
        };
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        this.hud.render(event.getGfx());
    }

    private String getRemaining(Mask mask, boolean worn, boolean warn) {
        long diff = System.currentTimeMillis() - COOLDOWNS.get(mask);
        long remaining = DURATIONS.get(mask) - diff;

        if (remaining > 0) {
            return ChatFormatting.RED + NumberUtils.millisToTwoPoint(remaining);
        }
        return (worn ? warn ? ChatFormatting.DARK_RED : ChatFormatting.GOLD : ChatFormatting.GREEN) + "Ready";
    }

    private boolean isOffCooldown(Mask mask) {
        return System.currentTimeMillis() - COOLDOWNS.get(mask) > DURATIONS.get(mask);
    }

    private enum Mask {
        BONZO,
        SPIRIT,
        PHOENIX;
    }
}
