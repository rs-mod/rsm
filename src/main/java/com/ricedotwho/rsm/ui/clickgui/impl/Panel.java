package com.ricedotwho.rsm.ui.clickgui.impl;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;

import java.util.Objects;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeWidth;

public class Panel implements Accessor {

    @Getter
    RSMConfig renderer;

    public Panel(RSMConfig renderer) {
        this.renderer = renderer;
    }

    private float progress = 0.0f;
    private boolean reversing = false;
    private Category selected = Category.MOVEMENT;
    private Category lastCategory = Category.OTHER;
    private final StopWatch stopWatch = new StopWatch();
    @Getter
    private final int width = 425;
    @Getter
    private final int height = 300;

    public boolean key(char typedChar, int keyCode) {
        boolean value = false;
        for (CategoryComponent category : renderer.categoryList) {
            if(category.key(typedChar, keyCode)) value = true;
        }

        for (ModuleComponent module : renderer.moduleList) {
            if(module.key(typedChar, keyCode)) value = true;
        }
        return value;
    }

    // schizo render
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        NVGUtils.drawRect(getPosition().x, getPosition().y, width, height, 2, FatalityColors.BACKGROUND);

        float x = (float) getPosition().x, y = (float) (getPosition().y + 25), w = 425, h = 262.5f;
        NVGUtils.drawRect(x, y, w, h, FatalityColors.PANEL);

        Window window = mc.getWindow();
        int f = window.getGuiScale(), sx = (int) (x * f), sy = (int) ((window.getGuiScaledHeight() - (y + h)) * f);

        NVGUtils.pushScissor(sx, sy, (int) (w * f), (int) (h * f));

        nvgBeginPath(NVGUtils.getVg());

        for (int i = 0; i < w + h; i += 2) {
            nvgMoveTo(NVGUtils.getVg(), x + i, y);
            nvgLineTo(NVGUtils.getVg(), x, y + i);
        }

        nvgStrokeWidth(NVGUtils.getVg(), 1.5f);
        NVGUtils.colour(FatalityColors.PANEL_LINES);
        nvgStrokeColor(NVGUtils.getVg(), NVGUtils.getNvgColor());
        nvgStroke(NVGUtils.getVg());

        NVGUtils.popScissor();

        NVGUtils.drawRect((float) getPosition().x, (float) (getPosition().y + 25), 425, 0.5f, FatalityColors.LINE);
        NVGUtils.drawRect((float) getPosition().x, (float) (getPosition().y + 300 - 12.5f), 425, 0.5f, FatalityColors.LINE);

        progress += 0.01f * partialTicks;
        if (progress >= 1.0f) {
            progress = 0.0f;
            reversing = !reversing;
        }

        float interpX = lerp((float) (getPosition().x + 9f), (float) (getPosition().x + 11f), reversing ? 1 - progress : progress);
        float interpY = lerp((float) (getPosition().y + 11.5f), (float) (getPosition().y + 10.5f), reversing ? 1 - progress : progress);

        String name = "RSM";

        NVGUtils.drawText(name, interpX, interpY, 18, FatalityColors.NAME2, NVGUtils.JOSEFIN);

        NVGUtils.drawText(name, lerp((float) (getPosition().x + 10.5f), (float) (getPosition().x + 9f), reversing ? 1 - progress : progress),
                lerp((float) (getPosition().y + 10.5f), (float) (getPosition().y + 11.5f), reversing ? 1 - progress : progress), 18, FatalityColors.NAME3, NVGUtils.JOSEFIN);

        NVGUtils.drawText(name, (float) (getPosition().x + 10f), (float) (getPosition().y + 11), 18, FatalityColors.NAME1, NVGUtils.JOSEFIN);

//        NVGUtils.drawCircle(getPosition().x + 425 - 25.5f, getPosition().y + 11.5f / 2, 6.5, FatalityColors.DEFAULT_AVATAR);
//        Fonts.getJoseFin(13).drawString(RSM.getInstance().getUser(), (float) (getPosition().x + 425 - 29.5f - Fonts.getJoseFin(12).getWidth(RS.getInstance().getUser())), (float) (getPosition().y + 8f), -1);
//        String expirationText = RSM.getInstance().getFormattedExpiration();
//        Fonts.getJoseFin(13).drawString(expirationText, (float) (getPosition().x + 425 - 29.5f - Fonts.getJoseFin(12).getWidth(expirationText)), (float) (getPosition().y + 16.0), FatalityColors.SELECTED.getRGB());
        //Fonts.getJoseFin(13).drawString("expires:", (float) (getPosition().x + 425 - 29.5f - Fonts.getJoseFin(12).getWidth("expires: " + expirationText) - 1), (float) (getPosition().y + 15.5), FatalityColors.UNSELECTED_TEXT.getRGB());

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 20);
        }

        int a = (int) (getPosition().x + (425 - totalWidth) / 2f + 10);
        for (Category cat : Category.values()) {

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) (a - 5f), (int) (getPosition().y + 6f), (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 19.5f), (int) 13.5f);

            boolean isSelected = (selected == cat);
            if (isSelected) {
                if (lastCategory != cat) {
                    lastCategory = cat;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                Colour textColor = ColorUtils.interpolateColorC(FatalityColors.UNSELECTED_TEXT, FatalityColors.SELECTED_TEXT, progress);

                float finalWidth = (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 15);
                NVGUtils.drawRect(a - 5f, getPosition().y + 6f, finalWidth * progress, 13.5f, 1.5f, FatalityColors.SELECTED_BACKGROUND);

                NVGUtils.drawText(cat.name(), a + 7, (float) (getPosition().y + 12.5f), 11, textColor, NVGUtils.JOSEFIN);
            } else {
                NVGUtils.drawText(cat.name(), (float) (a + 7), (float) (getPosition().y + 12.5f), 11, hovered ? FatalityColors.SELECTED_TEXT : FatalityColors.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
            }
            a += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 20);
            int b = (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 5);
            NVGUtils.renderImage(cat.getImage(), (a - b) - 19, (float) getPosition().y + 8f, 10, 10, 255);
            if (cat == selected) {
                renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(cat))
                        .findFirst().orElse(null).render(gfx, mouseX, mouseY, partialTicks);
            }
        }
        lastCategory = selected;
    }

    public void release(double mouseX, double mouseY, int mouseButton) {
        for (Category category : Category.values()) {
            if (category == selected) {
                renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst().orElse(null).release(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        renderer.maskList.clear();

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 20);
        }

        int a = (int) (getPosition().x + (double) (425 - totalWidth) / 2 + 10);
        String last;
        for (Category category : Category.values()) {
            String categoryName = category.name();
            renderer.maskList.add(new Mask((int) (a - 5f), (int) (getPosition().y + 6f), (int) (NVGUtils.getTextWidth(category.name(), 11, NVGUtils.JOSEFIN) + 19.5f), (int) 13.5f));
            if (NVGUtils.isHovering(mouseX, mouseY, (int) (a - 5f), (int) (getPosition().y + 6f), (int) (NVGUtils.getTextWidth(category.name(), 11, NVGUtils.JOSEFIN) + 19.5f), (int) 13.5f) && mouseButton == 0) {
                selected = category;
                Objects.requireNonNull(renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst().orElse(null)).selected =
                        Objects.requireNonNull(renderer.categoryList.stream()
                                .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                                .findFirst().orElse(null)).lastSelected = null;
            }
            if (category == selected) {
                Objects.requireNonNull(renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst().orElse(null)).click(mouseX, mouseY, mouseButton);
            }
            last = categoryName;
            a += (int) (NVGUtils.getTextHeight(last, 11, NVGUtils.JOSEFIN) + 20);
        }
    }
    public void onGuiClosed(){
        Objects.requireNonNull(renderer.categoryList.stream()
                .filter(categoryComponent -> categoryComponent.getCategory().equals(selected))
                .findFirst().orElse(null)).onGuiClosed();
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public Vector2d getPosition() {
        return renderer.getPosition();
    }
}