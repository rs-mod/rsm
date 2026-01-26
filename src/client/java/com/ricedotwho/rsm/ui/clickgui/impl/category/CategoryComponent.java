package com.ricedotwho.rsm.ui.clickgui.impl.category;

import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
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

    public void render(int mouseX, int mouseY, float partialTicks) {
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
            boolean isHovered = RenderUtils.isHovering(mouseX, mouseY,
                    (int) (a - 1),
                    (int) ((int) (getPosition().y + 37.5F) - Fonts.getJoseFin(12).getHeight("G")),
                    (int) (Fonts.getJoseFin(12).getWidth(module.getName()) + 2),
                    (int) Fonts.getJoseFin(12).getHeight("G") * 2 + 5);

            if (isSelected) {
                if (lastSelected != moduleComponent) {
                    lastSelected = moduleComponent;
                    stopWatch.reset();
                }
            }

            float progress = Math.min(1.0f, stopWatch.getElapsedTime() / 150.0f);


            int textColor = ColorUtils.interpolateInt(FatalityColors.UNSELECTED_TEXT.getRGB(), FatalityColors.SELECTED_TEXT.getRGB(), isHovered || isSelected ? progress : 0.0f);

            float finalWidth = (Fonts.getJoseFin(12).getWidth(module.getName()) + 2) * (isSelected ? progress : 1.0f);

            Fonts.getJoseFin(12).drawString(module.getName(), a, (float) (getPosition().y + 37.5F), textColor);

            if (isSelected) {
                RenderUtils.drawRect(a - 1, getPosition().y + 43, finalWidth, 1, FatalityColors.SELECTED);
                moduleComponent.render(mouseX, mouseY, partialTicks);
            }


            a += Fonts.getJoseFin(12).getWidth(module.getName()) + 7.5f;
        }
    }

    public void click(double mouseX, double mouseY, float mouseButton) {
        float a = (float) (getPosition().x + 8);

        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            Module module = moduleComponent.getModule();
            renderer.maskList.add(new Mask((int) (a - 1), (int) ((int) (getPosition().y + 37.5F) - Fonts.getJoseFin(12).getHeight("G")), (int) (Fonts.getJoseFin(12).getWidth(module.getName()) + 2), (int) Fonts.getJoseFin(12).getHeight("G") * 2 + 5));
            if (RenderUtils.isHovering(mouseX, mouseY, (int) (a - 1), (int) ((int) (getPosition().y + 37.5F) - Fonts.getJoseFin(12).getHeight("G")), (int) (Fonts.getJoseFin(12).getWidth(module.getName()) + 2), (int) Fonts.getJoseFin(12).getHeight("G") * 2 + 5)
                    && mouseButton == 0) {
                selected = moduleComponent;
            }
            if (selected == moduleComponent) {
                moduleComponent.click(mouseX, mouseY, mouseButton);
            }
            a += Fonts.getJoseFin(12).getWidth(module.getName()) + 7.5f;
        }
    }

    public void release(double mouseX, double mouseY, float mouseButton) {
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
