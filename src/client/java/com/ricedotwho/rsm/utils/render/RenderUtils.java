package com.ricedotwho.rsm.utils.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.shader.ColorHelper;
import com.ricedotwho.rsm.utils.render.shader.GlowFilter;
import com.ricedotwho.rsm.utils.render.shader.Shader;
import com.ricedotwho.rsm.utils.render.shader.ShaderUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderUtils {

    public static final HashMap<Integer, Integer> glowCache = new HashMap<Integer, Integer>();
    public static final Shader ROUNDED_GRADIENT = new Shader("rounded_gradient.frag");
    public static final int STEPS = 60;
    public static final double ANGLE = Math.PI * 2 / STEPS;
    public static final int EX_STEPS = 120;
    public static final double EX_ANGLE = Math.PI * 2 / EX_STEPS;
    private static final Shader ROUNDED = new Shader("rounded.frag");
    private static final Shader BLUR = new Shader("gaussian.frag");
    private static final Shader ROUNDED_BLURRED = new Shader("rounded_blurred.frag");
    private static final Shader ROUNDED_BLURRED_GRADIENT = new Shader("rounded_blurred_gradient.frag");
    private static final Shader ROUNDED_OUTLINE = new Shader("rounded_outline.frag");
    private static final Shader ROUNDED_OUTLINE_GRADIENT = new Shader("rounded_outline_gradient.frag");
    private static final Shader ROUNDED_TEXTURE = new Shader("rounded_texture.frag");
    private static final Shader GLOW = new Shader("glow.frag");
    private static final Shader DOWN = new Shader("down.frag");
    private static final List<Framebuffer> framebufferList = new ArrayList<>();
    public static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    public static Framebuffer buffer = new Framebuffer(1, 1, false);
    private static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int currentIterations;


    public boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        boolean isWithinX = mouseX >= x && mouseX <= x + width;
        boolean isWithinY = mouseY >= y && mouseY <= y + height;

        return isWithinX && isWithinY;
    }

    public void drawRect(int posX, int posY, int width, int height, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;

        glBegin(GL_QUADS);
        glColor4f(red, green, blue, alpha);
        glVertex2f(posX, posY);
        glVertex2f(posX, posY + height);
        glVertex2f(posX + width, posY + height);
        glVertex2f(posX + width, posY);
        glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void drawRect(float left, float top, float right, float bottom, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color((color >> 16 & 0xFF) / 255.0F,
                (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                (color >> 24 & 0xFF) / 255.0F);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(left, bottom, 0).endVertex();
        wr.pos(right, bottom, 0).endVertex();
        wr.pos(right, top, 0).endVertex();
        wr.pos(left, top, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
    }

    public void drawRect2(double x, double y, double x2, double y2, Color color) {
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }

    public void enableScissor() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void scissor(ScaledResolution scaledResolution, double x, double y, double width, double height) {
        if (x + width == x || y + height == y || x < 0 || y + height < 0) return;
        final int scaleFactor = scaledResolution.getScaleFactor();
        GL11.glScissor((int) Math.round(x * scaleFactor), (int) Math.round((scaledResolution.getScaledHeight() - (y + height)) * scaleFactor), (int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor));
    }

    // for scaledHeight, scale - use WINDOW.getGuiScaledHeight(), WINDOW.getGuiScale() if you haven’t changed them yourself
    public void scissor(double x, double y, double width, double height, double scale, double scaledHeight) {
        glScissor((int) (x * scale),
                (int) ((scaledHeight - y) * scale),
                (int) (width * scale),
                (int) (height * scale));
    }

    public void drawImage(int textureID, final float x, final float y, final float width, final float height) {
        GlStateManager.bindTexture(textureID);


        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.enableBlend();

        GlStateManager.enableAlpha();


        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);


        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.resetColor();
        GlStateManager.disableBlend();

    }
    public void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public void drawImage(ResourceLocation image, float x, float y, float width, float height, Color c) {
        GL11.glColor4f(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public void drawImage(ResourceLocation image, float x, float y, float width, float height, float alpha) {
        GL11.glColor4f(1f, 1f, 1f, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public void drawArrow(float x, float y, float size, float width, Color color, boolean expanded) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(size, size, 1);

        GL11.glLineWidth(width);
        GL11.glColor3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);

        GL11.glBegin(GL11.GL_LINES);

        if (expanded) {
            GL11.glVertex2f(0.2f, 0.3f);
            GL11.glVertex2f(0.5f, 0.7f);

            GL11.glVertex2f(0.5f, 0.7f);
            GL11.glVertex2f(0.8f, 0.3f);
        } else {
            GL11.glVertex2f(0.2f, 0.7f);
            GL11.glVertex2f(0.5f, 0.3f);

            GL11.glVertex2f(0.5f, 0.3f);
            GL11.glVertex2f(0.8f, 0.7f);
        }

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public Color fadeBetweenColors(Color startColor, Color endColor, float percentage) {
        int lerpedRed = (int) (startColor.getRed() * (1 - percentage) + endColor.getRed() * percentage);
        int lerpedGreen = (int) (startColor.getGreen() * (1 - percentage) + endColor.getGreen() * percentage);
        int lerpedBlue = (int) (startColor.getBlue() * (1 - percentage) + endColor.getBlue() * percentage);

        lerpedRed = Math.min(Math.max(lerpedRed, 0), 255);
        lerpedGreen = Math.min(Math.max(lerpedGreen, 0), 255);
        lerpedBlue = Math.min(Math.max(lerpedBlue, 0), 255);

        return new Color(lerpedRed, lerpedGreen, lerpedBlue);
    }

    public void setColor(Color color) {
        float alpha = (color.getRGB() >> 24 & 0xFF) / 255.0F;
        float red = (color.getRGB() >> 16 & 0xFF) / 255.0F;
        float green = (color.getRGB() >> 8 & 0xFF) / 255.0F;
        float blue = (color.getRGB() & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public void drawCircle(double x, double y, double radius, Color color) {
        drawSetup();
        applyColor(color);

        x += radius;
        y += radius;

        glBegin(GL_TRIANGLE_FAN);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    // progress [1;100]
    public void drawCircle(double x, double y, double radius, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        disableCull();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        enableCull();
        drawFinish();
    }

    private void glow(int framebufferTexture, int iterations, int offset) {
        if (currentIterations != iterations || (framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight)) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_ONE, GL_ONE);

        GL11.glClearColor(0, 0, 0, 0);
        renderFBO(framebufferList.get(1), framebufferTexture, DOWN, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, DOWN, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, GLOW, offset);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        GLOW.load();
        GLOW.setUniformf("offset", offset, offset);
        GLOW.setUniformi("inTexture", 0);
        GLOW.setUniformi("check", 1);
        GLOW.setUniformi("textureToCheck", 16);
        GLOW.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        GLOW.setUniformf("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        RenderUtils.bindTexture(framebufferTexture);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        RenderUtils.bindTexture(framebufferList.get(1).framebufferTexture);
        Shader.draw();
        GLOW.unload();


        GlStateManager.clearColor(0, 0, 0, 0);
        mc.getFramebuffer().bindFramebuffer(false);
        RenderUtils.bindTexture(framebufferList.get(0).framebufferTexture);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Shader.draw();
        GlStateManager.bindTexture(0);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderFBO(Framebuffer framebuffer, int framebufferTexture, Shader shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.load();
        RenderUtils.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        Shader.draw();
        shader.unload();
    }

    private void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        framebufferList.add(framebuffer = RenderUtils.createFrameBuffer(null, true));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (mc.displayWidth / Math.pow(2, i)), (int) (mc.displayHeight / Math.pow(2, i)), true);
            currentBuffer.setFramebufferFilter(GL_LINEAR);

            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }

    public Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }


    public void drawCirclePart(double x, double y, double radius, Part part, Color color) {
        double angle = ANGLE / part.ratio;

        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public void drawBlurredCircle(double x, double y, double radius, double blurRadius, Color color) {
        Color transparent = ColorHelper.injectAlpha(color, 0);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        for (int i = 0; i <= EX_STEPS; i++) {
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for (int i = 0; i <= EX_STEPS + 1; i++) {
            if (i % 2 == 1) {
                applyColor(transparent);
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(color);
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public void drawCircleOutline(double x, double y, double radius, float thickness, Color color) {
        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thickness);
        applyColor(color);

        glBegin(GL_LINE_LOOP);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public void drawCirclePartOutline(double x, double y, double radius, float thickness, Part part, Color color) {
        double angle = ANGLE / part.ratio;

        y += radius;
        x += radius;

        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thickness);
        applyColor(color);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    private Framebuffer createFrameBufferGoodSize(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return framebuffer;
    }

    public void blur(float radius) {
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);


        buffer = RenderUtils.createFrameBufferGoodSize(buffer);

        buffer.framebufferClear();
        buffer.bindFramebuffer(true);
        BLUR.load();
        setupUniforms(1, 0, radius);

        RenderUtils.bindTexture(mc.getFramebuffer().framebufferTexture);

        Shader.draw();
        buffer.unbindFramebuffer();
        BLUR.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        BLUR.load();
        setupUniforms(0, 1, radius);

        RenderUtils.bindTexture(buffer.framebufferTexture);
        Shader.draw();
        BLUR.unload();

        RenderUtils.resetColor();
        GlStateManager.bindTexture(0);
    }

    private void setupUniforms(float dir1, float dir2, float radius) {
        BLUR.setUniformi("textureIn", 0);
        BLUR.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        BLUR.setUniformf("direction", dir1, dir2);
        BLUR.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        OpenGlHelper.glUniform1(BLUR.getUniformf("weights"), weightBuffer);
    }

    private float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    /**
     *
     * @param x x
     * @param y y
     * @param radius radius
     * @param thickness thickness
     * @param progress progress
     * @param direction 1 - clockwise, 0 - counter clockwise
     * @param color colour
     */
    public void drawCircleOutline(double x, double y, double radius, float thickness, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thickness);
        applyColor(color);

        glBegin(GL_LINE_STRIP);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public void drawRainbowCircle(double x, double y, double radius, double blurRadius) {
        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(Color.WHITE);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= EX_STEPS; i++) {
            applyColor(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f));
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for (int i = 0; i <= EX_STEPS + 1; i++) {
            if (i % 2 == 1) {
                applyColor(ColorHelper.injectAlpha(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f), 0));
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f));
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
    }

    public void drawRect(double x, double y, double width, double height, Color color) {
        drawSetup();
        y += height;
        applyColor(color);

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x + width, y - height);
        glVertex2d(x, y - height);
        glEnd();

        drawFinish();
    }

    public void drawQuad(double x, double y, double x2, double y2, Color color) {
        drawSetup();
        applyColor(color);

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x2, y);
        glVertex2d(x2, y2);
        glVertex2d(x, y2);
        glEnd();

        drawFinish();
    }

    public void drawGradientRect(double x, double y, double width, double height, Color... clrs) {
        drawSetup();
        glShadeModel(GL_SMOOTH);

        glBegin(GL_QUADS);
        applyColor(clrs[1]);
        glVertex2d(x, y);
        applyColor(clrs[2]);
        glVertex2d(x + width, y);
        applyColor(clrs[3]);
        glVertex2d(x + width, y - height);
        applyColor(clrs[0]);
        glVertex2d(x, y - height);
        glEnd();

        glShadeModel(GL_FLAT);
        drawFinish();
    }

    public void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED.load();
        ROUNDED.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED.setUniformf("round", (float) radius * 2);
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED.unload();

        drawFinish();
    }

    public void drawRoundedRectPos(double x, double y, double x2, double y2, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        double width = x2 - x;
        double height = y2 - y;

        ROUNDED.load();
        ROUNDED.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED.setUniformf("round", (float) radius * 2);
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED.unload();

        drawFinish();
    }

    public void drawRoundedRectSide(double x, double y, double x2, double y2, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        double width = Math.max(x, x2) - Math.min(x, x2);
        double height = Math.max(y, y2) - Math.min(y, y2);


        ROUNDED.load();
        ROUNDED.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED.setUniformf("round", (float) radius * 2);
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED.unload();

        drawFinish();
    }

    public void drawHorizontalText(String text, int x, int y, double speed, double index, Color color1, Color color2) {
        int offsetX = 0;
        for (int i = 0; i < text.length(); i++) {
            mc.fontRendererObj.drawString(text.substring(i, i + 1), x + offsetX, y, ColorUtils.getColorFromIndex((int) speed, (int) (i * index), color1, color2, false).getRGB());
            offsetX += mc.fontRendererObj.getStringWidth(text.substring(i, i + 1));
        }
    }

    public boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity e = mc.getRenderViewEntity();
        Frustum f = new Frustum();
        f.setPosition(e.posX, e.posY, e.posZ);
        return f.isBoundingBoxInFrustum(bb);
    }

    public void drawRoundedGradientRect(double x, double y, double width, double height, double radius, Color... colors) {
        float[] c = ColorHelper.getColorComps(colors[0]);
        float[] c1 = ColorHelper.getColorComps(colors[1]);
        float[] c2 = ColorHelper.getColorComps(colors[2]);
        float[] c3 = ColorHelper.getColorComps(colors[3]);

        drawSetup();

        ROUNDED_GRADIENT.load();
        ROUNDED_GRADIENT.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_GRADIENT.setUniformf("round", (float) radius * 2);
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED_GRADIENT.unload();

        drawFinish();
    }

    public void drawRoundedBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        ROUNDED_BLURRED.load();
        ROUNDED_BLURRED.setUniformf("size", (float) (width + 2 * blurR), (float) (height + 2 * blurR));
        ROUNDED_BLURRED.setUniformf("softness", blurR);
        ROUNDED_BLURRED.setUniformf("radius", (float) roundR);
        ROUNDED_BLURRED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw((float) (x - blurR), (float) (y - blurR), (float) (width + blurR * 2), (float) (height + blurR * 2));
        ROUNDED_BLURRED.unload();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
        GL11.glPopAttrib();
    }

    public void drawRoundedGradientBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color... colors) {
        float[] c = ColorHelper.getColorComps(colors[0]);
        float[] c1 = ColorHelper.getColorComps(colors[1]);
        float[] c2 = ColorHelper.getColorComps(colors[2]);
        float[] c3 = ColorHelper.getColorComps(colors[3]);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        GL11.glPushAttrib(GL_CURRENT_BIT);

        ROUNDED_BLURRED_GRADIENT.load();
        ROUNDED_BLURRED_GRADIENT.setUniformf("size", (float) (width + 2 * blurR), (float) (height + 2 * blurR));
        ROUNDED_BLURRED_GRADIENT.setUniformf("softness", blurR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("radius", (float) roundR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw((float) (x - blurR), (float) (y - blurR), (float) (width + blurR * 2), (float) (height + blurR * 2));
        ROUNDED_BLURRED_GRADIENT.unload();

        GL11.glPopAttrib();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public void drawRoundedRectOutline(double x, double y, double width, double height, double radius, float thickness, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED_OUTLINE.load();
        ROUNDED_OUTLINE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_OUTLINE.setUniformf("round", (float) radius * 2);
        ROUNDED_OUTLINE.setUniformf("thickness", thickness);
        ROUNDED_OUTLINE.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED_OUTLINE.unload();

        drawFinish();
    }
    public void drawCheckmark(float x, float y, float size, float width, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(size, size, 1);

        GL11.glLineWidth(width);

        GL11.glColor4f(color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);

        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex2f(0.1f, 0.7f);
        GL11.glVertex2f(0.3f, 0.9f);

        GL11.glVertex2f(0.3f, 0.9f);
        GL11.glVertex2f(0.8f, 0.4f);

        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public void drawRoundedRectOutlineGradient(double x, double y, double width, double height, double radius, float thickness, Color... colors) {
        drawSetup();


        float[] c = ColorHelper.getColorComps(colors[0]);
        float[] c1 = ColorHelper.getColorComps(colors[1]);
        float[] c2 = ColorHelper.getColorComps(colors[2]);
        float[] c3 = ColorHelper.getColorComps(colors[3]);

        ROUNDED_OUTLINE_GRADIENT.load();
        ROUNDED_OUTLINE_GRADIENT.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("round", (float) radius * 2);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("thickness", thickness);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_OUTLINE_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        ROUNDED_OUTLINE_GRADIENT.unload();

        drawFinish();
    }

    public void drawTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        drawTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight);
    }

    public void drawTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        disableBlend();
    }

    public void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        drawRoundedTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight, radius);
    }

    public void drawRoundedTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.3f);

        MC.getFramebuffer().bindFramebuffer(false);
        ShaderUtils.initStencilReplace();
        drawRoundedRect(x, y, width, height, radius, new Color(255, 255, 255));
        ShaderUtils.uninitStencilReplace();

        bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_ALPHA_TEST);
        disableBlend();
    }

    public void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double radius) {
        drawRoundedTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, radius);
    }

    public void drawRoundedTexture(int texId, double x, double y, double width, double height, double radius) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        ROUNDED_TEXTURE.load();
        ROUNDED_TEXTURE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_TEXTURE.setUniformf("round", (float) radius * 2);
        bindTexture(texId);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        bindTexture(0);
        ROUNDED_TEXTURE.unload();

        disableBlend();
    }

    public void drawGlow(Runnable runnable) {

        stencilFramebuffer = createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        runnable.run();
        stencilFramebuffer.unbindFramebuffer();

        glow(stencilFramebuffer.framebufferTexture, 2, 3);
    }

    public void drawGlow(Runnable runnable, int intensity) {

        stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        runnable.run();
        stencilFramebuffer.unbindFramebuffer();

        glow(stencilFramebuffer.framebufferTexture, 2, intensity);
    }

    public void drawGlow(double x, double y, int width, int height, int glowRadius, Color color) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        bindTexture(getGlowTexture(width, height, glowRadius));
        width += glowRadius * 2;
        height += glowRadius * 2;
        x -= glowRadius;
        y -= height - glowRadius;

        applyColor(color);
        glBegin(GL_QUADS);
        glTexCoord2d(0, 1);
        glVertex2d(x, y);
        glTexCoord2d(0, 0);
        glVertex2d(x, y + height);
        glTexCoord2d(1, 0);
        glVertex2d(x + width, y + height);
        glTexCoord2d(1, 1);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        glDisable(GL_ALPHA_TEST);
        disableBlend();
    }

    public int getGlowTexture(int width, int height, int blurRadius) {
        int identifier = (width * 401 + height) * 407 + blurRadius;
        int texId = glowCache.getOrDefault(identifier, -1);

        if (texId == -1) {
            BufferedImage original = new BufferedImage(width + blurRadius * 2, height + blurRadius * 2, BufferedImage.TYPE_INT_ARGB_PRE);

            Graphics g = original.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(blurRadius, blurRadius, width, height);
            g.dispose();

            GlowFilter glow = new GlowFilter(blurRadius);
            BufferedImage blurred = glow.filter(original, null);
            try {
                texId = ShaderUtils.loadTexture(blurred);
                glowCache.put(identifier, texId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return texId;
    }

    public void applyColor(Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void resetColor() {
        glColor4f(1f, 1f, 1f, 1f);
    }

    public void drawSetup() {
        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawFinish() {
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = -180;
        ent.rotationYaw = -180;
        ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void drawPlayerHead(int x, int y, int width, EntityLivingBase player) {
        Minecraft mc = Minecraft.getMinecraft();
        NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getUniqueID());
        if (playerInfo != null) {
            mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());
            GL11.glColor4f(1F, 1F, 1F, 1F);
            Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, width, 64F, 64F);
            Gui.drawRect(x, y, x + width, y + width, new Color(200, 20, 20, player.hurtTime * 25).getRGB());

        }
    }

    public void mcText(String text, Number x, Number y, Number scale, Colour colour, boolean shadow, boolean center) {
        drawText(text + EnumChatFormatting.RESET, x.floatValue(), y.floatValue(), scale.doubleValue(), colour, shadow, center);
    }

    public void drawText(String text, float x, float y, double scale, Colour colour, boolean shadow, boolean center) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        translate(x, y, 0f);
        scale(scale, scale, scale);
        for (String line : text.split("\n")) {
            float yOffset = center ? mc.fontRendererObj.FONT_HEIGHT : 0f;
            float xOffset = center ? mc.fontRendererObj.getStringWidth(line) / -2f: 0f;
            mc.fontRendererObj.drawString(line, xOffset, yOffset, colour.getRGB(), shadow);
        }

        GlStateManager.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void scale(Number x, Number y, Number z) {
        GlStateManager.scale(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public int getMCTextWidth(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    public int getMCTextHeight() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    public void renderMCText(String text, Vector2d position, Vector2d scale, Colour colour, boolean shadow, boolean center) {
        mcText(text, center ? position.x + scale.x / 2 : position.x, position.y - scale.y, scale.y / 6.5, colour, shadow, center);
    }

    public float getPartialTicks() {
        return ((AccessorMinecraft) mc).getTimer().renderPartialTicks;
    }

    public Double[] fixRenderPos(double x, double y, double z) {
        return new Double[]{x+getRenderX(), y+getRenderY(), z+getRenderZ()};
    }

    private Double getRenderX() {
        return ((AccessorRenderManager) mc.getRenderManager()).getRenderX();
    }

    private Double getRenderY() {
        return ((AccessorRenderManager) mc.getRenderManager()).getRenderY();
    }

    private Double getRenderZ() {
        return ((AccessorRenderManager) mc.getRenderManager()).getRenderZ();
    }

    public enum Part {
        FIRST_QUARTER(4, Math.PI / 2),
        SECOND_QUARTER(4, Math.PI),
        THIRD_QUARTER(4, 3 * Math.PI / 2),
        FOURTH_QUARTER(4, 0d),
        FIRST_HALF(2, Math.PI / 2),
        SECOND_HALF(2, Math.PI),
        THIRD_HALF(2, 3 * Math.PI / 2),
        FOURTH_HALF(2, 0d);

        private final int ratio;
        private final double additionalAngle;

        Part(int ratio, double addAngle) {
            this.ratio = ratio;
            this.additionalAngle = addAngle;
        }
    }
}
