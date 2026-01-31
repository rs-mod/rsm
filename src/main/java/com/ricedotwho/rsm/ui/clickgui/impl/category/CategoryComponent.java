package com.ricedotwho.rsm.ui.clickgui.impl.category;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.render.render2d.ColorUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
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

    public boolean charTyped(char typedChar, int keyCode) {
        boolean value = false;
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                if(module.charTyped(typedChar, keyCode)) value = true;
            }
        }
        return value;
    }

    public boolean keyTyped(KeyEvent input) {
        boolean value = false;
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                if(module.keyTyped(input)) value = true;
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

    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        if (selected == null) {
            selected = renderer.moduleList.stream()
                    .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                    .findFirst().orElse(null);
        }
        float a = (float) (getPosition().x + 16);


        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            boolean isSelected = (selected == moduleComponent);
            Module module = moduleComponent.getModule();
            float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);
            boolean isHovered = NVGUtils.isHovering(mouseX, mouseY,
                    (int) (a - 2),
                    (int) ((int) (getPosition().y + 75F) - h),
                    (int) NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 4,
                    (int) h * 2 + 10);

            if (isSelected) {
                if (lastSelected != moduleComponent) {
                    lastSelected = moduleComponent;
                    stopWatch.reset();
                }
            }

            float progress = Math.min(1.0f, stopWatch.getElapsedTime() / 150.0f);


            Colour textColor = ColorUtils.interpolateColorC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, isHovered || isSelected ? progress : 0.0f);

            float finalWidth = (NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) * 1.05f) * (isSelected ? progress : 1.0f);

            NVGUtils.drawText(module.getName(), a, (float) (getPosition().y + 75F), 12, textColor, NVGUtils.JOSEFIN);

            if (isSelected) {
                NVGUtils.drawRect(a - 2, (float) (getPosition().y + 90), finalWidth, 2, FatalityColours.SELECTED);
                moduleComponent.render(gfx, mouseX, mouseY, partialTicks);
            }

            a += NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 15f;
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (getPosition().x + 8);

        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            Module module = moduleComponent.getModule();
            float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);
            float w = NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN);
            renderer.maskList.add(new Mask((int) (a - 2), (int) ((int) (getPosition().y + 75F) - h), (int) w, (int) h * 2 + 10));
            if (NVGUtils.isHovering(mouseX, mouseY, (int) (a - 2), (int) ((int) (getPosition().y + 75F) - h), (int) w, (int) h * 2 + 10)
                    && mouseButton == 0) {
                selected = moduleComponent;
            }
            if (selected == moduleComponent) {
                moduleComponent.click(mouseX, mouseY, mouseButton);
            }
            a += NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 15f;
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