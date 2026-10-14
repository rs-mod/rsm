package com.ricedotwho.rsm.utils.mouse;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;

@UtilityClass
@Register
public class MouseUtils implements Accessor {
    public double mouseX() {
        return mc.mouseHandler.xpos();
    }

    public double mouseY() {
        return mc.mouseHandler.ypos();
    }

    // Minecraft 26.3 reports mouse buttons with SDL codes (1=left, 2=middle, 3=right),
    // but the RSM ui (and saved keybinds) use the legacy GLFW codes (0=left, 1=right, 2=middle).
    public static int legacyButton(int sdlButton) {
        return switch (sdlButton) {
            case 1 -> 0;
            case 2 -> 2;
            case 3 -> 1;
            default -> sdlButton > 0 ? sdlButton - 1 : sdlButton;
        };
    }

    private CursorType currentCursor = CursorType.DEFAULT;
    private CursorType pendingCursor = CursorType.DEFAULT;
    private boolean allowCursorChanges = true;

    public void lockCursorRequest() {
        allowCursorChanges = false;
    }

    public void requestCursor(CursorType cursor) {
        pendingCursor = cursor;
    }

    @ApiStatus.Internal
    public void onPostRender() {
        CursorType effectiveCursor = allowCursorChanges ? pendingCursor : CursorType.DEFAULT;
        if (currentCursor != effectiveCursor) {
            currentCursor = effectiveCursor;
            effectiveCursor.select(mc.getWindow());
        }

        resetFrame();
    }

    private void resetFrame() {
        allowCursorChanges = true;
        pendingCursor = mc.mouseHandler.isMouseGrabbed() ? CursorType.DEFAULT_DISABLED : CursorType.DEFAULT;
    }
}
