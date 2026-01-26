package com.ricedotwho.rsm.ui.clickgui.impl;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

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

        RenderUtils.drawRoundedRect(getPosition().x, getPosition().y, width, 27, 2, FatalityColors.PANEL);

        float x1 = (float) getPosition().x, y1 = (float) (getPosition().y + 25), w = 425, h1 = 25f;
        RenderUtils.drawRect(x1, y1, w, h1, FatalityColors.HEADER_BACKGROUND);

        float x = (float) getPosition().x, y = (float) (getPosition().y + 50), h = 237.5f;
        RenderUtils.drawRect(x, y, w, h, FatalityColors.BACKGROUND);

        RenderUtils.drawRect(getPosition().x, getPosition().y + 25, 425, 0.5, FatalityColors.LINE);

        Window window = mc.getWindow();
        int f = window.getGuiScale(), sx = (int) (x * f), sy = (int) ((window.getGuiScaledHeight() - (y + h)) * f);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(sx, sy, (int) (w * f), (int) (h * f));

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int c = FatalityColors.PANEL_LINES.getRGB();
        GL11.glColor4f((c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f, (c >> 24 & 255) / 255f);

        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < w + h; i += 2) {
            GL11.glVertex2f(x + i, y);
            GL11.glVertex2f(x, y + i);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        progress += 0.01f * partialTicks;
        if (progress >= 1.0f) {
            progress = 0.0f;
            reversing = !reversing;
        }

        // bring back lerp?
//        float interpX = lerp((float) (getPosition().x + 9f), (float) (getPosition().x + 11f), reversing ? 1 - progress : progress);
//        float interpY = lerp((float) (getPosition().y + 11.5f), (float) (getPosition().y + 10.5f), reversing ? 1 - progress : progress);

        String name = "RSM";

//        Fonts.getJoseFinBold(18).drawString(name, interpX, interpY, FatalityColors.NAME2.getRGB());
//        Fonts.getJoseFinBold(18).drawString(name, lerp((float) (getPosition().x + 10.5f), (float) (getPosition().x + 9f), reversing ? 1 - progress : progress),
//                lerp((float) (getPosition().y + 10.5f), (float) (getPosition().y + 11.5f), reversing ? 1 - progress : progress),
//                FatalityColors.NAME3.getRGB());

        Fonts.getJoseFinBold(18).drawString(name, (float) (getPosition().x + 10f), (float) (getPosition().y + 11), FatalityColors.NAME1.getRGB());

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (Fonts.getJoseFin(11).getWidth(cat.name()) + 20);
        }

        int a = (int) (getPosition().x + (425 - totalWidth) / 2 + 10);
        for (Category cat : Category.values()) {
            boolean isSelected = (selected == cat);

            boolean hovered = RenderUtils.isHovering(mouseX, mouseY, (int) (a - 5f), (int) (getPosition().y + 6f), (int) (Fonts.getJoseFin(11).getWidth(cat.getName()) + 19.5f), (int) 13.5f);

            if (isSelected) {
                if (lastCategory != cat) {
                    lastCategory = cat;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                int textColor = ColorUtils.interpolateInt(FatalityColors.UNSELECTED_TEXT.getRGB(), FatalityColors.SELECTED_TEXT.getRGB(), progress);

                float finalWidth = (Fonts.getJoseFin(11).getWidth(cat.name()) + 15);
                RenderUtils.drawRoundedRect(a - 5f, getPosition().y + 6f, finalWidth * progress, 13.5f, 1.5f, FatalityColors.SELECTED_BACKGROUND);
                Fonts.getJoseFin(11).drawString(cat.name(), a + 7, (float) (getPosition().y + 12.5f), textColor);
            } else {
                Fonts.getJoseFin(11).drawString(cat.name(), (float) (a + 7), (float) (getPosition().y + 12.5f), hovered ? FatalityColors.SELECTED_TEXT.getRGB() : FatalityColors.UNSELECTED_TEXT.getRGB());
            }
            a += (int) (Fonts.getJoseFin(11).getWidth(cat.name()) + 20);
            int b = (int) (Fonts.getJoseFin(11).getWidth(cat.name()) + 5);
            RenderUtils.drawImage(gfx, cat.getIcon(), (a - b) - 19, (float) getPosition().y + 8f, 10, 10, FatalityColors.ICON);
            if (cat == selected) {
                renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(cat))
                        .findFirst().orElse(null).render(mouseX, mouseY, partialTicks);
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
            totalWidth += (int) (Fonts.getJoseFin(11).getWidth(cat.name()) + 20);
        }

        int a = (int) (getPosition().x + (double) (425 - totalWidth) / 2 + 10);
        String last;
        for (Category category : Category.values()) {
            String categoryName = category.name();
            renderer.maskList.add(new Mask((int) (a - 5f), (int) (getPosition().y + 6f), (int) (Fonts.getJoseFin(11).getWidth(category.getName()) + 19.5f), (int) 13.5f));
            if (RenderUtils.isHovering(mouseX, mouseY, (int) (a - 5f), (int) (getPosition().y + 6f), (int) (Fonts.getJoseFin(11).getWidth(category.getName()) + 19.5f), (int) 13.5f) && mouseButton == 0) {
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
            a += (int) (Fonts.getJoseFin(11).getWidth(last) + 20);
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