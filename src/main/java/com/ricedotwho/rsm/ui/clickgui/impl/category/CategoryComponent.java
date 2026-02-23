package com.ricedotwho.rsm.ui.clickgui.impl.category;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.utils.render.render2d.ColourUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.joml.Vector2d;

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



        //float a = (float) (getPosition().x + 16);

        float a = (float) (renderer.getPosition().y + 112);

        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            boolean isSelected = (selected == moduleComponent);
            Module module = moduleComponent.getModule();
            //float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);

//            boolean isHovered = NVGUtils.isHovering(mouseX, mouseY,
//                    (int) (a - 2),
//                    (int) ((int) (getPosition().y + 75F) - h),
//                    (int) NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 4,
//                    (int) h * 2 + 10);

            boolean isHovered = NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 16), (int) (a - 8),
                    (int) (NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 10),
                    (int) (NVGUtils.getTextHeight(module.getName(), 12, NVGUtils.JOSEFIN) + 10));

            if (isSelected) {
                if (lastSelected != moduleComponent) {
                    lastSelected = moduleComponent;
                    stopWatch.reset();
                }
            }

            float progress = Math.min(1.0f, stopWatch.getElapsedTime() / 150.0f);
            Colour textColor = ColourUtils.interpolateColourC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, isHovered || isSelected ? progress : 0.0f);

            float finalHeight = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) * progress;
            NVGUtils.drawText(module.getName(), (float) (renderer.getPosition().x + 22), a, 12, textColor, NVGUtils.JOSEFIN);

            if (isSelected) {
                NVGUtils.drawRect((float) (renderer.getPosition().x + 16f), a - 1.5f, 2, finalHeight, FatalityColours.SELECTED);
                moduleComponent.render(gfx, mouseX, mouseY, partialTicks);
            }
            a += 23f;
            //a += NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 15f;
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().y + 112);

        for (ModuleComponent moduleComponent : renderer.moduleList.stream()
                .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category))
                .toList()) {
            Module module = moduleComponent.getModule();
            float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);
            float w = NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 4;

            renderer.maskList.add(new Mask((int) (renderer.getPosition().x + 16f), (int) (a - 8), (int) (w + 10), (int) (h + 10)));
            if (NVGUtils.isHovering(mouseX, mouseY, (int) (renderer.getPosition().x + 16f), (int) (a - 8), (int) (w + 10), (int) (h + 10))
                    && mouseButton == 0) {
                selected = moduleComponent;
            }
            if (selected == moduleComponent) {
                moduleComponent.click(mouseX, mouseY, mouseButton);
            }
            a += 23f;
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