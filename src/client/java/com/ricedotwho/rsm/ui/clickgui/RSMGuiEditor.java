package com.ricedotwho.rsm.ui.clickgui;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import org.joml.Vector2d;

import java.awt.*;
import java.io.IOException;

public class RSMGuiEditor extends GuiScreen {

    public RSMGuiEditor() {

    }

    public static void open() {
        if(Minecraft.getMinecraft().currentScreen == null){
            Minecraft.getMinecraft().displayGuiScreen(RSM.getInstance().getGUIEditor());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    DragSetting dragSetting = (DragSetting) setting;

                    if (dragSetting.isDragging()) {
                        dragSetting.setPosition(new Vector2d(mouseX - dragSetting.getDragPos().x,mouseY - dragSetting.getDragPos().y));
                    }

                    Fonts.getSFProRounded(16).drawString(setting.getName(), (float) (dragSetting.getPosition().x - 5),
                            (float) (dragSetting.getPosition().y - 7 - Fonts.getSFProRounded(16).getHeight(setting.getName())),
                            Color.WHITE.getRGB());

                    RenderUtils.drawRoundedRectOutline((int) dragSetting.getPosition().x - 5,
                            (int) dragSetting.getPosition().y - 5,
                            dragSetting.getScale().x + 10,
                            dragSetting.getScale().y + 10,5, 2, Color.WHITE);


                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    DragSetting dragSetting = (DragSetting) setting;
                    boolean hovering = RenderUtils.isHovering(mouseX, mouseY,
                            (int) dragSetting.getPosition().x,
                            (int) dragSetting.getPosition().y,
                            (int) dragSetting.getScale().x,
                            (int) dragSetting.getScale().y);

                    if (mouseButton == 0 && hovering) {
                        dragSetting.setDragging(true);

                        // Set drag position relative to mouse click
                        dragSetting.setDragPos(new Vector2d(
                                mouseX - dragSetting.getPosition().x,
                                mouseY - dragSetting.getPosition().y));
                        return; // let's not drag multiple at once yeah
                    }

                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
            for (Setting<?> setting : module.getSettings()){
                if(setting instanceof DragSetting){
                    DragSetting drag = (DragSetting) setting;
                    if(state == 0){
                        drag.setDragging(false);
                    }
                }
            }
        }
    }

    @Override
    public void initGui() {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    ((DragSetting) setting).setDragging(false);
                }
            }
        }
        super.initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        for (Module module : RSM.getInstance().getModuleManager().getModules()) {
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof DragSetting) {
                    ((DragSetting) setting).setDragging(false);
                }
            }
        }

        super.onGuiClosed();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int dWheel = Mouse.getDWheel(); // scroll amount

        if (dWheel != 0) {
            int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;

            for (Module module : RSM.getInstance().getModuleManager().getModules()) {
                if (!module.isEnabled() && !module.getInfo().alwaysDisabled()) continue;
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof DragSetting) {
                        DragSetting dragSetting = (DragSetting) setting;
                        boolean hovering = RenderUtils.isHovering(mouseX, mouseY,
                                (int) dragSetting.getPosition().x,
                                (int) dragSetting.getPosition().y,
                                (int) dragSetting.getScale().x,
                                (int) dragSetting.getScale().y);

                        if (hovering) {
                            double zoom = dWheel > 0 ? 1.15 : 0.85;
                            Vector2d scale = dragSetting.getScale();
                            double newWidth = scale.x * zoom;
                            double newHeight = scale.y * zoom;

                            scale.setX(newWidth);
                            scale.setY(newHeight);

                            return;
                        }
                    }
                }
            }
        }
    }
}

