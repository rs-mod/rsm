package com.ricedotwho.rsm.utils.render.render2d;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ricedotwho.rsm.utils.GlDeviceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.lwjgl.opengl.GL33C;

public class NVGSpecialRenderer extends PictureInPictureRenderer<NVGSpecialRenderer.NVGRenderState> {

    public NVGSpecialRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(NVGRenderState state, PoseStack poseStack) {
        var colorTex = RenderSystem.outputColorTextureOverride;
        if (colorTex == null) return;

        int width = colorTex.getWidth(0);
        int height = colorTex.getHeight(0);
        if (width == 0 || height == 0) return;

        if (!(colorTex.texture() instanceof GlTexture glTex)) return;
        int texId = glTex.glId();

        int fbo = GL33C.glGenFramebuffers();
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, fbo);
        GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER, GL33C.GL_COLOR_ATTACHMENT0, GL33C.GL_TEXTURE_2D, texId, 0);
        GlStateManager._viewport(0, 0, width, height);

        GL33C.glBindSampler(0, 0);
        NVGUtils.beginFrame((float) width, (float) height);
        state.renderContent.run();
        NVGUtils.endFrame();

        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);
        GL33C.glDeleteFramebuffers(fbo);
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 2f;
    }

    @Override
    public @NotNull Class<NVGRenderState> getRenderStateClass() {
        return NVGRenderState.class;
    }

    @Override
    protected @NotNull String getTextureLabel() {
        return "nvg_renderer_rsm";
    }


    public record NVGRenderState(int x, int y, int width, int height, Matrix3x2f poseMatrix, ScreenRectangle scissor,
                                 ScreenRectangle bounds,
                                 Runnable renderContent) implements PictureInPictureRenderState {

        @Override
            public float scale() {
                return 1f;
            }

            @Override
            public int x0() {
                return x;
            }

            @Override
            public int y0() {
                return y;
            }

            @Override
            public int x1() {
                return x + width;
            }

            @Override
            public int y1() {
                return y + height;
            }

            @Override
            public ScreenRectangle scissorArea() {
                return scissor;
            }
        }

    /**
     * Draw NVG content as a special GUI element.
     */
    public static void draw(
            GuiGraphicsExtractor context,
            int x,
            int y,
            int width,
            int height,
            Runnable renderContent
    ) {
        if (Minecraft.getInstance().level == null) return;
        ScreenRectangle scissor = context.scissorStack.peek();
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        ScreenRectangle bounds = createBounds(
                x, y, x + width, y + height,
                pose, scissor
        );

        NVGRenderState state = new NVGRenderState(
                x, y, width, height,
                pose, scissor, bounds,
                renderContent
        );

        context.guiRenderState.addPicturesInPictureState(state);
    }

    private static ScreenRectangle createBounds(
            int x0,
            int y0,
            int x1,
            int y1,
            Matrix3x2f pose,
            ScreenRectangle scissorArea
    ) {
        ScreenRectangle screenRect =
                new ScreenRectangle(x0, y0, x1 - x0, y1 - y0)
                        .transformMaxBounds(pose);

        return scissorArea != null
                ? scissorArea.intersection(screenRect)
                : screenRect;
    }
}