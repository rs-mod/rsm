package com.ricedotwho.rsm.utils.mouse;

import com.mojang.blaze3d.platform.Window;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.sdl.SDLMouse;

/// Mod implementation of {@link com.mojang.blaze3d.platform.cursor.CursorType} allowing for input mode, and not restricted by the minecraft option
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CursorType {
    public static final int CURSOR_NORMAL = 0;
    public static final int CURSOR_DISABLED = 1;
    public static final int CURSOR_HIDDEN = 2;

    public static final CursorType DEFAULT = new CursorType("default", 0L, CURSOR_NORMAL);
    public static final CursorType DEFAULT_DISABLED = new CursorType("default", 0L, CURSOR_DISABLED);

    private final String name;
    public final long handle;
    public final int inputMode;

    @Override
    public String toString() {
        return this.name;
    }

    public void select(Window window) {
        long windowHandle = window.handle();

        switch (inputMode) {
            case CURSOR_DISABLED -> {
                if (!SDLMouse.SDL_GetWindowRelativeMouseMode(windowHandle)) {
                    SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, true);
                }
            }
            case CURSOR_HIDDEN -> {
                if (SDLMouse.SDL_CursorVisible()) {
                    SDLMouse.SDL_HideCursor();
                }
            }
            default -> {
                if (SDLMouse.SDL_GetWindowRelativeMouseMode(windowHandle)) {
                    SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, false);
                }
                if (!SDLMouse.SDL_CursorVisible()) {
                    SDLMouse.SDL_ShowCursor();
                }
            }
        }

        if (this.handle != 0L) {
            SDLMouse.SDL_SetCursor(this.handle);
        }
    }

    static CursorType createStandardCursor(final int inputMode, final int shape, final String name, final CursorType fallback) {
        long handle = SDLMouse.SDL_CreateSystemCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, inputMode);
    }

    static CursorType createStandardCursor(final int shape, final String name, final CursorType fallback) {
        long handle = SDLMouse.SDL_CreateSystemCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, CURSOR_NORMAL);
    }
}