package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.GuiRender;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

@UtilityClass
@Register
public class MouseUtils implements Accessor {
    public double mouseX() {
        return mc.mouseHandler.xpos();
    }

    public double mouseY() {
        return mc.mouseHandler.ypos();
    }

    private long handCursor;
    private long iBeamCursor;

    private long currentCursor = 0L;
    private boolean currentHidden = false;

    private long requestedCursor = 0L;
    private boolean requestedHidden = false;
    private boolean preventRequest = false;

    public void init() {
        handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        iBeamCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    }

    public void lockCursorRequest() { preventRequest = true; }
    public void hideCursor() { if (!preventRequest) requestedHidden = true; }
    public void setIBeamCursor() {
        if (!preventRequest) requestedCursor = iBeamCursor;
    }
    public void setHandCursor() { if (!preventRequest) requestedCursor = handCursor; }

    @SubscribeEvent
    private void onPreRender(GuiRender.Start event) {
        requestedCursor = 0L;
        requestedHidden = false;
        preventRequest = false;
    }

    @SubscribeEvent
    private void onPostRender(GuiRender.End event) {
        long handle = mc.getWindow().handle();

        if (requestedHidden != currentHidden) {
            GLFW.glfwSetInputMode(
                    handle,
                    GLFW.GLFW_CURSOR,
                    requestedHidden ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL
            );
            currentHidden = requestedHidden;
        }

        if (!requestedHidden && requestedCursor != currentCursor) {
            GLFW.glfwSetCursor(handle, requestedCursor);
            currentCursor = requestedCursor;
        }
    }
}
