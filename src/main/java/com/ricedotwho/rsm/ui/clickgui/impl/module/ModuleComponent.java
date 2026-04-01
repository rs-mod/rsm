package com.ricedotwho.rsm.ui.clickgui.impl.module;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.group.GroupValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.utils.render.animation.Animation;
import com.ricedotwho.rsm.utils.render.animation.Easing;
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
    final StopWatch stopWatch = new StopWatch();

    private final Animation hoverAnimation = new Animation()
            .setEasing(Easing.OUT_SINE)
            .setDuration(400);

    private final Animation selectAnimation = new Animation()
            .setEasing(Easing.OUT_SINE)
            .setDuration(400);

    private final Animation toggleAnimation = new Animation()
            .setEasing(Easing.OUT_SINE)
            .setDuration(200);

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
        float y = (float) ((renderer.getPosition().y + 75F) - h);

        for (GroupValueComponent group : groupValues) {
            if (!group.getSetting().isShown()) continue;

            float w = NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 4;

            boolean isHovered = NVGUtils.isHovering(
                    mouseX, mouseY,
                    (a - 2), y,
                    w, h * 2 + 10
            );

            boolean isEnabled = group.getSetting().getValue().isEnabled() && !group.getSetting().getValue().getInfo().alwaysDisabled();
            boolean isSelected = selectedGroup == group;

            Animation hoverAnimation = group.getHoverAnimation();
            Animation selectAnimation = group.getSelectAnimation();
            Animation toggleAnimation = group.getToggleAnimation();

            hoverAnimation
                    .setTargetValue(isSelected || isHovered ? 1 : 0)
                    .run();

            selectAnimation
                    .setTargetValue(isSelected ? 1 : 0)
                    .run();

            toggleAnimation
                    .setTargetValue(isEnabled ? 1 : 0)
                    .run();

            float hoverValue = hoverAnimation.getValue().floatValue();
            float selectValue = selectAnimation.getValue().floatValue();
            float toggleValue = toggleAnimation.getValue().floatValue();

            Colour highlightColor = ColourUtils.interpolateColourC(Colour.TRANSPARENT, FatalityColours.ENABLED, toggleValue);
            Colour textColor = ColourUtils.interpolateColourC(isEnabled ? FatalityColours.ENABLED_TEXT : FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, hoverValue);
            float finalWidth = (NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) * 1.05f) * selectValue;

            NVGUtils.drawDropShadow(a - 2f, (float) (renderer.getPosition().y + 71F), w + 2, 16f, 3f, 2f, 3f, highlightColor);
            NVGUtils.drawText(group.getSetting().getName(), a, (float) (renderer.getPosition().y + 75F), 12, textColor, NVGUtils.JOSEFIN);

            if (finalWidth > 0.05) {
                NVGUtils.drawRect(a, (float) (renderer.getPosition().y + 90), finalWidth - 2, 2, FatalityColours.SELECTED);
            }

            if (isSelected) {
                group.render(gfx, mouseX, mouseY, partialTicks);
            }

            a += NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 15f;
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        float a = (float) (renderer.getPosition().x + 144f);
        float h = NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN);

        for (GroupValueComponent group : groupValues) {
            if (!group.getSetting().isShown()) continue;

            if (NVGUtils.isHovering(mouseX, mouseY,
                    (int) (a - 2),
                    (int) ((int) (renderer.getPosition().y + 75F) - h),
                    (int) NVGUtils.getTextWidth(group.getSetting().getName(), 12, NVGUtils.JOSEFIN) + 4,
                    (int) h * 2 + 10)) {

                if (mouseButton == RSM.getModule(ClickGUI.class).getToggleClickType().getIndex() && !group.getSetting().getValue().getInfo().alwaysDisabled() && !group.getSetting().getName().equals("General")) {
                    group.getSetting().getValue().toggle();
                } else if (!group.getSetting().getValue().getSettings().isEmpty()) {
                    selectedGroup = group;
                }
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