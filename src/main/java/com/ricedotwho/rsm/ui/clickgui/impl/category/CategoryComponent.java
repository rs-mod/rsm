package com.ricedotwho.rsm.ui.clickgui.impl.category;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import com.ricedotwho.rsm.utils.render.font.Fonts;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import com.ricedotwho.rsm.module.Module;

@Getter
public class CategoryComponent {

    private final RSMConfig renderer;
    private final Category category;
    public ModuleComponent selected;
    public ModuleComponent lastSelected;
    private final StopWatch stopWatch = new StopWatch();

    public CategoryComponent(RSMConfig renderer, Category category) {
        this.renderer = renderer;
        this.category = category;

    }

    public boolean key(char typedChar, int keyCode) {
        boolean value = false;
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                if(module.key(typedChar, keyCode)) value = true;
            }
        }
        return value;
    }
    public void onGuiClosed() {
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                module.getModule().onGuiClosed();
            }
        }
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (selected == null) {
            selected = renderer.moduleList.stream()
                    .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                    .findFirst().orElse(null);
        }
        float a = (float) (getPosition().x + 8);


        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            boolean isSelected = (selected == moduleComponent);
            Module module = moduleComponent.getModule();
            float h = Fonts.getJoseFin(12).getHeight();
            boolean isHovered = NVGUtils.isHovering(mouseX, mouseY,
                    (int) (a - 1),
                    (int) ((int) (getPosition().y + 37.5F) - h),
                    (int) (Fonts.getJoseFin(12).getWidth(module.getName()) + 2),
                    (int) h * 2 + 5);

            if (isSelected) {
                if (lastSelected != moduleComponent) {
                    lastSelected = moduleComponent;
                    stopWatch.reset();
                }
            }

            float progress = Math.min(1.0f, stopWatch.getElapsedTime() / 150.0f);


            Colour textColor = ColorUtils.interpolateColorC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, isHovered || isSelected ? progress : 0.0f);

            float finalWidth = (Fonts.getJoseFin(12).getWidth(module.getName()) + 2) * (isSelected ? progress : 1.0f);

            Fonts.getJoseFin(12).drawString(module.getName(), a, (float) (getPosition().y + 37.5F), textColor);

            if (isSelected) {
                NVGUtils.drawRect(a - 1, (float) (getPosition().y + 43), finalWidth, 1, FatalityColours.SELECTED);
                moduleComponent.render(gfx, mouseX, mouseY, partialTicks);
            }

            a += Fonts.getJoseFin(12).getWidth(module.getName()) + 7.5f;
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (getPosition().x + 8);

        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            Module module = moduleComponent.getModule();
            float h = Fonts.getJoseFin(12).getHeight();
            float w = Fonts.getJoseFin(12).getWidth(module.getName()) + 2;
            renderer.maskList.add(new Mask((int) (a - 1), (int) ((int) (getPosition().y + 37.5F) - h), (int) w, (int) h * 2 + 5));
            if (NVGUtils.isHovering(mouseX, mouseY, (int) (a - 1), (int) ((int) (getPosition().y + 37.5F) - h), (int) w, (int) h * 2 + 5)
                    && mouseButton == 0) {
                selected = moduleComponent;
            }
            if (selected == moduleComponent) {
                moduleComponent.click(mouseX, mouseY, mouseButton);
            }
            a += Fonts.getJoseFin(12).getWidth(module.getName()) + 7.5f;
        }
    }

    public void release(double mouseX, double mouseY, int mouseButton) {
        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            if (selected == moduleComponent) {
                moduleComponent.release(mouseX, mouseY, mouseButton);
            }
        }
    }

    public Vector2d getPosition() {
        return renderer.getPosition();
    }

}
