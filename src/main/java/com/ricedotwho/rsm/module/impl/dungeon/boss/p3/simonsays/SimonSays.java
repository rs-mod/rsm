package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.simonsays;

import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.regex.Matcher;

@Getter
@ModuleInfo(aliases = "Simon Says", id = "SimonSays", category = Category.DUNGEONS)
public class SimonSays extends Module {
    public final GroupSetting<Solver> solver = new GroupSetting<>("Solver", new Solver(this));

    public SimonSays() {
        this.registerProperty(solver);
    }

    public static void chat(String message) {
        ChatUtils.chat(
                Component.empty()
                        .append(Component.literal("s").withColor(0x4585ed))
                        .append(Component.literal("s").withColor(0x45aaed))
                        .append(Component.literal(" » ").withStyle(ChatFormatting.BLUE))
                        .append(message)
        );
    }

    public boolean ssDone = false;
    public boolean s1Done = false;
    public int expectedTerms = 0;

    @Override
    public void reset() {
        ssDone = false;
        s1Done = false;
        expectedTerms = 0;
    }

    @SubscribeEvent
    public void onChat(ChatEvent event) {
        String message = event.getMessage().getString();

        if (message.equals("[DS] Starting S1 on next tick")) {
            reset();
        }

        Matcher matcher = Dungeon.TERM.matcher(message);
        if (matcher.find()) {
            String type = matcher.group(2);
            String count = matcher.group(3);
            int countInt = 0;

            try {
                countInt = Integer.parseInt(count);
            } catch (NumberFormatException ignored) {
            }

            if (count.equals("7")) s1Done = true;

            if (countInt != (expectedTerms + 1) || expectedTerms == 7) return;

            expectedTerms = countInt;

            if (type.equals("device")) {
                ssDone = true;
                if (solver.getValue().isEnabled()) solver.getValue().onSSDone();
            }

        }
    }

    public boolean isAtSS() {
        if (mc.player == null) return false;
        return mc.player.position().x > 104 && mc.player.position().x < 112 &&
                mc.player.position().y > 116 && mc.player.position().y < 123 &&
                mc.player.position().z > 86 && mc.player.position().z < 96;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }
}
