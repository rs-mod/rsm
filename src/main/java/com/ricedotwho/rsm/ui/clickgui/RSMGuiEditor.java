package com.ricedotwho.rsm.ui.clickgui;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2d;
import com.ricedotwho.rsm.module.Module;

public class RSMGuiEditor extends Screen implements Accessor {

    public RSMGuiEditor() {
        super(Component.literal("RSM Gui Editor"));
    }

    public static void open() {
        if (mc.screen == null){
            mc.setScreen(RSM.getInstance().getGUIEditor());
        }
    }

    private double deltaX = 0;
    private double deltaY = 0;

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {

        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            NVGUtils.scale(RSMConfig.getStandardGuiScale());
            for (Module module : RSM.getInstance().getModuleManager().getModules()) {
                if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof DragSetting) {
                        DragSetting dragSetting = (DragSetting) setting;

                        if (dragSetting.isDragging()) {
                            dragSetting.setPosition(
                                    new Vector2d(
                                            Math.floor(deltaX + MouseUtils.scaledMouseX()),
                                            Math.floor(deltaY + MouseUtils.scaledMouseY())
                                    )
                            );
                        }

                        NVGUtils.drawText(setting.getName(),
                                (float) (dragSetting.getPosition().x - 5),
                                (float) (dragSetting.getPosition().y - 7 - NVGUtils.getTextHeight(16, NVGUtils.SF_PRO)),
                                16, Colour.WHITE, NVGUtils.SF_PRO);

                        NVGUtils.drawOutlineRect((float) ((int) dragSetting.getPosition().x - 5),
                                (float) ((int) dragSetting.getPosition().y - 5),
                                (float) (dragSetting.getScale().x + 10f),
                                (float) (dragSetting.getScale().y + 10f),5, 2, FatalityColours.TEXT);


                    }
                }
            }
        });
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return super.keyPressed(keyEvent);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public final boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {

        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting dragSetting) {
                    boolean hovering = NVGUtils.isHovering((int) MouseUtils.mouseX(), (int) MouseUtils.mouseY(),
                            (int) dragSetting.getPosition().x,
                            (int) dragSetting.getPosition().y,
                            (int) dragSetting.getScale().x,
                            (int) dragSetting.getScale().y,
                            true);

                    if (click.button() == 0 && hovering) {
                        dragSetting.setDragging(true);

                        double mouseX = MouseUtils.scaledMouseX(), mouseY = MouseUtils.scaledMouseY();

                        deltaX = (dragSetting.getPosition().x - mouseX);
                        deltaY = (dragSetting.getPosition().y - mouseY);

                        // set drag position relative to mouse click
                        dragSetting.setDragPos(new Vector2d(
                                Math.floor(deltaX + MouseUtils.scaledMouseX()),
                                Math.floor(deltaY + MouseUtils.scaledMouseY())));
                        return false;
                    }

                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent click) {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
            for (Setting<?> setting : module.getSettings()){
                if(setting instanceof DragSetting drag){
                    drag.setDragging(false);
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        super.mouseScrolled(mouseX, mouseY, hScroll, vScroll);

        int amount = (int) (Math.signum(vScroll) * 16);

        if (amount != 0) {
            for (Module module : RSM.getInstance().getModuleManager().getModules()) {
                if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof DragSetting dragSetting) {
                        boolean hovering = NVGUtils.isHovering((int) MouseUtils.mouseX(), (int) MouseUtils.mouseY(),
                                (int) dragSetting.getPosition().x,
                                (int) dragSetting.getPosition().y,
                                (int) dragSetting.getScale().x,
                                (int) dragSetting.getScale().y,
                                true);

                        if (hovering) {
                            double zoom = amount > 0 ? 1.15 : 0.85;
                            Vector2d scale = dragSetting.getScale();
                            double newWidth = scale.x * zoom;
                            double newHeight = scale.y * zoom;
                            scale.set(newWidth, newHeight);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void init() {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    ((DragSetting) setting).setDragging(false);
                }
            }
        }
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    ((DragSetting) setting).setDragging(false);
                }
            }
        }

        super.onClose();
    }
}

