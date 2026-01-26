package com.ricedotwho.rsm.utils.font;

import com.ricedotwho.rsm.utils.Accessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Fonts implements Accessor {
    private static final HashMap<Integer, TTFFontRenderer> ROBOTO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> NUNITO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> SF_PRO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> PRODUCT_SANS = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> JOSEFIN_BOLD = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> JOSEFIN = new HashMap<>();

    private static final HashMap<Integer, TTFFontRenderer> CUSTOM = new HashMap<>();

    public static TTFFontRenderer getRobotoMedium(final int size) {
        return get(ROBOTO, size, "Roboto Medium", true, true);
    }

    public static TTFFontRenderer getSFProRounded(final int size) {
        return get(SF_PRO, size, "SF Pro Rounded", true, true);
    }

    public static TTFFontRenderer getNunito(final int size) {
        return get(NUNITO, size, "Nunito", true, true);
    }

    public static TTFFontRenderer getProductSans(final int size) {
        return get(PRODUCT_SANS, size, "Product Sans", true, true);
    }

    public static TTFFontRenderer getJoseFinBold(final int size) {
        return get(JOSEFIN_BOLD, size, "JoseFin Bold", true, true);
    }
    public static TTFFontRenderer getJoseFin(final int size) {
        return get(JOSEFIN, size, "JoseFin", true, true);
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

        Font font;
        try {
            if (map != CUSTOM) {
                font = Font.createFont(Font.TRUETYPE_FONT,
                                Objects.requireNonNull(Fonts.class.getResourceAsStream("/assets/font/" + name + ".ttf")))
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
