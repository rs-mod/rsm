package com.ricedotwho.rsm.ui.clickgui.impl.module;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
import com.ricedotwho.rsm.module.Module;
import net.minecraft.client.gui.GuiGraphics;

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

    public boolean key(char typedChar, int keyCode) {
        boolean value = false;
        for (GroupValueComponent group : groupValues) {
            if(group.key(typedChar, keyCode)) value = true;
        }
        return value;
    }

    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        if (selectedGroup == null) {
            selectedGroup = groupValues.stream()
                    .filter(gv -> gv.getSetting().getName().equalsIgnoreCase("General"))
                    .findFirst()
                    .orElse(groupValues.isEmpty() ? null : groupValues.get(0));
        }
        float a = (float) (renderer.getPosition().y + 56);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 8), (int) (a - 4),
                    (int) (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 5),
                    (int) (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 5), false);

            if (selectedGroup == group) {
                if (lastSelected != group) {
                    lastSelected = group;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                Colour textColor = ColorUtils.interpolateColorC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, progress);

                float finalHeight = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) * 2 * progress;
                NVGUtils.drawRect((float) (renderer.getPosition().x + 8f), a - NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN), 1, finalHeight, FatalityColours.SELECTED);
                NVGUtils.drawText(group.getSetting().getName(), (float) (renderer.getPosition().x + 11), a, 12, textColor, NVGUtils.JOSEFIN);
                group.render(gfx, mouseX, mouseY, partialTicks);
            } else {
                NVGUtils.drawText(group.getSetting().getName(), (float) (renderer.getPosition().x + 11), a, 12, hovered ? FatalityColours.SELECTED_TEXT : FatalityColours.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
            }
            a += 11.5f;
        }

    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().y + 56);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;
            if (NVGUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 8), (int) (a - 4),
                    (int) (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 5),
                    (int) (NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN)), true) && mouseButton == 0) {
                selectedGroup = group;


            }
            if (group == selectedGroup) {
                group.click(mouseX, mouseY, mouseButton);

            }
            a += 11.5f;
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