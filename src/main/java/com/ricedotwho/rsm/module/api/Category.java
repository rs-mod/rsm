package com.ricedotwho.rsm.module.api;

import lombok.Getter;

@Getter
public enum Category {
    MOVEMENT("Movement"),
    DUNGEONS("Dungeons"),
    PLAYER("Player"),
    RENDER("Render"),
    OTHER("Other");

    private final String name;

    Category(String name) {
        this.name = name;
    }
}

