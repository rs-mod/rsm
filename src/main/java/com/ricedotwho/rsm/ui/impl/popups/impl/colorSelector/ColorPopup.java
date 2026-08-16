package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.elements.NumberBox;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.Popup;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorPopup extends Popup {
    public static final float colorElementHeight = 180f;
    public static final float precomputedWidth = 272f;
    @Getter
    private static ColorPopup instance = new ColorPopup();

    final ColorDifference colorDifference;
    final RectangleNode favoriteColorsContainer;
    @Nullable
    private Runnable onClose = null;

    @Getter @NotNull
    private Color targetColor = Palette.createColorContainer();

    private ColorPopup() {
        val base = new RectangleNode.Builder()
                .flexDirection(FlexDirection.COLUMN)
                .positionType(PositionType.ABSOLUTE)
                .gap(8)
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .padding(12)
                .alignItems(Align.STRETCH)
                .build();
        super(base);

        val colorSelectorContainer = new RectangleNode.Builder()
                .flexDirection(FlexDirection.ROW)
                .gap(8)
                .build();

        this.addChild(colorSelectorContainer);
        val colorGradientBoxWrapper = new RectangleNode.Builder()
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .justifyContent(Node.JustifyContent.FLEX_START)
                .padding(Palette.elementInteriorPadding)
                .alignItems(Align.CENTER)
                .build();
        colorGradientBoxWrapper.addChild(new ColorGradientBox(this));

        colorSelectorContainer.addChild(colorGradientBoxWrapper);
        colorSelectorContainer.addChild(new HueSlider(this));
        colorSelectorContainer.addChild(new AlphaSlider(this));
        this.colorDifference = new ColorDifference(this);

        val hexRow = new RectangleNode.Builder()
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.STRETCH)
                .justifyContent(JustifyContent.FLEX_START)
                .gap(8)
                .build();

        hexRow.addChild(new RevertColorButton(this));
        hexRow.addChild(colorDifference);
        hexRow.addChild(new HexCodeBox(this, true));
        this.addChild(hexRow);

        val rgbRow = new RectangleNode.Builder()
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.STRETCH)
                .justifyContent(JustifyContent.FLEX_START)
                .gap(8)
                .build();

        val yogaNodeBuilder = new YogaNodeBuilder()
                .height(Palette.largeElementHeight)
                .padding(Palette.elementInteriorPadding)
                .flexGrow(1f);

        rgbRow.addChild(
                new NumberBox(
                        yogaNodeBuilder.build(),
                        0,
                        255,
                        0,
                        () -> (double) (targetColor.getRedByte() & 0xFF),
                        (value) -> targetColor.setToColor(
                                (byte) Math.clamp(value, 0, 255),
                                targetColor.getGreenByte(),
                                targetColor.getBlueByte(),
                                targetColor.getAlphaByte()
                        )
                )
        );

        rgbRow.addChild(
                new NumberBox(
                        yogaNodeBuilder.build(),
                        0,
                        255,
                        0,
                        () -> (double) (targetColor.getGreenByte() & 0xFF),
                        (value) -> targetColor.setToColor(
                                targetColor.getRedByte(),
                                (byte) Math.clamp(value, 0, 255),
                                targetColor.getBlueByte(),
                                targetColor.getAlphaByte()
                        )
                )
        );

        rgbRow.addChild(
                new NumberBox(
                        yogaNodeBuilder.build(),
                        0,
                        255,
                        0,
                        () -> (double) (targetColor.getBlueByte() & 0xFF),
                        (value) -> targetColor.setToColor(
                                targetColor.getRedByte(),
                                targetColor.getGreenByte(),
                                (byte) Math.clamp(value, 0, 255),
                                targetColor.getAlphaByte()
                        )
                )
        );

        rgbRow.addChild(
                new NumberBox(
                        yogaNodeBuilder.build(),
                        0,
                        255,
                        0,
                        () -> (double) (targetColor.getAlphaByte() & 0xFF),
                        (value) -> targetColor.setToColor(
                                targetColor.getRedByte(),
                                targetColor.getGreenByte(),
                                targetColor.getBlueByte(),
                                (byte) Math.clamp(value, 0, 255)
                        )
                )
        );

        this.addChild(rgbRow);

        favoriteColorsContainer = new RectangleNode.Builder()
                .height(Palette.elementHeight * 2f + 8f)
                .width(Palette.elementInteriorPadding * 2f + 8f * 2f + colorElementHeight + Palette.elementHeight * 2f)
                .gap(8.5f) //this is to fix a 4px gap. it is painful ik, but not noticeable.
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .flexWrap(FlexWrap.WRAP)
                .justifyContent(JustifyContent.FLEX_START)
                .alignItems(Align.FLEX_START)
                .flexShrink(1f)
                .flexBasis(1f)
                .build();

        this.addChild(favoriteColorsContainer);
        favoriteColorsContainer.addChild(new AddFavoriteColorButton(this));
        setVisible(false);
        loadFavoriteColors();

        Gui.registerPopup(this);
    }

    public void loadFavoriteColors() {
        val favoriteColors = UniversalSettings.getFavoriteColors();
        while (favoriteColors.size() > 17) {
            favoriteColors.removeLast();
        }

        for (Color favoriteColor : favoriteColors) {
            favoriteColorsContainer.addChildAt(1, new FavoriteColor(favoriteColor, this));
        }
    }

    public void addFavoriteColor(Color color) {
        UniversalSettings.getFavoriteColors().addFirst(color);
        favoriteColorsContainer.addChildAt(1, new FavoriteColor(color, this));

        if (favoriteColorsContainer.getChildrenSize() <= 18) return; //18 because that is how many fit

        val child = favoriteColorsContainer.removeLastChild();
        child.close();
        UniversalSettings.getFavoriteColors().removeLast();
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
        MouseUtils.lockCursorRequest();
    }

    @Override
    protected boolean mouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        return true;
    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return true;
        setVisible(false);
        if (onClose != null) onClose.run();
        return true;
    }

    @Override
    public void onGuiClose() {
        setVisible(false);
        if (onClose != null) onClose.run();
    }

    public static void openColorPopup(Color color, float x, float y, float scrollY, @Nullable Runnable onClose) {
        instance.targetColor = color;
        instance.setLeft(x);
        instance.setTop(y + scrollY);
        instance.colorDifference.capture();
        instance.setVisible(true);
        instance.onClose = onClose;
    }
}
