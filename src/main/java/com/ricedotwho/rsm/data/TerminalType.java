package com.ricedotwho.rsm.data;

import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;

@Getter
public enum TerminalType {
    PANES("Correct all the panes!", Panes::new),
    RUBIX("Change all to same color!", Rubix::new),
    ORDER("Click in order!", Order::new),
    STARTS_WITH("What starts with:", StartsWith::new),
    SELECT("Select all the", Select::new),
    MELODY("Click the button on time!", Melody::new),
    NONE("None", null);

    private final String guiName;
    private final Function<String, Term> factory;

    TerminalType(String guiName, Function<String, Term> factory) {
        this.guiName = guiName;
        this.factory = factory;
    }

    public Term create(String title) {
        if (this.factory == null) return null;
        return this.factory.apply(title);
    }

    public static TerminalType findByStartsWithGuiName(String name) {
        return Arrays.stream(TerminalType.values())
                .filter(type -> name.startsWith(type.getGuiName()))
                .findFirst()
                .orElse(TerminalType.NONE);
    }
}