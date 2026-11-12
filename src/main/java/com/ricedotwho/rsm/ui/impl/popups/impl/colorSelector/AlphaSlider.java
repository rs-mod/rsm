package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.render.render2d.Gradient;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;
import net.minecraft.client.gui.GuiGraphicsExtractor;

class AlphaSlider extends Widget {
    private static final Image alphaBackground = new Image("/assets/rsm/clickgui/AlphaBackground.png");
    public final ColorPopup colorPopup;
    public final Node filling;

    public AlphaSlider(ColorPopup colorPopup) {
        val base = new RectangleNode.Builder()
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.FLEX_START)
                .padding(Palette.elementInteriorPadding)
                .alignItems(Align.CENTER)
                .build();
        super(base);
        filling = new ImageNode.Builder()
                .height(ColorPopup.colorElementHeight)
                .width(Palette.elementHeight - Palette.elementInteriorPadding * 2f)
                .image(alphaBackground)
                .build();
        base.addChild(filling);
        this.colorPopup = colorPopup;
    }

    private boolean dragging = false;

    @Override
    public void dispatchFrame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        if (isHovered(parentX, parentY, mouseX, mouseY, scrollY)) hover();

        node.dispatchFrame(gfx, parentX, parentY, mouseX, mouseY, scrollY);

        val targetColor = colorPopup.getTargetColor();
        float percentage;
        if (dragging) {
            val localMouseY = mouseY - Palette.elementInteriorPadding - parentY - layoutTop();
            percentage = 1f - Math.clamp(localMouseY / ColorPopup.colorElementHeight, 0f, 1f);

            targetColor.setAlpha(percentage);
        } else {
            percentage = targetColor.getAlpha();
        }

        Render2D.gradient(gfx,
                filling.layoutLeft() + originX(parentX),
                filling.layoutTop() + originY(parentY),
                filling.layoutWidth(),
                filling.layoutHeight(),
                targetColor.withAlpha(1F),
                targetColor.withAlpha(0F),
                Gradient.TopToBottom
        );

        float y = originY(parentY) + Palette.elementInteriorPadding + ColorPopup.colorElementHeight - ColorPopup.colorElementHeight * percentage;
        float x = originX(parentX);

        Render2D.line(gfx,
                x,
                y,
                x + layoutWidth(),
                y,
                Color.WHITE,
                2F
        );
    }


    @Override
    protected void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        dragging = false;
    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;
        dragging = true;
        return false;
    }
}
