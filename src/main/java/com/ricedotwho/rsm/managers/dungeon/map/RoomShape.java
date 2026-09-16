package com.ricedotwho.rsm.managers.dungeon.map;

import com.google.gson.annotations.SerializedName;

public enum RoomShape {
    @SerializedName("Unknown")
    UNKNOWN,
    @SerializedName("L")
    L,
    @SerializedName("1x1")
    S1x1,
    @SerializedName("1x2")
    S2x1,
    @SerializedName("1x3")
    S3x1,
    @SerializedName("1x4")
    S4x1,
    @SerializedName("2x2")
    S2x2
}
