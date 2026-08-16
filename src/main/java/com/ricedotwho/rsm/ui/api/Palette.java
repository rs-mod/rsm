package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.impl.elements.SettingElementContainer;
import lombok.experimental.UtilityClass;


@UtilityClass
public final class Palette {
    public final Color backdrop = Color.fromHex(0x131313);
    public final Color foreground = Color.fromHex(0x1A1A1A);
    public final Color stroke = Color.fromHex(0x303030);

    public final Color text = Color.fromHex(0xFFFFFF);
    public final Color descriptions = Color.fromHex(0x919191);
    public final Color textHighlighted = createColorContainer();

    public final Color elementHighlight = Color.fromHex(0xFF5263);

    public final Color elementBackgroundLight = Color.fromHex(0x282828);
    public final Color elementBackgroundDark = backdrop;

    public final FontSupplier font = new FontSupplier(NVGUtils.JOSEFIN);
    public final FontSupplier fontBold = new FontSupplier(NVGUtils.JOSEFIN_BOLD);

    public final FontSizeSupplier fontSize = new FontSizeSupplier(14);
    public final FontSizeSupplier fontSizeLarge = new FontSizeSupplier(16);
    public final FontSizeSupplier titleFontSize = new FontSizeSupplier(24);

    public static final float smallElementWidth = 68f;
    public static final float mediumElementWidth = 220f;
    public static final float largeElementWidth = smallElementWidth + 8f + mediumElementWidth;
    public void updateColors() {
        SettingElementContainer.elementStrokeColor.mutateLerpNoAlpha(Palette.stroke, Color.BLACK, 0.2f);
        textHighlighted.setToColor(elementHighlight);
        textHighlighted.setAlpha(0.4f);
    }


    public final float strokeThickness = 2f;
    public final float elementHeight = 20f;
    public final float largeElementHeight = 28f;
    public static float elementInteriorPadding = Palette.strokeThickness * 3f;


    /**
     * This is for cases where the color needs to be dynamically changed for a specific element,
     * where it will get its color based on a color in the Palette
     */
    public Color createColorContainer() {
        return Color.WHITE.clone();
    }
}

