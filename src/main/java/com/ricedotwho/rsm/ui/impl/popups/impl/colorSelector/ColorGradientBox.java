package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.render.render2d.Gradient;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.Setter;
import lombok.val;

class ColorGradientBox extends Widget {

    @Setter
    ColorPopup popupElement;
    Color colorContainer = Palette.createColorContainer();

    public ColorGradientBox(ColorPopup targetColorSupplier) {
        val base = new RectangleNode.Builder()
                .flexGrow(1f)
                .height(ColorPopup.colorElementHeight)
                .width(ColorPopup.colorElementHeight)
                .build();
        super(base);

        this.popupElement = targetColorSupplier;
    }

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        float x = originX(parentX);
        float y = originY(parentY);
        float size = layoutWidth();
        val targetColor = popupElement.getTargetColor();

        if (dragging) {
            float localMouseX = mouseX - x;
            float localMouseY = mouseY - y;
            float saturationPercent = Math.clamp(localMouseX / size, 0f, 1f);
            float valuePercent = Math.clamp(localMouseY / size, 0f, 1f);
            targetColor.setHSV(targetColor.getHueFloat(), saturationPercent, 1f - valuePercent, targetColor.getAlpha());
        }

        val hsbMax = targetColor.hsbMax();
        colorContainer.setToColor(Color.setArgbAlpha(hsbMax, 1f));

        NVGUtils.drawGradientRect(x, y, size, size, 0f, Color.WHITE, colorContainer, Gradient.LeftToRight);
        NVGUtils.drawGradientRect(x, y, size, size, 0f, Color.TRANSPARENT, Color.BLACK, Gradient.TopToBottom);

        float radius = 5f;
        float dotX = x + targetColor.getSaturationFloat() * size;
        float dotY = y + size - targetColor.getValueFloat() * size;
        NVGUtils.drawCircleOutline(dotX, dotY, radius, 2f, Color.WHITE);
    }

    boolean dragging = false;

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != 0 || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;
        dragging = true;
        return true;
    }

    @Override
    protected void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        dragging = false;
    }
}
