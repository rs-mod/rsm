package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.Image;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;

@Getter
public enum Category {
    MOVEMENT("Movement", "/assets/rsm/clickgui/movenew.png", new Colour(85, 170, 255)),
    DUNGEONS("Dungeons", "/assets/rsm/clickgui/exploit.png", new Colour(255, 85, 85)),
    PLAYER("Player", "/assets/rsm/clickgui/player.png", new Colour(170, 255, 85)),
    RENDER("Render", "/assets/rsm/clickgui/visuals.png", new Colour(255, 255, 0)),
    OTHER("Other", "/assets/rsm/clickgui/script.png", new Colour(221, 66, 245));

    private final String name;
    private final String path;
    private final Colour color;
    private Image image = null;

    Category(String name, String path, Colour color) {
        this.name = name;
        this.path = path;
        this.color = color;
    }

    public Image getImage() {
        if (image == null) {
            this.image = NVGUtils.createImage(path);
        }
        return image;
    }
}

