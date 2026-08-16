package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import lombok.Getter;
import lombok.val;
import org.lwjgl.nanovg.NanoVG;


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

    private static void renderOutline(float x, float y, float width, float height, float thickness, Color outlineColor, float[] rounding) {
        if (outlineColor == null || thickness == -1) return;

        val halfThickness = thickness / 2f;
        NanoVG.nvgBeginPath(vg);
        if (rounding == null) {
            NanoVG.nvgRect(
                    vg,
                    x + halfThickness,
                    y + halfThickness,
                    width - thickness,
                    height - thickness
            );
        } else {
            NanoVG.nvgRoundedRectVarying(
                    vg,
                    x + halfThickness,
                    y + halfThickness,
                    width - thickness,
                    height - thickness,
                    rounding[0],
                    rounding[1],
                    rounding[2],
                    rounding[3]
            );
        }

        NanoVG.nvgStrokeWidth(vg, thickness);
        NanoVG.nvgPathWinding(vg, NanoVG.NVG_HOLE);
        NVGUtils.color(outlineColor);
        NanoVG.nvgStrokeColor(vg, NVGUtils.getNvgColor());
        NanoVG.nvgStroke(vg);
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
                renderOutline(x, y, layoutWidth, layoutHeight, thickness, outlineColor, rounding);
            }
            return;
        }


        NanoVG.nvgBeginPath(vg);
        if (rounding == null) {
            NanoVG.nvgRect(vg, x, y, layoutWidth, layoutHeight);
        } else {
            NanoVG.nvgRoundedRectVarying(vg, x, y, layoutWidth, layoutHeight, rounding[0], rounding[1], rounding[2], rounding[3]);
        }

        NVGUtils.color(color);
        NanoVG.nvgFillColor(vg, NVGUtils.getNvgColor());
        NanoVG.nvgFill(vg);

        renderOutline(x, y, layoutWidth, layoutHeight, thickness, outlineColor, rounding);
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
