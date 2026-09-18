package com.ricedotwho.rsm.utils.mouse;

import com.mojang.blaze3d.platform.Window;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFW;

/// Mod implementation of {@link com.mojang.blaze3d.platform.cursor.CursorType} allowing for input mode, and not restricted by the minecraft option
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CursorType {
    public static final CursorType DEFAULT = new CursorType("default", 0L, GLFW.GLFW_CURSOR_NORMAL);
    public static final CursorType DEFAULT_DISABLED = new CursorType("default", 0L, GLFW.GLFW_CURSOR_DISABLED);

    private final String name;
    public final long handle;
    public final int inputMode;

    @Override
    public String toString() {
        return this.name;
    }

    public void select(Window window) {
        // hopefully this won't override minecraft when it should not
        if (GLFW.glfwGetInputMode(window.handle(), GLFW.GLFW_CURSOR) != inputMode) {
            GLFW.glfwSetInputMode(
                    window.handle(),
                    GLFW.GLFW_CURSOR,
                    inputMode
            );
        }
        GLFW.glfwSetCursor(window.handle(), this.handle);
    }

    static CursorType createStandardCursor(final int inputMode, final int shape, final String name, final CursorType fallback) {
        long handle = GLFW.glfwCreateStandardCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, inputMode);
    }

    static CursorType createStandardCursor(final int shape, final String name, final CursorType fallback) {
        long handle = GLFW.glfwCreateStandardCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, GLFW.GLFW_CURSOR_NORMAL);
    }
}
