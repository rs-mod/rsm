package com.ricedotwho.rsm.utils.mouse;

import com.mojang.blaze3d.platform.Window;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.sdl.SDLMouse;

/// Mod implementation of {@link com.mojang.blaze3d.platform.cursor.CursorType} allowing for input mode, and not restricted by the minecraft option
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CursorType {
    public static final CursorType DEFAULT = new CursorType("default", 0L, false);
    public static final CursorType DEFAULT_DISABLED = new CursorType("default", 0L, true);

    private final String name;
    public final long handle;
    public final boolean hidden;

    @Override
    public String toString() {
        return this.name;
    }

    public void select(Window window) {
        var visible = SDLMouse.SDL_CursorVisible();
        if (hidden) {
            if (visible) SDLMouse.SDL_HideCursor();
        } else if (!visible) {
            SDLMouse.SDL_ShowCursor();
        }
        SDLMouse.SDL_SetCursor(this.handle == 0L ? SDLMouse.SDL_GetDefaultCursor() : this.handle);
    }

    static CursorType createStandardCursor(final boolean hidden, final int shape, final String name, final CursorType fallback) {
        long handle = SDLMouse.SDL_CreateSystemCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, hidden);
    }

    static CursorType createStandardCursor(final int shape, final String name, final CursorType fallback) {
        long handle = SDLMouse.SDL_CreateSystemCursor(shape);
        return handle == 0L ? fallback : new CursorType(name, handle, false);
    }
}
