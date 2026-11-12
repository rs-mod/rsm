package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.Corners;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


@SuppressWarnings("unused")
public class RectangleNode extends Node {

    public RectangleNode(long yogaNode, Color color, @NonNull Corners rounding, float thickness, Color outlineColor) {
        super(yogaNode, color);
        this.rounding = rounding;
        this.thickness = thickness;
        this.outlineColor = outlineColor;
    }


    private @NotNull Corners rounding;
    private final float thickness;
    @Getter
    private final Color outlineColor;

    public void setRounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.rounding = new Corners(topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        renderRectangleNode(gfx,
                parentX, parentY, mouseX, mouseY,
                color, outlineColor,
                layoutLeft(), layoutTop(), layoutWidth(), layoutHeight(),
                rounding, thickness
        );
    }

    public static void renderRectangleNode(GuiGraphicsExtractor gfx,
            float parentX, float parentY, float mouseX, float mouseY,
            Color color, Color outlineColor,
            float layoutLeft, float layoutTop, float layoutWidth, float layoutHeight,
            Corners rounding, float thickness
    ) {
        float x = parentX + layoutLeft;
        float y = parentY + layoutTop;


        if (color == null) {
            if (outlineColor != null) {
                Render2D.outline(gfx, x, y, layoutWidth, layoutHeight, outlineColor, thickness, rounding);
            }
            return;
        }

        Render2D.rectOutline(gfx, x, y, layoutWidth, layoutHeight, outlineColor, color, thickness, rounding);
    }

    public final static class Builder extends Node.Builder<Builder> {
        private Corners rounding = Corners.NONE;
        private float thickness = -1f;
        private Color outlineColor = null;

        public Builder() {}

        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new Corners(topLeft, topRight, bottomLeft, bottomRight);
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new Corners(radius);
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
