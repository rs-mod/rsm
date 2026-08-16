package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.api.Node;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;


@SuppressWarnings("unused")
public class ImageNode extends Node {

    public ImageNode(long yogaNode, float alpha, float[] rounding, @Nullable Image image) {
        super(yogaNode, null);
        this.rounding = rounding;
        this.image = image;
        this.alpha = alpha;
    }


    private float[] rounding; // null, or {topLeft, topRight, bottomRight, bottomLeft}
    @Setter
    @Nullable
    private Image image;

    @Setter
    private float alpha;

    public void setRounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.rounding = new float[] { topLeft, topRight, bottomRight, bottomLeft };
    }

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (image == null) return;
        float x = originX(parentX);
        float y = originY(parentY);
        float w = layoutWidth();
        float h = layoutHeight();


        nvgImagePattern(vg, x, y, w, h, 0f, NVGUtils.getImage(image), alpha, NVGUtils.getNvgPaint());
        nvgBeginPath(vg);
        if (rounding == null) {
            NanoVG.nvgRect(vg, x, y, w, h);
        } else {
            NanoVG.nvgRoundedRectVarying(vg, x, y, w, h, rounding[0], rounding[1], rounding[2], rounding[3]);
        }
        nvgFillPaint(vg, NVGUtils.getNvgPaint());
        nvgFill(vg);
    }

    public final static class Builder extends Node.Builder<Builder> {
        private float[] rounding;
        private float alpha = 1f;
        private Image image = null;

        public Builder() {}

        public Builder alpha(float alpha) { this.alpha = alpha; return this; }
        public Builder image(Image image) { this.image = image; return this; }
        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new float[] { topLeft, topRight, bottomRight, bottomLeft };
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new float[] { radius, radius, radius, radius };
            return this;
        }



        @Override
        public ImageNode build() {
            return new ImageNode(buildYogaNode(), alpha, rounding, image);
        }
    }
}
