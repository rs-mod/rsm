package com.ricedotwho.rsm.module.api;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

@Getter
public enum Category {
    MOVEMENT("Movement", ResourceLocation.fromNamespaceAndPath("rsm", "clickgui/movenew.png"), new Color(85, 170, 255)),
    DUNGEONS("Dungeons", ResourceLocation.fromNamespaceAndPath("rsm", "clickgui/exploit.png"), new Color(255, 85, 85)),
    PLAYER("Player", ResourceLocation.fromNamespaceAndPath("rsm", "clickgui/player.png"), new Color(170, 255, 85)),
    RENDER("Render", ResourceLocation.fromNamespaceAndPath("rsm", "clickgui/visuals.png"), new Color(255, 255, 0)),
    OTHER("Other", ResourceLocation.fromNamespaceAndPath("rsm", "clickgui/script.png"), new Color(221, 66, 245));

    private final String name;
    private final ResourceLocation icon;
    private final Color color;

    Category(String name, ResourceLocation icon, Color color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }
}

