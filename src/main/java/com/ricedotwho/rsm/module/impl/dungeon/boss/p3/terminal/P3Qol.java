package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

@Getter
@ModuleInfo(id = "P3Qol", category = Category.DUNGEONS)
public class P3Qol extends Module {
    private final BooleanSetting deviceDone = new BooleanSetting("device_done", false);
    private final ColourSetting deviceDoneColour = new ColourSetting("device_done_colour", Colour.GREEN);
    private final StringSetting deviceContent = new StringSetting("device_content", "Device Done!");
    private final BooleanSetting sectionDone = new BooleanSetting("section_done", false);
    private final ColourSetting sectionDoneColour = new ColourSetting("section_colour", Colour.BLUE);
    private final StringSetting sectionContent = new StringSetting("section_content", "Section Complete");
    private final NumberSetting volume = new NumberSetting("volume", 0f, 20f, 5f, 0.1);
    private final BooleanSetting termHitboxes = new BooleanSetting("terminal_hitboxes", false);
    private final ColourSetting termLine = new ColourSetting("terminal_hitboxes_line", Colour.GREEN.copy());
    private final ColourSetting termFill = new ColourSetting("terminal_hitboxes_fill", new Colour(0, 255, 0, 127));
    private final BooleanSetting termDepth = new BooleanSetting("terminal_hitboxes_depth", false);

    private final Set<AABB> stands = new HashSet<>();

    public P3Qol() {
        this.registerProperty(
                deviceDone,
                deviceDoneColour,
                deviceContent,
                sectionDone,
                sectionDoneColour,
                volume,
                termHitboxes,
                termLine,
                termFill,
                termDepth
        );
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
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
            Hud.showTitle(deviceContent.getValue(), deviceDoneColour.getValue(), 1500);
            mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), volume.getValue().floatValue(), 5f);
        }

        if (sectionDone.getValue() && start == end) {
            Hud.showTitle(sectionContent.getValue(), sectionDoneColour.getValue(), 1500);
            mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), volume.getValue().floatValue(), 1f);
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
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
    public void onExtract(Render3DEvent.Extract event) {
        if (stands.isEmpty() || !termHitboxes.getValue() || !Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || !Utils.equalsOneOf(Location.getFloor(), Floor.M7, Floor.F7)) return;
        stands.forEach(aabb -> Renderer3D.addTask(new FilledOutlineBox(aabb, termFill.getValue(), termLine.getValue(), termDepth.getValue())));
    }
}
