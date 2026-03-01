package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.CustomSounds;
import com.ricedotwho.rsm.utils.DungeonUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import org.joml.Vector2d;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "P3 Qol", id = "P3Qol", category = Category.DUNGEONS)
public class P3Qol extends Module {
    private static final Pattern term = Pattern.compile("^(.*?) (?:activated|completed) a (terminal|device|lever)! \\((\\d+)/(\\d+)\\)");

    private final BooleanSetting deviceDone = new BooleanSetting("Device Title", false);
    private final ColourSetting deviceDoneColour = new ColourSetting("Device Colour", Colour.GREEN);
    private final StringSetting deviceContent = new StringSetting("Device", "Device Done!");
    private final BooleanSetting sectionDone = new BooleanSetting("Section Title", false);
    private final ColourSetting sectionDoneColour = new ColourSetting("Section Colour", Colour.BLUE);
    private final StringSetting sectionContent = new StringSetting("Secion", "Section Complete");
    private final NumberSetting volume = new NumberSetting("Volume", 0f, 20f, 5f, 0.1);

    private final StringSetting test = new StringSetting("asd", "", null, true, false, 512);

    public P3Qol() {
        this.registerProperty(
                deviceDone,
                deviceDoneColour,
                deviceContent,
                sectionDone,
                sectionDoneColour,
                volume,
                test
        );
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon) || !DungeonUtils.isPhase(Phase7.P3) || mc.player == null) return;
        String text = ChatFormatting.stripFormatting(event.getMessage().getString());
        Matcher matcher = term.matcher(text);
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
}
