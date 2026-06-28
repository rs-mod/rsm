package com.ricedotwho.rsm.utils.api;

import com.ricedotwho.rsm.component.impl.location.Floor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record DungeonData(Map<Floor, FloorData> pbs, int secrets, float secretsPerRun, int mp) {
    public record FloorData(long s, long sPlus) {
        @Override
        public @NotNull String toString() {
            return "FloorData{s=" + s + ",sPlus=" + sPlus + "}";
        }
    }
}
