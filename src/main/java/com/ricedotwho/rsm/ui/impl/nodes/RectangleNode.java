package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.GuiRenderUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import lombok.Getter;
import lombok.val;


@SuppressWarnings("unused")
public class RectangleNode extends Node {

    public RectangleNode(long yogaNode, Color color, float[] rounding, float thickness, Color outlineColor) {
        super(yogaNode, color);
        this.rounding = rounding;
        this.thickness = thickness;
        this.outlineColor = outlineColor;
    }


    private float[] rounding; // null, or {topLeft, topRight, bottomRight, bottomLeft}
    private final float thickness;
    @Getter
    private final Color outlineColor;

    public void setRounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.rounding = new float[] { topLeft, topRight, bottomRight, bottomLeft };
    }

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        renderRectangleNode(
                parentX, parentY, mouseX, mouseY,
                color, outlineColor,
                layoutLeft(), layoutTop(), layoutWidth(), layoutHeight(),
                rounding, thickness
        );
    }

    public static void renderRectangleNode(
            float parentX, float parentY, float mouseX, float mouseY,
            Color color, Color outlineColor,
            float layoutLeft, float layoutTop, float layoutWidth, float layoutHeight,
            float[] rounding, float thickness
    ) {
        float x = parentX + layoutLeft;
        float y = parentY + layoutTop;

        if (color == null) {
            if (outlineColor != null) {
                GuiRenderUtils.drawRoundedOutlineRect(x, y, layoutWidth, layoutHeight, rounding, thickness, outlineColor);
            }
            return;
        }

        GuiRenderUtils.drawRoundedRect(x, y, layoutWidth, layoutHeight, rounding, color);

        if (outlineColor != null) {
            GuiRenderUtils.drawRoundedOutlineRect(x, y, layoutWidth, layoutHeight, rounding, thickness, outlineColor);
        }
    }

    public final static class Builder extends Node.Builder<Builder> {
        private float[] rounding;
        private float thickness = -1f;
        private Color outlineColor = null;

        public Builder() {}

        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new float[] { topLeft, topRight, bottomRight, bottomLeft };
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new float[] { radius, radius, radius, radius };
            return this;
        }

        public Builder outline(float thickness, Color outlineColor) {
            this.thickness = thickness;
            this.outlineColor = outlineColor;
            return this;
        }

        @Override
        public RectangleNode build() {
            return new RectangleNode(buildYogaNode(), color, rounding, thickness, outlineColor);
        }
    }
}
