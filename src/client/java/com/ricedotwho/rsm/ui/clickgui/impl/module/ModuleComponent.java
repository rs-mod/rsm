package com.ricedotwho.rsm.ui.clickgui.impl.module;

import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.ColorUtils;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
import com.ricedotwho.rsm.module.Module;

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

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (selectedGroup == null) {
            selectedGroup = groupValues.stream()
                    .filter(gv -> gv.getSetting().getName().equalsIgnoreCase("General"))
                    .findFirst()
                    .orElse(groupValues.isEmpty() ? null : groupValues.get(0));
        }
        float a = (float) (renderer.getPosition().y + 56);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;

            boolean hovered = RenderUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 8), (int) (a - 4),
                    (int) (Fonts.getJoseFin(12).getWidth(group.getSetting().getName()) + 5),
                    (int) (Fonts.getJoseFin(12).getHeight(group.getSetting().getName()) + 5));

            if (selectedGroup == group) {
                if (lastSelected != group) {
                    lastSelected = group;
                    stopWatch.reset();
                }
                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                int textColor = ColorUtils.interpolateInt(FatalityColors.UNSELECTED_TEXT.getRGB(), FatalityColors.SELECTED_TEXT.getRGB(), progress);

                float finalHeight = Fonts.getJoseFin(12).getHeight(group.getSetting().getName()) * 2 * progress;
                RenderUtils.drawRect(renderer.getPosition().x + 8, a - Fonts.getJoseFin(12).getHeight(group.getSetting().getName()), 1, finalHeight, FatalityColors.SELECTED);
                Fonts.getJoseFin(12).drawString(group.getSetting().getName(), (float) (renderer.getPosition().x + 11), a, textColor);
                group.render(mouseX, mouseY, partialTicks);
            } else {
                Fonts.getJoseFin(12).drawString(group.getSetting().getName(), (float) (renderer.getPosition().x + 11), a, hovered ? FatalityColors.SELECTED_TEXT.getRGB() : FatalityColors.UNSELECTED_TEXT.getRGB());
            }
            a += 11.5f;
        }

    }

    public void click(double mouseX, double mouseY, float mouseButton) {
        float a = (float) (renderer.getPosition().y + 56);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;
            if (RenderUtils.isHovering(mouseX, mouseY, (int)
                            (renderer.getPosition().x + 8), (int) (a - 4),
                    (int) (Fonts.getJoseFin(12).getWidth(group.getSetting().getName()) + 5),
                    (int) (Fonts.getJoseFin(12).getHeight(group.getSetting().getName()) + 5)) && mouseButton == 0) {
                selectedGroup = group;


            }
            if (group == selectedGroup) {
                group.click(mouseX, mouseY, mouseButton);

            }
            a += 11.5f;
        }

    }

    public void release(double mouseX, double mouseY, float button) {
        for (GroupValueComponent group : groupValues) {
            if (group == selectedGroup) {
                group.release(mouseX, mouseY, button);
            }
        }
    }
}
