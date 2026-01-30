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
        return (mouseY() / RSMConfig.getStandardGuiScale());
    }
}
