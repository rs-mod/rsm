package com.ricedotwho.rsm.ui.clickgui.impl.category;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render2d.ColourUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.joml.Vector2d;

import java.util.List;

@Getter
public class CategoryComponent {

    private final RSMConfig renderer;
    private final Category category;
    public ModuleComponent selected;
    public ModuleComponent lastSelected;
    private final StopWatch stopWatch = new StopWatch();
    private static final float MODULE_SECTION_HEIGHT = 463f;
    private static float scroll = 0;

    public CategoryComponent(RSMConfig renderer, Category category) {
        this.renderer = renderer;
        this.category = category;
    }

    public boolean scroll(double mouseX, double mouseY, int a) {
        if (!NVGUtils.isHovering(mouseX, mouseY, (float) getPosition().x, (float) (getPosition().y + 112), 126, MODULE_SECTION_HEIGHT)) return false;

        float amount = a * 11.5f;
        List<ModuleComponent> components = renderer.getPanel().getModuleResults().isEmpty()
                ? renderer.moduleList.stream().filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category)).toList()
                : renderer.getPanel().getModuleResults().stream().map(Panel.Entry::module).toList();

        float h = components.size() * 23f;

        if (h < MODULE_SECTION_HEIGHT && scroll != 0) return false;
        float nextScroll = scroll - amount;
        if (nextScroll < 0) return false;
        float bottom = h - (nextScroll);
        if (bottom < MODULE_SECTION_HEIGHT) return false;

        scroll = nextScroll;
        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean value = false;
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                if (module.charTyped(typedChar, keyCode)) value = true;
            }
        }
        return value;
    }

    public boolean keyTyped(KeyEvent input) {
        boolean value = false;
        for (ModuleComponent module : renderer.moduleList) {
            if (module.getModule().getCategory() == this.category) {
                if (module.keyTyped(input)) value = true;
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
                    .filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category) && !moduleComponent.getGroupValues().isEmpty())
                    .findFirst().orElse(null);
        }

        ModuleComponent renderComponent = null;

        float a = (float) (renderer.getPosition().y + 112) - scroll;

        List<ModuleComponent> components = renderer.getPanel().getModuleResults().isEmpty()
                ? renderer.moduleList.stream().filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category)).toList()
                : renderer.getPanel().getModuleResults().stream().map(Panel.Entry::module).toList();

        float totalHeight = components.size() * 23f;

        if (totalHeight < MODULE_SECTION_HEIGHT && scroll != 0) scroll = 0;

        // scroll bar
        if (NVGUtils.isHovering(mouseX, mouseY, (float) getPosition().x, (float) (getPosition().y + 112), 126, MODULE_SECTION_HEIGHT)) {
            float scrollX = (float) (getPosition().x + 8f);
            float scrollY = (float) (getPosition().y + 112);
            float maxScroll = Math.max(0, totalHeight - MODULE_SECTION_HEIGHT);
            float barHeight = Math.min(MODULE_SECTION_HEIGHT, (MODULE_SECTION_HEIGHT / totalHeight) * MODULE_SECTION_HEIGHT);

            if (barHeight < MODULE_SECTION_HEIGHT) {
                float scrollProgress = maxScroll == 0 ? 0 : scroll / maxScroll;
                float sY = scrollProgress * (MODULE_SECTION_HEIGHT - barHeight);
                NVGUtils.drawRect(scrollX, scrollY + sY - 4, 2f, barHeight + 4, 2f, FatalityColours.SCROLL_BAR);
            }
        }


        NVGUtils.pushScissor((float) renderer.getPosition().x, (float) (renderer.getPosition().y + 102), (float) (renderer.getPosition().x + 126), MODULE_SECTION_HEIGHT + 10);

        for (ModuleComponent moduleComponent : components) {
            boolean isSelected = (selected == moduleComponent);
            Module module = moduleComponent.getModule();

            float w = NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 10;

            boolean isHovered = NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 16), (int) (a - 8),
                    (int) (w),
                    (int) (NVGUtils.getTextHeight(module.getName(), 12, NVGUtils.JOSEFIN) + 10));

            if (isSelected) {
                if (lastSelected != moduleComponent) {
                    lastSelected = moduleComponent;
                    stopWatch.reset();
                }
            }

            if (module.isEnabled()) {
                NVGUtils.drawDropShadow((float) (renderer.getPosition().x + 15f), a - 3f, w + 4, 15f, 3f, 2f, 3f, FatalityColours.ENABLED);
            }

            float progress = Math.min(1.0f, stopWatch.getElapsedTime() / 150.0f);
            Colour textColor = ColourUtils.interpolateColourC(module.isEnabled() ? FatalityColours.ENABLED_TEXT : FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, isHovered || isSelected ? progress : 0.0f);

            float finalHeight = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) * progress;
            NVGUtils.drawText(module.getName(), (float) (renderer.getPosition().x + 22), a, 12, textColor, NVGUtils.JOSEFIN);

            if (isSelected) {
                NVGUtils.drawRect((float) (renderer.getPosition().x + 16f), a - 1.5f, 2, finalHeight, FatalityColours.SELECTED);
                renderComponent = moduleComponent;
            }
            a += 23f;
        }
        NVGUtils.popScissor();

        if (renderComponent != null) renderComponent.render(gfx, mouseX, mouseY, partialTicks);
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().y + 112) - scroll;

        List<ModuleComponent> components = renderer.getPanel().getModuleResults().isEmpty()
                ? renderer.moduleList.stream().filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category)).toList()
                : renderer.getPanel().getModuleResults().stream().map(Panel.Entry::module).toList();

        for (ModuleComponent moduleComponent : components) {
            if (a < renderer.getPosition().y + 112) {
                a += 23f;
                continue;
            }
            if (a > renderer.getPosition().y + 585) break;
            Module module = moduleComponent.getModule();
            float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);
            float w = NVGUtils.getTextWidth(module.getName(), 12, NVGUtils.JOSEFIN) + 4;

            renderer.maskList.add(new Mask((int) (renderer.getPosition().x + 16f), (int) (a - 8), (int) (w + 10), (int) (h + 10)));

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) (renderer.getPosition().x + 16f), (int) (a - 8), (int) (w + 10), (int) (h + 10));
            int button = RSM.getModule(ClickGUI.class).getToggleClickType().getIndex();

            if (module.getCategory() == this.category) {
                if (hovered) {
                    if ((mouseButton == button || module.getSettings().isEmpty()) && !module.getInfo().alwaysDisabled()) {
                        module.toggle();
                    } else if (!module.getSettings().isEmpty()) {
                        selected = moduleComponent;
                    }
                }
                if (selected == moduleComponent) {
                    moduleComponent.click(mouseX, mouseY, mouseButton);
                }
            } else if (hovered && !getRenderer().getPanel().getSearch().getValue().isBlank()) {
                if ((mouseButton == button || module.getSettings().isEmpty()) && !module.getInfo().alwaysDisabled()) {
                    module.toggle();
                } else if (!module.getSettings().isEmpty()) {
                    CategoryComponent cat = this.getRenderer().getPanel().getCategory(module.getCategory());
                    cat.selected = moduleComponent;
                    this.getRenderer().getPanel().setSelected(cat.category);
                    moduleComponent.click(mouseX, mouseY, mouseButton);
                }
            }
            a += 23f;
        }
    }

    public void release(double mouseX, double mouseY, int mouseButton) {

        List<ModuleComponent> components = renderer.getPanel().getModuleResults().isEmpty()
                ? renderer.moduleList.stream().filter(moduleComponent -> moduleComponent.getModule().getInfo().category().equals(category)).toList()
                : renderer.getPanel().getModuleResults().stream().map(Panel.Entry::module).toList();

        for (ModuleComponent moduleComponent : components) {
            if (selected == moduleComponent) {
                moduleComponent.release(mouseX, mouseY, mouseButton);
            }
        }
    }

    public Vector2d getPosition() {
        return renderer.getPosition();
    }

}