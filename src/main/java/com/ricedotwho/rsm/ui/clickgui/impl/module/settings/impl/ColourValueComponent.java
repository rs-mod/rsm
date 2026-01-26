package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Getter
public class ColourValueComponent extends ValueComponent<ColourSetting> {
    private boolean expanded = false;

    private final int boxSize = 50;
    private final int hueStripWidth = 5;

    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private final float baseHeight = 10.5f;

    public ColourValueComponent(ColourSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    //todo: add a way to make it chroma

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        float sbX = posX + 120 + 12;
        float sbY = posY - baseHeight / 2f;

        float width = 25;

        // todo: fade
        Color color = RenderUtils.isHovering(mouseX, mouseY, (int) sbX, (int) sbY, (int) width, (int) baseHeight) ? setting.getValue().toJavaColor().brighter() : setting.getValue().toJavaColor();
        RenderUtils.drawRoundedRect(gfx, sbX, sbY, width, baseHeight, 1, color);

        if (!expanded) return;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        float bgwidth = boxSize + (hueStripWidth * 2) + 12;
        float boxX = (sbX + width + 1) - bgwidth;

        float hueX = boxX + boxSize + 5;
        float alphaX = hueX + hueStripWidth + 5;
        float boxY = sbY + baseHeight + 2;

        RenderUtils.drawRoundedRect(gfx, boxX - 1, boxY - 2, bgwidth, 54, 1, FatalityColors.PANEL);

        renderOverlay(mouseX, mouseY);

        drawRounded(gfx, () -> drawSB(boxX, boxY), boxX, boxY, boxSize, boxSize, 1.5);
        drawRounded(gfx, () -> drawHue(hueX, boxY), hueX, boxY, hueStripWidth, boxSize, 1.5);
        drawRounded(gfx, () -> drawAlpha2(alphaX, boxY, 1.5), alphaX, boxY, hueStripWidth, boxSize, 1.5);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    private void drawRounded(GuiGraphics gfx, Runnable run, double x, double y, double width, double height, double radius) {
        // holy frick
        //RenderUtil.drawRoundedRect(x - (t / 2), y - (t / 2), width + t, height + t, radius, Color.BLACK);

        GL11.glPushMatrix();

        boolean stencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);

        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        GL11.glColorMask(false, false, false, false);

        RenderUtils.drawRoundedRect(gfx, x, y, width, height, radius, FatalityColors.SELECTED_BACKGROUND);

        GL11.glColorMask(true, true, true, true);


        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        run.run();

        if(!stencil) GL11.glDisable(GL11.GL_STENCIL_TEST);

        GL11.glPopMatrix();
    }
    private void drawSB(float sbX, float sbY) {
        float hue = setting.getValue().getHue() / 360f;

        for (int x = 0; x < boxSize; x++) {
            for (int y = 0; y < boxSize; y++) {
                float sat = x / (float) boxSize;
                float bright = 1.0f - (y / (float) boxSize);
                int color = Color.HSBtoRGB(hue, sat, bright);
                RenderUtils.drawRect(sbX + x, sbY + y, sbX + x + 1, sbY + y + 1, color);
            }
        }

        short[] hsba = setting.getValue().getHSBA();
        float sat = hsba[1] / 100f;
        float bright = hsba[2] / 100f;

        // box position dot
        float selX = sbX + sat * boxSize;
        float selY = sbY + (1 - bright) * boxSize;
        float radius = 1;
        RenderUtils.drawCircle(selX - radius, selY - radius, radius, Color.WHITE);
    }
    private void drawHue(float hueX, float sbY) {
        for (int y = 0; y < boxSize; y++) {
            float h = y / (float) boxSize;
            int color = Color.HSBtoRGB(h, 1.0f, 1.0f);
            RenderUtils.drawRect(hueX, sbY + y, hueX + hueStripWidth, sbY + y + 1, color);
        }
        short[] hsba = setting.getValue().getHSBA();
        float hueIndicator = hsba[0] / 360f;
        // hue slider line
        float hueMarkerY = sbY + hueIndicator * boxSize;
        RenderUtils.drawRect(hueX, hueMarkerY - 1, hueX + hueStripWidth, hueMarkerY + 1, 0xFFFFFFFF);
    }
    private void drawAlpha(float x, float sbY) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (int y = 0; y < boxSize; y++) {
            float a = (y / (float) boxSize) * 255f;
            Colour color = setting.getValue().clone();
            color.setAlpha((int) a);
            RenderUtils.drawRect(x, sbY + y, x + hueStripWidth, sbY + y + 1, color.getRGB());
        }
        GL11.glDisable(GL11.GL_BLEND);

        short[] hsba = setting.getValue().getHSBA();
        float alphaIndicator = hsba[3] / 255f;
        float alphaMarkerY = sbY + alphaIndicator * boxSize;
        RenderUtils.drawRect(x, alphaMarkerY - 1, x + hueStripWidth, alphaMarkerY + 1, 0xFFFFFFFF);
    }
    private void drawAlpha2(double alphaX, double boxY, double radius) {
        Colour c1 = setting.getValue().clone();
        Colour c2 = setting.getValue().clone();
        c1.setAlpha(255);
        c2.setAlpha(0);
        RenderUtils.drawRoundedGradientRect(alphaX, boxY, hueStripWidth, boxSize, radius, c2.toJavaColor(), c1.toJavaColor(), c2.toJavaColor(), c1.toJavaColor());

        short[] hsba = setting.getValue().getHSBA();
        float alphaIndicator = hsba[3] / 255f;
        float alphaMarkerY = (float) (boxY + alphaIndicator * boxSize);
        RenderUtils.drawRect((float) alphaX, alphaMarkerY - 1, (float) (alphaX + hueStripWidth), alphaMarkerY + 1, 0xFFFFFFFF);
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        float y = getPosition().y + baseHeight / 2f + 1f;

        float sbX = getPosition().x + 165 + 12 + 1;
        float sbY = getPosition().y - baseHeight / 2f + 0;
        float width = 25;
        float bgwidth = boxSize + (hueStripWidth * 2) + 12; // 122
        float expandX = (sbX + width + 1) - bgwidth;


        if (mouseButton == 1 || mouseButton == 0) {
            if (RenderUtils.isHovering(mouseX, mouseY, (int) expandX, (int) sbY, (int) width, (int) baseHeight)) {
                expanded = !expanded;
                return;
            }
        }
        if (mouseButton != 0 || !expanded) return;

        float boxX = (expandX + width + 1) - bgwidth;
        float hueX = boxX + boxSize + 5;
        float alphaX = hueX + hueStripWidth + 5;
        float boxY = sbY + baseHeight + 2;

        float relX = (float) (mouseX - (getPosition().x + 165 + 12 + width));
        float relY = (float) (mouseY - y);

        if (RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, boxSize, boxSize)) {
            updateSB(relX, relY);
            draggingSB = true;
        } else if (RenderUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, hueStripWidth, boxSize)) {
            updateHue(relY);
            draggingHue = true;
        } else if(RenderUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, hueStripWidth, boxSize)) {
            updateAlpha(relY);
            draggingAlpha = true;
        }
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    public void renderOverlay(double mouseX, double mouseY) {
        float y = getPosition().y + baseHeight / 2f + 1f;
        float x = getPosition().x + 85;

        if (draggingSB) {
            updateSB((float) (mouseX - x), (float) (mouseY - y));
        }
        if (draggingHue) {
            updateHue((float) (mouseY - y));
        }
        if (draggingAlpha) {
            updateAlpha((float) (mouseY - y));
        }
    }

    private void updateSB(float relX, float relY) {
        float sat = Math.max(0, Math.min(1, relX / boxSize));
        float bright = Math.max(0, Math.min(1, 1 - relY / boxSize));
        Colour color = setting.getValue();
        color.setHSBA(1, (int) (sat * 100));
        color.setHSBA(2, (int) (bright * 100));
    }

    private void updateHue(float relY) {
        float hue = Math.max(0, Math.min(1, relY / boxSize));
        setting.getValue().setHSBA(0, (int) (hue * 360));
    }
    private void updateAlpha(float relY) {
        float alpha = Math.max(0, Math.min(1, relY / boxSize));
        setting.getValue().setAlpha((int) (alpha * 255f));
    }

    @Override
    public int getHeight() {
        return (int) (baseHeight + (expanded ? boxSize : 0));
    }
}
