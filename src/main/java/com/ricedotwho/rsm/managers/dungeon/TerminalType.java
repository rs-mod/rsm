package com.ricedotwho.rsm.managers.dungeon;

import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Getter
public enum TerminalType {
    PANES("Correct all the panes!", Panes::new, 9 * 5),
    RUBIX("Change all to same color!", Rubix::new, 9 * 5),
    ORDER("Click in order!", Order::new, 9 * 4),
    STARTS_WITH("What starts with:", StartsWith::new, 9 * 5),
    SELECT("Select all the", Select::new, 9 * 6),
    MELODY("Click the button on time!", Melody::new, 9 * 6),
    NONE("None", null, -1);

    private final String guiName;
    private final Function<String, Term> factory;
    private final int size;

    TerminalType(String guiName, Function<String, Term> factory, int size) {
        this.guiName = guiName;
        this.factory = factory;
        this.size = size;
    }

    public Term create(String title) {
        if (this.factory == null) return null;
        return this.factory.apply(title);
    }

    public static List<TerminalType> getValues() {
        return List.of(PANES, RUBIX, ORDER, STARTS_WITH, SELECT, MELODY);
    }

    public static TerminalType findByStartsWithGuiName(String name) {
        return Arrays.stream(TerminalType.values())
                .filter(type -> name.startsWith(type.getGuiName()))
                .findFirst()
                .orElse(TerminalType.NONE);
    }
}