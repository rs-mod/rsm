package com.ricedotwho.rsm.component.impl.map.map;

import com.ricedotwho.rsm.utils.Utils;

public enum Puzzle {
    BOMB_DEFUSE("Bomb Defuse"),
    BOULDER("Boulder"),
    CREEPER_BEAMS("Creeper Beams"),
    HIGHER_BLAZE("Higher Blaze", "Higher Or Lower"),
    ICE_FILL("Ice Fill"),
    ICE_PATH("Ice Path"),
    LOWER_BLAZE("Lower Blaze", "Higher Or Lower"),
    QUIZ("Quiz"),
    TELEPORT_MAZE("Teleport Maze"),
    THREE_WEIRDOS("Three Weirdos"),
    TIC_TAC_TOE("Tic Tac Toe"),
    WATER_BOARD("Water Board");

    private final String roomDataName;
    private final String tabName;

    Puzzle(String roomDataName, String tabName) {
        this.roomDataName = roomDataName;
        this.tabName = tabName;
    }

    Puzzle(String roomDataName) {
        this(roomDataName, roomDataName);
    }

    public static Puzzle fromName(String name) {
        for (Puzzle puzzle : Puzzle.values()) {
            if (Utils.equalsOneOf(name, puzzle.roomDataName, puzzle.tabName)) {
                return puzzle;
            }
        }
        return null;
    }
}

