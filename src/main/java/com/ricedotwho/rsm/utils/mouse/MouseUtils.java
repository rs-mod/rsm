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
            effectiveCursor.select(mc.window);
        }

        resetFrame();
    }

    private void resetFrame() {
        allowCursorChanges = true;
        pendingCursor = mc.mouseHandler.isMouseGrabbed ? CursorType.DEFAULT_DISABLED : CursorType.DEFAULT;
    }
}
