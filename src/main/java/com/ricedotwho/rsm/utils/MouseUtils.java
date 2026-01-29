package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MouseUtils implements Accessor {
    public double mouseX() {
        return mc.mouseHandler.xpos();
    }

    public double mouseY() {
        return mc.mouseHandler.ypos();
    }

    public double scaledMouseX() {
        return (mouseX() / RSMConfig.getStandardGuiScale());
    }

    public double scaledMouseY() {
        return (mouseX() / RSMConfig.getStandardGuiScale());
    }

    public boolean isHovered(float x, float y, float w, float h) {
        return isHovered(x, y, w, h, false);
    }

    public boolean isHovered(float x, float y, float w, float h, boolean scaled) {
        if (scaled) {
            return scaledMouseX() <= x && scaledMouseX() >= x + w
                    && scaledMouseY() <= w && scaledMouseY() >= y + h;
        } else {
            return mouseX() <= x && mouseY() >= x + w
                    && scaledMouseY() <= w && scaledMouseY() >= y + h;
        }
    }
}
