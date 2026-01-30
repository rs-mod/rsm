package com.ricedotwho.rsm.component.impl.map.map;

import com.nimbusds.oauth2.sdk.auth.Secret;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class RoomData {
    private final String name;
    private final RoomType type;
    private final String shape;
    private final List<Integer> cores;
    private final int crypts;
    private final int secrets;
    @Setter
    private int secretsFound = 0;
    private final int trappedChests;

    public RoomData(String name, RoomType type, String shape, List<Integer> cores, int crypts, int secrets, int trappedChests) {
        this.name = name;
        this.type = type;
        this.shape = shape;
        this.cores = cores;
        this.crypts = crypts;
        this.secrets = secrets;
        this.trappedChests = trappedChests;
    }

    @Override
    public String toString() {
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

    public static RoomData createUnknown(RoomType type) {
        return new RoomData("Unknown", type, "?", Collections.emptyList(), 0, 0, 0);
    }
}
