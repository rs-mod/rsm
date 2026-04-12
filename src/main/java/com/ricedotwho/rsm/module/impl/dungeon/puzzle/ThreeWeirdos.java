package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import lombok.Getter;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@SubModuleInfo(name = "ThreeWeirdos", alwaysDisabled = false)
public class ThreeWeirdos extends SubModule<Puzzles> {
    private final BooleanSetting enable = new BooleanSetting("Solver Enable", false);

    private static final Set<String> correctAnswers = Set.of(
            "The reward is not in my chest!",
            "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward we are all telling the truth.",
            "The reward isn't in any of our chests.",
            "Both of them are telling the truth. Also,"
    );

    private BlockPos correct = null;
    private Set<Integer> wrong = new HashSet<>();

    public ThreeWeirdos(Puzzles puzzles) {
        super(puzzles);
        this.registerProperty(
                enable
        );
    }

    private boolean check() {
        return Location.getArea().is(Island.Dungeon) && !Dungeon.isInBoss() && Map.getCurrentRoom() != null && Map.getCurrentRoom().getData().name().equals("Three Weirdos");
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!check()) return;
    }
}
