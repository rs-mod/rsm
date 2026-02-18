package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.render.render2d.Gradient;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

@Getter
public class ColourValueComponent extends ValueComponent<ColourSetting> {
    private boolean expanded = false;

    private final int boxSize = 100;
    private final int hueStripWidth = 10;

    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private final float baseHeight = 21f;

    public ColourValueComponent(ColourSetting setting, ModuleBase parent) {
        super(setting, parent);
    }

    //todo: add a way to make it chroma

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        float sbX = posX + 240 + 24;
        float sbY = posY - baseHeight / 2f;

        float width = 50;

        // todo: fade
        Colour colour = NVGUtils.isHovering(mouseX, mouseY, (int) sbX, (int) sbY, (int) width, (int) baseHeight) ? setting.getValue().brighter() : setting.getValue();
        NVGUtils.drawRect(sbX, sbY, width, baseHeight, 1, colour);

        if (!expanded) return;
        float bgwidth = boxSize + (hueStripWidth * 2) + 24;
        float boxX = (sbX + width + 2) - bgwidth;

        float hueX = boxX + boxSize + 10;
        float alphaX = hueX + hueStripWidth + 10;
        float boxY = sbY + baseHeight + 4;

        NVGUtils.drawRect(boxX - 2, boxY - 4, bgwidth, 108, 2, FatalityColours.PANEL);

        renderOverlay(mouseX, mouseY);

        drawRounded(gfx, () -> drawSB(boxX, boxY), boxX, boxY, boxSize, boxSize, 1.5);
        drawRounded(gfx, () -> drawHue(hueX, boxY), hueX, boxY, hueStripWidth, boxSize, 1.5);
        drawRounded(gfx, () -> drawAlpha2(alphaX, boxY, 1.5f), alphaX, boxY, hueStripWidth, boxSize, 1.5);
    }
    private void drawRounded(GuiGraphics gfx, Runnable run, double x, double y, double width, double height, double radius) {
        NVGUtils.push();

        // todo: figure out how to render a rounded rect mask

        //NVGUtils.drawRect(x, y, width, height, radius, FatalityColors.SELECTED_BACKGROUND);

        run.run();

        NVGUtils.pop();
    }
    private void drawSB(float sbX, float sbY) {
        float hue = setting.getValue().getHue() / 360f;

        for (int x = 0; x < boxSize; x++) {
            for (int y = 0; y < boxSize; y++) {
                float sat = x / (float) boxSize;
                float bright = 1.0f - (y / (float) boxSize);
                int color = Color.HSBtoRGB(hue, sat, bright);
                NVGUtils.drawRect(sbX + x, sbY + y, sbX + x + 1, sbY + y + 1, new Colour(color));
            }
        }

        short[] hsba = setting.getValue().getHSBA();
        float sat = hsba[1] / 100f;
        float bright = hsba[2] / 100f;

        // box position dot
        float selX = sbX + sat * boxSize;
        float selY = sbY + (1 - bright) * boxSize;
        float radius = 1;
        NVGUtils.drawCircle(selX - radius, selY - radius, radius, new Colour(Color.WHITE));
    }
    private void drawHue(float hueX, float sbY) {
        for (int y = 0; y < boxSize; y++) {
            float h = y / (float) boxSize;
            int color = Color.HSBtoRGB(h, 1.0f, 1.0f);
            NVGUtils.drawRect(hueX, sbY + y, hueX + hueStripWidth, sbY + y + 1, new Colour(color));
        }
        short[] hsba = setting.getValue().getHSBA();
        float hueIndicator = hsba[0] / 360f;
        // hue slider line
        float hueMarkerY = sbY + hueIndicator * boxSize;
        NVGUtils.drawRect(hueX, hueMarkerY - 1, hueX + hueStripWidth, hueMarkerY + 1, new Colour(0xFFFFFFFF));
    }
    private void drawAlpha(float x, float sbY) {
        for (int y = 0; y < boxSize; y++) {
            float a = (y / (float) boxSize) * 255f;
            Colour color = setting.getValue().clone();
            color.setAlpha((int) a);
            NVGUtils.drawRect(x, sbY + y, x + hueStripWidth, sbY + y + 1, color);
        }

        short[] hsba = setting.getValue().getHSBA();
        float alphaIndicator = hsba[3] / 255f;
        float alphaMarkerY = sbY + alphaIndicator * boxSize;
        NVGUtils.drawRect(x, alphaMarkerY - 1, x + hueStripWidth, alphaMarkerY + 1, new Colour(0xFFFFFFFF));
    }
    private void drawAlpha2(float alphaX, float boxY, float radius) {
        Colour c1 = setting.getValue().clone();
        Colour c2 = setting.getValue().clone();
        c1.setAlpha(255);
        c2.setAlpha(0);
        NVGUtils.drawGradientRect(alphaX, boxY, hueStripWidth, boxSize, radius, c2, c1, Gradient.TopToBottom);

        short[] hsba = setting.getValue().getHSBA();
        float alphaIndicator = hsba[3] / 255f;
        float alphaMarkerY = (float) (boxY + alphaIndicator * boxSize);
        NVGUtils.drawRect((float) alphaX, alphaMarkerY - 1, (float) (alphaX + hueStripWidth), alphaMarkerY + 1, new Colour(0xFFFFFFFF));
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        float y = getPosition().y + baseHeight / 2f + 2f;

        float sbX = getPosition().x + 330 + 24 + 2;
        float sbY = getPosition().y - baseHeight / 2f + 0;
        float width = 50;
        float bgwidth = boxSize + (hueStripWidth * 2) + 24; // 122
        float expandX = (sbX + width + 2) - bgwidth;


        if (mouseButton == 1 || mouseButton == 0) {
            if (NVGUtils.isHovering(mouseX, mouseY, (int) expandX, (int) sbY, (int) width, (int) baseHeight)) {
                expanded = !expanded;
                consumeClick();
                return;
            }
        }
        if (mouseButton != 0 || !expanded) return;

        float boxX = (expandX + width + 1) - bgwidth;
        float hueX = boxX + boxSize + 10;
        float alphaX = hueX + hueStripWidth + 10;
        float boxY = sbY + baseHeight + 4;

        float relX = (float) (mouseX - (getPosition().x + 330 + 24 + width));
        float relY = (float) (mouseY - y);

        if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, boxSize, boxSize)) {
            updateSB(relX, relY);
            draggingSB = true;
            consumeClick();
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, hueStripWidth, boxSize)) {
            updateHue(relY);
            draggingHue = true;
            consumeClick();
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, hueStripWidth, boxSize)) {
            updateAlpha(relY);
            draggingAlpha = true;
            consumeClick();
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    public void renderOverlay(double mouseX, double mouseY) {
        float y = getPosition().y + baseHeight / 2f + 2f;
        float x = getPosition().x + 170;

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