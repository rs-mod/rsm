package com.ricedotwho.rsm.ui.clickgui.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
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
    private final int width = 850;
    @Getter
    private final int height = 600;

    public boolean key(char typedChar, int keyCode) {
        boolean value = false;
        for (CategoryComponent category : renderer.categoryList) {
            if (category.key(typedChar, keyCode)) value = true;
        }

        for (ModuleComponent module : renderer.moduleList) {
            if (module.key(typedChar, keyCode)) value = true;
        }
        return value;
    }

    // schizo render
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        NVGUtils.drawRect(getPosition().x, getPosition().y, width, height, 2, FatalityColours.BACKGROUND);

        float x = (float) getPosition().x, y = (float) (getPosition().y + 50), w = width, h = 525f;
        NVGUtils.drawRect(x, y, w, h, FatalityColours.PANEL);

        NVGUtils.pushScissor(x, y, (int) w, (int) h);

        nvgBeginPath(NVGUtils.getVg());

        for (int i = 0; i < w + h; i += 2) {
            nvgMoveTo(NVGUtils.getVg(), x + i, y);
            nvgLineTo(NVGUtils.getVg(), x, y + i);
        }

        nvgStrokeWidth(NVGUtils.getVg(), 1f);
        NVGUtils.colour(FatalityColours.PANEL_LINES);
        nvgStrokeColor(NVGUtils.getVg(), NVGUtils.getNvgColor());
        nvgStroke(NVGUtils.getVg());

        NVGUtils.popScissor();

        NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + 50),
                (float) getPosition().x + width, (float) (getPosition().y + 50), 1f, FatalityColours.LINE);
        NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + 500 - 23.5f),
                (float) getPosition().x + width, (float) (getPosition().y + 500 - 23.5f), 1f, FatalityColours.LINE);

        progress += 0.01f * partialTicks;
        if (progress >= 1.0f) {
            progress = 0.0f;
            reversing = !reversing;
        }

        float interpX = lerp((float) (getPosition().x + 16f), (float) (getPosition().x + 20f), reversing ? 1 - progress : progress);
        float interpY = lerp((float) (getPosition().y + 21f), (float) (getPosition().y + 19f), reversing ? 1 - progress : progress);

        String name = "RSM";

        NVGUtils.drawText(name, interpX, interpY, 18, FatalityColours.NAME2, NVGUtils.JOSEFIN);

        NVGUtils.drawText(name, lerp((float) (getPosition().x + 19f), (float) (getPosition().x + 16f), reversing ? 1 - progress : progress),
                lerp((float) (getPosition().y + 19f), (float) (getPosition().y + 21f), reversing ? 1 - progress : progress), 18, FatalityColours.NAME3, NVGUtils.JOSEFIN);

        NVGUtils.drawText(name, (float) (getPosition().x + 20f), (float) (getPosition().y + 20), 18, FatalityColours.NAME1, NVGUtils.JOSEFIN);

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 40);
        }

        int a = (int) (getPosition().x + (width - totalWidth) / 2f + 20);
        for (Category cat : Category.values()) {

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) (a - 10f), (int) (getPosition().y + 12f), (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 39f), (int) 25f, false);

            boolean isSelected = (selected == cat);
            if (isSelected) {
                if (lastCategory != cat) {
                    lastCategory = cat;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                Colour textColor = ColorUtils.interpolateColorC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, progress);

                float finalWidth = (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 30f);
                NVGUtils.drawRect(a - 10f, getPosition().y + 12f, finalWidth * progress, 25f, 3f, FatalityColours.SELECTED_BACKGROUND);

                NVGUtils.drawText(cat.name(), a + 14, (float) (getPosition().y + 25f), 11, textColor, NVGUtils.JOSEFIN);
            } else {
                NVGUtils.drawText(cat.name(), (float) (a + 14), (float) (getPosition().y + 25f), 11, hovered ? FatalityColours.SELECTED_TEXT : FatalityColours.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
            }
            a += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 40);
            int b = (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 10);
            NVGUtils.renderImage(cat.getImage(), (a - b) - 38, (float) getPosition().y + 17.5f, 20, 20, 255);
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
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, NVGUtils.JOSEFIN) + 40);
        }

        int a = (int) (getPosition().x + (width - totalWidth) / 2f + 20);
        String last;

        for (Category category : Category.values()) {
            String categoryName = category.name();

            renderer.maskList.add(new Mask((int) (a - 10f), (int) (getPosition().y + 12f), (int) (NVGUtils.getTextWidth(category.name(), 11, NVGUtils.JOSEFIN) + 39f), (int) 25f));
            if (NVGUtils.isHovering(mouseX, mouseY, (int) (a - 10f), (int) (getPosition().y + 12f), (int) (NVGUtils.getTextWidth(categoryName, 11, NVGUtils.JOSEFIN) + 39f), (int) 25f, false) && mouseButton == 0) {
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
            a += (int) (NVGUtils.getTextHeight(last, 11, NVGUtils.JOSEFIN) + 40);
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