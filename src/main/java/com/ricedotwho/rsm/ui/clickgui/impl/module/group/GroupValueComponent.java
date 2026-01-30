package com.ricedotwho.rsm.ui.clickgui.impl.module.group;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.*;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class GroupValueComponent implements Accessor {
    @Getter
    private GroupSetting setting;
    private List<ValueComponent> settings;
    @Getter
    private ModuleComponent module;

    private final float panelWidth = 710;
    private final float  panelHeight = 460f;

    public GroupValueComponent(GroupSetting setting, ModuleComponent parent) {
        this.setting = setting;
        this.module = parent;

        settings = new ArrayList<>();
        if (setting.getName().equals("General")) {
            if(!module.getModule().getInfo().alwaysDisabled()) settings.add(new BooleanValueComponent(parent));
            if(module.getModule().getInfo().hasKeybind()) settings.add(new KeybindValueComponent(parent));
        }
        settings.addAll(getSetting().getValue().stream()
                .map(setting1 -> {
                    if (setting1 instanceof BooleanSetting)
                        return new BooleanValueComponent((BooleanSetting) setting1, parent);
                    if (setting1 instanceof ModeSetting)
                        return new ModeValueComponent((ModeSetting) setting1, parent);
                    if (setting1 instanceof MultiBoolSetting)
                        return new MultiBoolValueComponent((MultiBoolSetting) setting1, parent);
                    if (setting1 instanceof NumberSetting)
                        return new NumberValueComponent((NumberSetting) setting1, parent);
                    if (setting1 instanceof StringSetting)
                        return new StringValueComponent((StringSetting) setting1, parent);
                    if(setting1 instanceof KeybindSetting)
                        return new KeybindValueComponent((KeybindSetting) setting1, parent);
                    if(setting1 instanceof ButtonSetting)
                        return new ButtonValueComponent((ButtonSetting) setting1, parent);
                    if(setting1 instanceof ColourSetting)
                        return new ColourValueComponent((ColourSetting) setting1, parent);
                    return new EmptyValueComponent(setting1, parent);
                })
                .collect(Collectors.toList()));
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean value = false;
        for (ValueComponent component : settings) {
            if(!isSettingShown(component)) continue;
            if(component.charTyped(typedChar, keyCode)) value = true;
        }
        return value;
    }

    public boolean keyTyped(KeyEvent input) {
        boolean value = false;
        for (ValueComponent component : settings) {
            if(!isSettingShown(component)) continue;
            if(component.keyTyped(input)) value = true;
        }
        return value;
    }

    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float panelX = (float) (module.getRenderer().getPosition().x + 126f);
        float panelY = (float) (module.getRenderer().getPosition().y + 99f);

        NVGUtils.drawRect(panelX, panelY, panelWidth, panelHeight, 6, new Colour(28, 28, 28));
        NVGUtils.drawOutlineRect(panelX, panelY, panelWidth, panelHeight, 6, 1, new Colour(50, 50, 50));

        List<ValueComponent<?>> expandedDropdown = new ArrayList<>();

        int offsetY = (int) (module.getRenderer().getPosition().y + 128f);
        int offsetX = (int) (module.getRenderer().getPosition().x + 144f);
        int offset = 0;

        for (ValueComponent<?> component : settings) {
            if(!isSettingShown(component)) continue;
            component.setPosition(new Vector2f(offsetX, offsetY));

            if ((component instanceof ModeValueComponent && ((ModeValueComponent) component).isExpanded())
                    || (component instanceof MultiBoolValueComponent && ((MultiBoolValueComponent) component).isExpanded())
                    || (component instanceof ColourValueComponent && ((ColourValueComponent) component).isExpanded())) {
                expandedDropdown.add(component);
            } else {
                component.render(gfx, mouseX, mouseY, partialTicks);
            }

            if (!(component instanceof EmptyValueComponent)) {
                offsetY += 28;
                offset++;

                if (offset % 15 == 0) {
                    offset = 0;
                    offsetY = (int) (module.getRenderer().getPosition().y + 128f);
                    offsetX += 350;
                }
            }
        }

        expandedDropdown.forEach(component -> component.render(gfx, mouseX, mouseY, partialTicks));
    }

    public void click(double mouseX, double mouseY, int button) {
        for (ValueComponent<?> component : settings) {
            component.resetClick();
        }

        ValueComponent<?> expandedDropdown = null;

        for (ValueComponent<?> component : settings) {
            if(!isSettingShown(component)) continue;
            if ((component instanceof ModeValueComponent && ((ModeValueComponent) component).isExpanded())
                    || (component instanceof MultiBoolValueComponent && ((MultiBoolValueComponent) component).isExpanded())
                    || (component instanceof ColourValueComponent && ((ColourValueComponent) component).isExpanded())) {
                expandedDropdown = component;
                break;
            }
        }

        if (expandedDropdown != null) {
            float dropdownX = expandedDropdown.getPosition().x;
            float dropdownY = expandedDropdown.getPosition().y;
            float dropdownWidth = 160;
            float dropdownHeight = getDropdownHeight(expandedDropdown);

            if (NVGUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) dropdownY, (int) dropdownWidth, (int) dropdownHeight)) {
                expandedDropdown.click(mouseX, mouseY, button);
                return;
            }
        }

        for (ValueComponent<?> component : settings) {
            if(component.getPosition() == null || !isSettingShown(component)) continue;
            if (!component.isClickConsumed()) {
                component.click(mouseX, mouseY, button);
                if (component.isClickConsumed()) {
                    return;
                }
            }
        }
    }

    private static float getDropdownHeight(ValueComponent<?> expandedDropdown) {
        float dropdownHeight = 0;

        if (expandedDropdown instanceof ModeValueComponent) {
            dropdownHeight = ((ModeValueComponent) expandedDropdown).getSetting().getValues().size() * 14;
        }
        if (expandedDropdown instanceof MultiBoolValueComponent) {
            dropdownHeight = ((MultiBoolValueComponent) expandedDropdown).getSetting().getValue().size() * 14;
        }
        if(expandedDropdown instanceof ColourValueComponent) {
            dropdownHeight = 66;
        }
        return dropdownHeight;
    }

    public void release(double mouseX, double mouseY, int button) {
        for (ValueComponent<?> component : settings) {
            if(!isSettingShown(component)) continue;
            component.release(mouseX, mouseY, button);
        }
    }
    private boolean isSettingShown(ValueComponent<?> component){
        if(component.getSetting() == null) return true; // module toggles are null for some reason
        return component.getSetting().getSupplier() != null && component.getSetting().getSupplier().getAsBoolean();
    }
}
