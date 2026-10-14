package com.ricedotwho.rsm.utils.mouse;

import org.lwjgl.sdl.SDLMouse;

public class CursorTypes {
    public static final CursorType ARROW = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT, "arrow", CursorType.DEFAULT);
    public static final CursorType IBEAM = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_TEXT, "ibeam", CursorType.DEFAULT);
    public static final CursorType CROSSHAIR = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR, "crosshair", CursorType.DEFAULT);
    public static final CursorType POINTING_HAND = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_POINTER, "pointing_hand", CursorType.DEFAULT);
    public static final CursorType RESIZE_NS = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE, "resize_ns", CursorType.DEFAULT);
    public static final CursorType RESIZE_EW = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE, "resize_ew", CursorType.DEFAULT);
    public static final CursorType RESIZE_ALL = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_MOVE, "resize_all", CursorType.DEFAULT);
    public static final CursorType NOT_ALLOWED = CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED, "not_allowed", CursorType.DEFAULT);
    public static final CursorType HIDDEN = CursorType.createStandardCursor(CursorType.CURSOR_HIDDEN, SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT, "hidden", CursorType.DEFAULT);
}