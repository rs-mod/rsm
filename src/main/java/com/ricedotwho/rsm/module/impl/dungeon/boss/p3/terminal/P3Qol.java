package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Floor;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.Renderer3D;
import com.ricedotwho.rsm.managers.dungeon.Phase7;
import com.ricedotwho.rsm.managers.dungeon.map.handler.Dungeon;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import com.ricedotwho.rsm.module.api.settings.impl.StringSetting;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

@Getter
@ModuleInfo(aliases = "P3 Qol", id = "P3Qol", category = Category.DUNGEONS)
public class P3Qol extends Module {
    @SuppressWarnings("unused")
    private static final P3Qol instance = new P3Qol();

    private final BooleanSetting deviceDone = new BooleanSetting("Device Title", false);
    private final ColorSetting deviceDoneColor = new ColorSetting("Device Color", Color.GREEN);
    private final StringSetting deviceContent = new StringSetting("Device", "Device Done!");
    private final BooleanSetting sectionDone = new BooleanSetting("Section Title", false);
    private final ColorSetting sectionDoneColor = new ColorSetting("Section Color", Color.BLUE);
    private final StringSetting sectionContent = new StringSetting("Section", "Section Complete");
    private final NumberSetting<Float> volume = new NumberSetting<>("Volume", 0f, 20f, 5f, 0.1f);
    private final BooleanSetting termHitboxes = new BooleanSetting("Terminals Hitboxes", false);
    private final ColorSetting termLine = new ColorSetting("Terminal Line", Color.GREEN);
    private final ColorSetting termFill = new ColorSetting("Terminal Fill", Color.fromRGB(0, 255, 0, 0.5f));
    private final BooleanSetting termDepth = new BooleanSetting("Terminals Depth", false);
    private final BooleanSetting noTerminalPling = new BooleanSetting("No Terminal Pling", false);

    private final Set<AABB> stands = new HashSet<>();
    private int pendingPlings = 0;

    @Override
    public void reset() {
        pendingPlings = 0;
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon) || !DungeonUtils.isPhase(Phase7.P3) || mc.player == null) return;
        String text = ChatFormatting.stripFormatting(event.getMessage().getString());
        Matcher matcher = Dungeon.TERM.matcher(text);
        if (!matcher.find()) return;
        String name = matcher.group(1);
        String type = matcher.group(2);
        int start = Integer.parseInt(matcher.group(3));
        int end = Integer.parseInt(matcher.group(4));
        if (name.contains(">") || name.contains("]")) return;

        if (deviceDone.getValue() && name.contains(mc.player.getName().getString()) && type.contains("device")) {
            Hud.showTitle(deviceContent.getValue(), deviceDoneColor.getValue(), 1500);
            PlayerUtils.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), volume.getValue(), 5f);
        }

        if (sectionDone.getValue() && start == end) {
            Hud.showTitle(sectionContent.getValue(), sectionDoneColor.getValue(), 1500);
            PlayerUtils.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), volume.getValue(), 1f);
        }
    }

    @SubscribeEvent
    private void onTick(ClientTickEvent.Start event) {
        if (!termHitboxes.getValue() || mc.level == null || !Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || !Utils.equalsOneOf(Location.getFloor(), Floor.M7, Floor.F7)) return;
        stands.clear();
        for (Entity entity : mc.level.entitiesForRendering()) {
            // cba making it better
            if (entity instanceof ArmorStand stand && stand.getDisplayName().getString().contains("Inactive Terminal")) {
                stands.add(stand.getBoundingBox());
            }
        }
    }

    @SubscribeEvent
    private void onExtract(Render3DEvent.Extract event) {
        if (stands.isEmpty() || !termHitboxes.getValue() || !Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || !Utils.equalsOneOf(Location.getFloor(), Floor.M7, Floor.F7)) return;
        stands.forEach(aabb -> Renderer3D.addTask(new FilledOutlineBox(aabb, termFill.getValue(), termLine.getValue(), termDepth.getValue())));
    }

    @SubscribeEvent
    public void onTerminal(PacketEvent.MainReceivePre event) {
        if (!Location.getArea().is(Island.Dungeon) || !Dungeon.isInBoss() || !DungeonUtils.isPhase(Phase7.P3)) return;
        Matcher matcher = Dungeon.TERM.matcher(event.toString());
        if (matcher.find()) {
            pendingPlings++;
        }
    }

    @SubscribeEvent
    public void onSound(PacketEvent.MainReceivePre event) {
        if (!(event.getPacket() instanceof ClientboundSoundPacket packet)
                || packet.getSound().value() != SoundEvents.NOTE_BLOCK_PLING.value()
                || packet.getVolume() != 8F || packet.getPitch() != 4.04761) return; // probably correct pitch i forgot
        if (pendingPlings > 0) {
            pendingPlings--;
            if (noTerminalPling.getValue()) {
                event.setCancelled(true);
            }
        }
    }
}
