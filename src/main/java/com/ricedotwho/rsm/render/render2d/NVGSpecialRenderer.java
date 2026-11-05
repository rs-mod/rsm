package com.ricedotwho.rsm.render.render2d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Backend-agnostic replacement for the old NanoVG PictureInPicture renderer.
 *
 * <p>Runs the supplied rendering commands inside a {@link GuiRecorder} and flushes the resulting
 * GUI render states immediately. Works on both OpenGL and Vulkan.</p>
 */
public final class NVGSpecialRenderer {

    /**
     * Draw NVG-style content as immediate GUI render states.
     *
     * @param gfx the active GUI graphics extractor
     * @param x unused; retained for source compatibility
     * @param y unused; retained for source compatibility
     * @param width unused; retained for source compatibility
     * @param height unused; retained for source compatibility
     * @param renderContent the drawing commands to record
     */
    public static void draw(
            GuiGraphicsExtractor gfx,
            int x,
            int y,
            int width,
            int height,
            Runnable renderContent
    ) {
        if (Minecraft.getInstance().level == null) return;

        boolean topLevel = !NVGUtils.isDrawing();
        if (topLevel) {
            NVGUtils.beginFrame(gfx);
        }

        try {
            renderContent.run();
        } finally {
            if (topLevel) {
                NVGUtils.endFrame();
            }
        }
    }

    private NVGSpecialRenderer() {
    }
}
