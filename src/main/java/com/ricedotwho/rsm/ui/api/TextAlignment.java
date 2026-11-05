package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.render.render2d.NVGUtils;

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
        NVGUtils.setTextAlignment(this);
    }
}
