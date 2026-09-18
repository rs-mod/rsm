package com.ricedotwho.rsm.utils.mouse;

import org.lwjgl.glfw.GLFW;

public class CursorTypes {
    public static final CursorType ARROW = CursorType.createStandardCursor(GLFW.GLFW_ARROW_CURSOR, "arrow", CursorType.DEFAULT);
    public static final CursorType IBEAM = CursorType.createStandardCursor(GLFW.GLFW_IBEAM_CURSOR, "ibeam", CursorType.DEFAULT);
    public static final CursorType CROSSHAIR = CursorType.createStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR, "crosshair", CursorType.DEFAULT);
    public static final CursorType POINTING_HAND = CursorType.createStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR, "pointing_hand", CursorType.DEFAULT);
    public static final CursorType RESIZE_NS = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NS_CURSOR, "resize_ns", CursorType.DEFAULT);
    public static final CursorType RESIZE_EW = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_EW_CURSOR, "resize_ew", CursorType.DEFAULT);
    public static final CursorType RESIZE_ALL = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR, "resize_all", CursorType.DEFAULT);
    public static final CursorType NOT_ALLOWED = CursorType.createStandardCursor(GLFW.GLFW_NOT_ALLOWED_CURSOR, "not_allowed", CursorType.DEFAULT);
    public static final CursorType HIDDEN = CursorType.createStandardCursor(GLFW.GLFW_CURSOR_HIDDEN, GLFW.GLFW_ARROW_CURSOR, "hidden", CursorType.DEFAULT);
}
