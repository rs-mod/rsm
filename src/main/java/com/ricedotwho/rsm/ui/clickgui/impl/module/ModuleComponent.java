package com.ricedotwho.rsm.ui.clickgui.impl.module;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.utils.render.render2d.ColourUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
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

    public static InputValueComponent<?> focusedComponent;

    private final float WIDTH = 710;
    private final float HEIGHT = 499f;

    public ModuleComponent(RSMConfig renderer, Module module) {
        this.renderer = renderer;
        this.module = module;
        groupValues = new ArrayList<>();
        groupValues.addAll(getModule().getSettings().stream()
                .map(setting -> new GroupValueComponent(setting, this.getModule()))
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

        float panelX = (float) (RSM.getInstance().getConfigGui().getPosition().x + 126f);
        float panelY = (float) (RSM.getInstance().getConfigGui().getPosition().y + 60f);

        NVGUtils.drawRect(panelX, panelY, WIDTH, HEIGHT, 6, FatalityColours.GROUP_FILL);
        NVGUtils.drawOutlineRect(panelX, panelY, WIDTH, HEIGHT, 6, 1, FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawLine(panelX + 5, panelY + 39f,  panelX + WIDTH - 5, panelY + 39f, 1f, FatalityColours.GROUP_OUTLINE);


        float a = (float) (renderer.getPosition().x + 144f);
        float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);

        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;

            boolean hovered = NVGUtils.isHovering(mouseX, mouseY,
                    (int) (a - 2),
                    (int) ((int) (renderer.getPosition().y + 75F) - h),
                    (int) NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 4,
                    (int) h * 2 + 10);

            if (selectedGroup == group) {
                if (lastSelected != group) {
                    lastSelected = group;
                    stopWatch.reset();
                }

                long elapsed = stopWatch.getElapsedTime();
                float progress = Math.min(1.0f, elapsed / 150.0f);

                Colour textColor = ColourUtils.interpolateColourC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, progress);

//                float finalHeight = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) * progress;
//                NVGUtils.drawRect((float) (renderer.getPosition().x + 16f), a - 1.5f, 2, finalHeight, FatalityColours.SELECTED);
//                NVGUtils.drawText(group.getSetting().getName(), (float) (renderer.getPosition().x + 22), a, 12, textColor, NVGUtils.JOSEFIN);

                float finalWidth = (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) * 1.05f) * progress;
                NVGUtils.drawRect(a, (float) (renderer.getPosition().y + 90), finalWidth - 2, 2, FatalityColours.SELECTED);
                NVGUtils.drawText(group.getSetting().getName(), a, (float) (renderer.getPosition().y + 75F), 12, textColor, NVGUtils.JOSEFIN);
                group.render(gfx, mouseX, mouseY, partialTicks);
            } else {
                NVGUtils.drawText(group.getSetting().getName(), a, (float) (renderer.getPosition().y + 75F), 12, hovered ? FatalityColours.SELECTED_TEXT : FatalityColours.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
            }
            a += NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 15f;
        }

    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().x + 144f);
        float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);
        for (GroupValueComponent group : groupValues) {
            if(!group.getSetting().isShown()) continue;
            if (NVGUtils.isHovering(mouseX, mouseY,
                    (int) (a - 2),
                    (int) ((int) (renderer.getPosition().y + 75F) - h),
                    (int) NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 4,
                    (int) h * 2 + 10) && mouseButton == 0) {
                selectedGroup = group;


            }
            if (group == selectedGroup) {
                group.click(mouseX, mouseY, mouseButton);

            }
            a += NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 15f;
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