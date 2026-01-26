package com.ricedotwho.rsm.utils.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.shader.ColorHelper;
import com.ricedotwho.rsm.utils.render.shader.GlowFilter;
import com.ricedotwho.rsm.utils.render.shader.Shader;
import com.ricedotwho.rsm.utils.render.shader.ShaderUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderUtils implements Accessor {
    public static final HashMap<Integer, Integer> glowCache = new HashMap<>();
    public static final Shader ROUNDED_GRADIENT = new Shader("rounded_gradient.frag");
    public static final int STEPS = 60;
    public static final double ANGLE = Math.PI * 2 / STEPS;
    public static final int EX_STEPS = 120;
    public static final double EX_ANGLE = Math.PI * 2 / EX_STEPS;
    private static final Shader ROUNDED = new Shader("rounded.frag");
    private static final Shader ROUNDED_BLURRED = new Shader("rounded_blurred.frag");
    private static final Shader ROUNDED_BLURRED_GRADIENT = new Shader("rounded_blurred_gradient.frag");
    private static final Shader ROUNDED_OUTLINE = new Shader("rounded_outline.frag");
    private static final Shader ROUNDED_OUTLINE_GRADIENT = new Shader("rounded_outline_gradient.frag");
    private static final Shader ROUNDED_TEXTURE = new Shader("rounded_texture.frag");


    public boolean isHovering(double mouseX, double mouseY, float x, float y, float width, float height) {
        boolean isWithinX = mouseX >= x && mouseX <= x + width;
        boolean isWithinY = mouseY >= y && mouseY <= y + height;

        return isWithinX && isWithinY;
    }

    public void drawRect(int posX, int posY, int width, int height, Color color) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    public void drawRect(float left, float top, float right, float bottom, int color) {
        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left,  top);
        GL11.glVertex2f(left,  bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
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

    public void scissor(Window window, double x, double y, double width, double height) {
        if (x + width == x || y + height == y || x < 0 || y + height < 0) return;
        final int scaleFactor = window.getGuiScale();
        GL11.glScissor((int) Math.round(x * scaleFactor), (int) Math.round((window.getGuiScaledHeight() - (y + height)) * scaleFactor), (int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor));
    }

    // for scaledHeight, scale - use WINDOW.getGuiScaledHeight(), WINDOW.getGuiScale() if you haven’t changed them yourself
    public void scissor(double x, double y, double width, double height, double scale, double scaledHeight) {
        glScissor((int) (x * scale),
                (int) ((scaledHeight - y) * scale),
                (int) (width * scale),
                (int) (height * scale));
    }

    public void drawImage(GuiGraphics gfx, ResourceLocation image, int x, int y, int width, int height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawTexture(gfx, image, x, y, 0, 0, width, height, width, height);
    }

    public void drawImage(GuiGraphics gfx, ResourceLocation image, float x, float y, float width, float height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawTexture(gfx, image, (int) x, (int) y, 0, 0, (int) width, (int) height, (int) width, (int) height);
    }

    public void drawImage(GuiGraphics gfx, ResourceLocation image, float x, float y, float width, float height, Color c) {
        GL11.glColor4f(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        drawTexture(gfx, image, (int) x, (int) y, 0, 0, (int) width, (int) height, (int) width, (int) height);
    }

    public void drawImage(GuiGraphics gfx, ResourceLocation image, float x, float y, float width, float height, float alpha) {
        GL11.glColor4f(1f, 1f, 1f, alpha);
        drawTexture(gfx, image, (int) x, (int) y, 0, 0, (int) width, (int) height, (int) width, (int) height);
    }

    public static void drawTexture(
            GuiGraphics gfx,
            ResourceLocation texture,
            int x, int y,
            int u, int v,
            int width, int height,
            int textureWidth, int textureHeight
    ) {
        gfx.blit(
                texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight
        );
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
        glDisable(GL_CULL_FACE);
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
        glEnable(GL_CULL_FACE);
        drawFinish();
    }

    public void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
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

//    public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
//        float f = 1.0F / textureWidth;
//        float f1 = 1.0F / textureHeight;
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
//        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
//        worldrenderer.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
//        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
//        worldrenderer.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
//        worldrenderer.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
//        tessellator.draw();
//    }

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

    public boolean isInViewFrustrum(AABB bb) {
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        Frustum frustum = Minecraft.getInstance().levelRenderer.getCapturedFrustum();
        if (frustum == null || cameraEntity == null) return false;
        frustum.prepare(cameraEntity.getX(), cameraEntity.getY(), cameraEntity.getZ());
        return frustum.isVisible(bb);
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

//    public void drawTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
//        drawTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight);
//    }
//
//    public void drawTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        resetColor();
//
//        bindTexture(texId);
//
//        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
//        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
//        y -= height;
//        texX = texX / iWidth;
//        texY = texY / iHeight;
//        texWidth = texWidth / iWidth;
//        texHeight = texHeight / iHeight;
//
//        glBegin(GL_QUADS);
//        glTexCoord2d(texX, texY);
//        glVertex2d(x, y);
//        glTexCoord2d(texX, texY + texHeight);
//        glVertex2d(x, y + height);
//        glTexCoord2d(texX + texWidth, texY + texHeight);
//        glVertex2d(x + width, y + height);
//        glTexCoord2d(texX + texWidth, texY);
//        glVertex2d(x + width, y);
//        glEnd();
//
//        bindTexture(0);
//        glDisable(GL_BLEND);
//    }

//    public void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
//        drawRoundedTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight, radius);
//    }

//    public void drawRoundedTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//
//        glEnable(GL_ALPHA_TEST);
//        glAlphaFunc(GL_GREATER, 0.3f);
//
//        MC.getFramebuffer().bindFramebuffer(false);
//        ShaderUtils.initStencilReplace();
//        drawRoundedRect(x, y, width, height, radius, new Color(255, 255, 255));
//        ShaderUtils.uninitStencilReplace();
//
//        bindTexture(texId);
//
//        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
//        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
//        y -= height;
//        texX = texX / iWidth;
//        texY = texY / iHeight;
//        texWidth = texWidth / iWidth;
//        texHeight = texHeight / iHeight;
//
//        glBegin(GL_QUADS);
//        glTexCoord2d(texX, texY);
//        glVertex2d(x, y);
//        glTexCoord2d(texX, texY + texHeight);
//        glVertex2d(x, y + height);
//        glTexCoord2d(texX + texWidth, texY + texHeight);
//        glVertex2d(x + width, y + height);
//        glTexCoord2d(texX + texWidth, texY);
//        glVertex2d(x + width, y);
//        glEnd();
//
//        bindTexture(0);
//        glDisable(GL_STENCIL_TEST);
//        glDisable(GL_ALPHA_TEST);
//        glEnable(GL_BLEND);
//    }
//
//    public void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double radius) {
//        drawRoundedTexture(ShaderUtils.getTextureId(identifier), x, y, width, height, radius);
//    }

    public void drawRoundedTexture(int texId, double x, double y, double width, double height, double radius) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        ROUNDED_TEXTURE.load();
        ROUNDED_TEXTURE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_TEXTURE.setUniformf("round", (float) radius * 2);
        bindTexture(texId);
        Shader.draw((float) x, (float) (y), (float) width, (float) height);
        bindTexture(0);
        ROUNDED_TEXTURE.unload();

        glDisable(GL_BLEND);
    }

    public void drawGlow(double x, double y, int width, int height, int glowRadius, Color color) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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
        glDisable(GL_BLEND);
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
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawFinish() {
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        resetColor();
    }

//    public void mcText(String text, Number x, Number y, Number scale, Colour colour, boolean shadow, boolean center) {
//        drawText(text + ChatFormatting.RESET, x.floatValue(), y.floatValue(), scale.doubleValue(), colour, shadow, center);
//    }

//    public void drawText(String text, float x, float y, double scale, Colour colour, boolean shadow, boolean center) {
//        glPushMatrix();
//        glEnable(GL_BLEND);
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//        glTranslatef(x, y, 0);
//        scale(scale, scale, scale);
//        for (String line : text.split("\n")) {
//            float yOffset = center ? mc.fontRendererObj.FONT_HEIGHT : 0f;
//            float xOffset = center ? mc.fontRendererObj.getStringWidth(line) / -2f: 0f;
//            mc.fontRendererObj.drawString(line, xOffset, yOffset, colour.getRGB(), shadow);
//        }
//        resetColor();
//        glDisable(GL_BLEND);
//        glPopMatrix();
//    }

    public void scale(float x, float y, float z) {
        glScaled(x, y, z);
    }

    public void scale(double x, double y, double z) {
        glScaled(x, y, z);
    }

//    public int getMCTextWidth(String text) {
//        return mc.fontRendererObj.getStringWidth(text);
//    }
//
//    public int getMCTextHeight() {
//        return mc.fontRendererObj.FONT_HEIGHT;
//    }

//    public void renderMCText(String text, Vector2d position, Vector2d scale, Colour colour, boolean shadow, boolean center) {
//        mcText(text, center ? position.x + scale.x / 2 : position.x, position.y - scale.y, scale.y / 6.5, colour, shadow, center);
//    }

//    public float getPartialTicks() {
//        return ((AccessorMinecraft) mc).getTimer().renderPartialTicks;
//    }
//
//    public Double[] fixRenderPos(double x, double y, double z) {
//        return new Double[]{x+getRenderX(), y+getRenderY(), z+getRenderZ()};
//    }

//    private Double getRenderX() {
//        return ((AccessorRenderManager) mc.getRenderManager()).getRenderX();
//    }
//
//    private Double getRenderY() {
//        return ((AccessorRenderManager) mc.getRenderManager()).getRenderY();
//    }
//
//    private Double getRenderZ() {
//        return ((AccessorRenderManager) mc.getRenderManager()).getRenderZ();
//    }

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
