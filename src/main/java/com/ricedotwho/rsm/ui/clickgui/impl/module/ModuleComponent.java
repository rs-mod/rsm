package com.ricedotwho.rsm.ui.clickgui.impl.module;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.utils.render.render2d.ColorUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import com.ricedotwho.rsm.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleComponent {
    private final RSMConfig renderer;
    private final Module module;
    private final List<GroupValueComponent> groupValues;
    private GroupValueComponent selectedGroup;
    private GroupValueComponent lastSelected;
    final StopWatch stopWatch = new StopWatch();

    public ModuleComponent(RSMConfig renderer, Module module) {
        this.renderer = renderer;
        this.module = module;
        groupValues = new ArrayList<>();
        groupValues.addAll(getModule().getGroupsSetting().stream()
                .map(setting -> new GroupValueComponent(setting, this))
                .toList());
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean value = false;
        for (GroupValueComponent group : groupValues) {
            if(group.charTyped(typedChar, keyCode)) value = true;
        }
        return value;
    }

    public boolean keyTyped(KeyEvent input) {
        boolean value = false;
        for (GroupValueComponent group : groupValues) {
            if(group.keyTyped(input)) value = true;
        }
        return value;
    }

    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        if (selectedGroup == null) {
            selectedGroup = groupValues.stream()
                    .filter(gv -> gv.getSetting().getName().equalsIgnoreCase("General"))
                    .findFirst()
                    .orElse(groupValues.isEmpty() ? null : groupValues.getFirst());
        }
        float a = (float) (renderer.getPosition().y + 112);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 16), (int) (a - 8),
                    (int) (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 10),
                    (int) (NVGUtils.getTextHeight(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 10));

            if (selectedGroup == group) {
                if (lastSelected != group) {
                    lastSelected = group;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                Colour textColor = ColorUtils.interpolateColorC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, progress);

                float finalHeight = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) * progress;
                NVGUtils.drawRect((float) (renderer.getPosition().x + 16f), a - 1.5f, 2, finalHeight, FatalityColours.SELECTED);
                NVGUtils.drawText(group.getSetting().getName(), (float) (renderer.getPosition().x + 22), a, 12, textColor, NVGUtils.JOSEFIN);
                group.render(gfx, mouseX, mouseY, partialTicks);
            } else {
                NVGUtils.drawText(group.getSetting().getName(), (float) (renderer.getPosition().x + 22), a, 12, hovered ? FatalityColours.SELECTED_TEXT : FatalityColours.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
            }
            a += 23f;
        }

    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().y + 112);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;
            if (NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 16f), (int) (a - 8),
                    (int) (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 10),
                    (int) (NVGUtils.getTextHeight(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 10)) && mouseButton == 0) {
                selectedGroup = group;


            }
            if (group == selectedGroup) {
                group.click(mouseX, mouseY, mouseButton);

            }
            a += 23f;
        }
    }

    public void release(double mouseX, double mouseY, int button) {
        for (GroupValueComponent group : groupValues) {
            if (group == selectedGroup) {
                group.release(mouseX, mouseY, button);
            }
        }
    }
}