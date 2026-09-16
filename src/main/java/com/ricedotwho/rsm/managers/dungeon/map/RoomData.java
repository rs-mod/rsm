package com.ricedotwho.rsm.managers.dungeon.map;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record RoomData(String name, RoomType type, RoomShape shape, List<Integer> cores, int crypts, int secrets, int trappedChests) {

    @Override
    public @NotNull String toString() {
        return "RoomData{" +
                "name=" + name +
                ",type=" + type +
                ",shape=" + shape +
                ",cores=" + cores +
                ",crypts=" + crypts +
                ",secrets=" + secrets +
                ",trappedChests=" + trappedChests +
                "}";

    }
}
