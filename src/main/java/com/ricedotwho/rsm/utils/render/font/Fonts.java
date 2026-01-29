package com.ricedotwho.rsm.utils.render.font;

import com.ricedotwho.rsm.utils.Accessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Fonts implements Accessor {
    private static final HashMap<Integer, TTFFontRenderer> ROBOTO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> NUNITO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> SFPRO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> PRODUCTSANS = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> JOSEFINBOLD = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> JOSEFIN = new HashMap<>();

    private static final HashMap<Integer, TTFFontRenderer> CUSTOM = new HashMap<>();

    public static TTFFontRenderer getRobotoMedium(final int size) {
        return get(ROBOTO, size, "roboto-medium", true, true);
    }

    public static TTFFontRenderer getSFProRounded(final int size) {
        return get(SFPRO, size, "sf-pro-rounded", true, true);
    }

    public static TTFFontRenderer getNunito(final int size) {
        return get(NUNITO, size, "Nunito", true, true);
    }

    public static TTFFontRenderer getProductSans(final int size) {
        return get(PRODUCTSANS, size, "product-sans", true, true);
    }

    public static TTFFontRenderer getJoseFinBold(final int size) {
        return get(JOSEFINBOLD, size, "josefin-bold", true, true);
    }
    public static TTFFontRenderer getJoseFin(final int size) {
        return get(JOSEFIN, size, "josefin", true, true);
    }
    public static TTFFontRenderer getCustom(final String name, final int size) {
        return get(CUSTOM, size, name, true, true);
    }

    private static TTFFontRenderer get(HashMap<Integer, TTFFontRenderer> map, int size, String name,
                                       boolean antialiasing, boolean fractionalMetrics) {
        if (map.containsKey(size)) {
            TTFFontRenderer existingRenderer = map.get(size);
            if (map == CUSTOM && existingRenderer.getFont().getName().equalsIgnoreCase(name)) {
                return existingRenderer;
            } else if (map != CUSTOM) {
                return existingRenderer;
            }
        }

        Font font = null;
        try {
            if (map != CUSTOM) {
                font = Font.createFont(Font.TRUETYPE_FONT,
                                Objects.requireNonNull(Fonts.class.getResourceAsStream("/assets/rsm/font/" + name + ".ttf")))
                        .deriveFont((float) size);
            } else {
                File fontFile = new File("C:\\Windows\\Fonts\\" + (name.toLowerCase().endsWith(".ttf") ? name : name + ".ttf"));



                font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont((float) size);
            }
        } catch (FontFormatException | IOException e) {
            return null;
        }

        TTFFontRenderer renderer = new TTFFontRenderer(font, antialiasing, fractionalMetrics);
        map.put(size, renderer);
        return renderer;
    }

}
