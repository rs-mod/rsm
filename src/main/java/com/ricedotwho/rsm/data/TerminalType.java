package com.ricedotwho.rsm.data;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TerminalType {
    PANES("Correct all the panes!"),
    RUBIX("Change all to same color!"),
    ORDER("Click in order!"),
    STARTS_WITH("What starts with:"),
    SELECT("Select all the"),
    MELODY("Click the button on time!"),
    NONE("None");

    private final String guiName;

    TerminalType(String guiName) {
        this.guiName = guiName;
    }

    public static TerminalType findByStartsWithGuiName(String name) {
        return Arrays.stream(TerminalType.values())
                .filter(type -> name.startsWith(type.getGuiName()))
                .findFirst()
                .orElse(TerminalType.NONE);
    }
}