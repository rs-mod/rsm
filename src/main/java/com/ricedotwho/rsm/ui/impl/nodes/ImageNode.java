package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.Corners;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.ui.api.Node;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;


@SuppressWarnings("unused")
public class ImageNode extends Node {

    public ImageNode(long yogaNode, float alpha, @NotNull Corners rounding, @Nullable Image image) {
        super(yogaNode, null);
        this.rounding = rounding;
        this.image = image;
        this.alpha = alpha;
    }


    private Corners rounding;
    @Setter
    @Nullable
    private Image image;

    @Setter
    private float alpha;

    public void setRounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.rounding = new Corners(topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (image == null) return;
        float x = originX(parentX);
        float y = originY(parentY);
        float w = layoutWidth();
        float h = layoutHeight();

        Render2D.image(gfx, x, y, w, h, image, corners:rounding);
    }

    public final static class Builder extends Node.Builder<Builder> {
        private Corners rounding = Corners.NONE;
        private float alpha = 1f;
        private Image image = null;

        public Builder() {}

        public Builder alpha(float alpha) { this.alpha = alpha; return this; }
        public Builder image(Image image) { this.image = image; return this; }
        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new Corners(topLeft, topRight, bottomLeft, bottomRight);
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new Corners(radius);
            return this;
        }



        @Override
        public ImageNode build() {
            return new ImageNode(buildYogaNode(), alpha, rounding, image);
        }
    }
}
