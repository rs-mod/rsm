package com.ricedotwho.rsm.module.api;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

@Getter
public enum Category {
    MOVEMENT("Movement", new ResourceLocation("clickgui/movenew.png"), new Color(85, 170, 255)),
    DUNGEONS("Dungeons", new ResourceLocation("clickgui/exploit.png"), new Color(255, 85, 85)),
    PLAYER("Player", new ResourceLocation("clickgui/player.png"), new Color(170, 255, 85)),
    RENDER("Render", new ResourceLocation("clickgui/visuals.png"), new Color(255, 255, 0)),
    OTHER("Other", new ResourceLocation("clickgui/script.png"), new Color(221, 66, 245));

    private final String name;
    private final ResourceLocation icon;
    private final Color color;

    Category(String name, ResourceLocation icon, Color color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }
}

