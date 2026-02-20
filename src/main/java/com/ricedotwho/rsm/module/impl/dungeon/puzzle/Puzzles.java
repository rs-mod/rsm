package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Puzzles", id = "Puzzles", category = Category.DUNGEONS)
public class Puzzles extends Module {

    private final GroupSetting<TicTacToe> ticTacToe = new GroupSetting<>("TTT", new TicTacToe(this));
    private final GroupSetting<ThreeWeirdos> threeWeirdos = new GroupSetting<>("Three Weirdos", new ThreeWeirdos(this));
    private final GroupSetting<IceFill> iceFill = new GroupSetting<>("Ice Fill", new IceFill(this));

    public Puzzles() {
        this.registerProperty(
                ticTacToe,
                threeWeirdos,
                iceFill
        );
    }
}
