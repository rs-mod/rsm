package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.render.render2d.NVGUtils;
import org.lwjgl.nanovg.NanoVG;

public enum TextAlignment {
    TopLeft,
    TopMiddle,
    TopRight,
    CenterLeft,
    CenterMiddle,
    CenterRight,
    BottomLeft,
    BottomMiddle,
    BottomRight;

    public float calculateX(float width) {
        return switch (this) {
            case TopLeft, CenterLeft, BottomLeft -> 0f;
            case TopMiddle, CenterMiddle, BottomMiddle -> 0.5f * width;
            case TopRight, CenterRight, BottomRight -> width;
        };
    }

    public float calculateY(float height) {
        return switch (this) {
            case TopLeft, TopMiddle, TopRight -> 0f;
            case CenterLeft, CenterMiddle, CenterRight -> height * 0.5f;
            case BottomLeft, BottomMiddle, BottomRight -> height;
        };
    }

    public void setTextAlign() {
        int horizontal = switch (this) {
            case TopLeft, CenterLeft, BottomLeft -> NanoVG.NVG_ALIGN_LEFT;
            case TopMiddle, CenterMiddle, BottomMiddle -> NanoVG.NVG_ALIGN_CENTER;
            case TopRight, CenterRight, BottomRight -> NanoVG.NVG_ALIGN_RIGHT;
        };

        int vertical = switch (this) {
            case TopLeft, TopMiddle, TopRight -> NanoVG.NVG_ALIGN_TOP;
            case CenterLeft, CenterMiddle, CenterRight -> NanoVG.NVG_ALIGN_MIDDLE;
            case BottomLeft, BottomMiddle, BottomRight -> NanoVG.NVG_ALIGN_BOTTOM;
        };

        NanoVG.nvgTextAlign(NVGUtils.getVg(), horizontal | vertical);
    }
}

