package com.ricedotwho.rsm.ui.clickgui;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.joml.Vector2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RSMConfig extends GuiScreen {
    @Getter
    private final Panel panel;
    @Getter
    @Setter
    private Vector2d position;


    @Setter
    private int scale;

    @Getter
    public List<Mask> maskList;
    public List<CategoryComponent> categoryList;
    public List<ModuleComponent> moduleList;
    private boolean clickHandled = false;
    private long lastClickTime = 0;

    public RSMConfig() {
        this.scale = 1;
        this.panel = new Panel(this);
        this.maskList = new ArrayList<>();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.position = new Vector2d(sr.getScaledWidth() / 2.0 - this.panel.getWidth() / 2.0 + 50, sr.getScaledHeight() / 2.0 - this.panel.getHeight() / 2.0 + 50);

        this.categoryList = Arrays.stream(Category.values())
                .map(category -> new CategoryComponent(this, category))
                .sorted(Comparator.comparing(component -> component.getCategory().name()))
                .collect(Collectors.toList());

        this.moduleList = Arrays.stream(RSM.getInstance().getModuleManager().getMap().values().toArray(new Module[0]))
                .map(module -> new ModuleComponent(this, module))
                .sorted(Comparator.comparing(component -> component.getModule().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.position = new Vector2d(sr.getScaledWidth() / 2.0 - this.panel.getWidth() / 2.0, sr.getScaledHeight() / 2.0 - this.panel.getHeight() / 2.0);

        this.panel.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(panel.key(typedChar, keyCode)) return; // cancel keypress
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        clickHandled = false;
        lastClickTime = System.currentTimeMillis();

        panel.click(mouseX, mouseY, mouseButton);

        if (!clickHandled && RenderUtils.isHovering(mouseX, mouseY, (int) getPosition().x, (int) getPosition().y, 425 * scale, 300 * scale) && mouseButton == 0) {
            for (Mask mask : maskList) {
                if (mask.contains(mouseX, mouseY)) {
                    clickHandled = true;
                    return;
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > 50) {
            panel.release(mouseX, mouseY, state);
        } else {
        }
    }

//    @Override // This should not be needed
//    public void onGuiClosed() {
//        ConfigUtil.configSave("default");
//    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void reloadModules(){
        moduleList = Arrays.stream(RSM.getInstance().getModuleManager().getMap().values().toArray(new Module[0]))
                .map(module -> new ModuleComponent(this, module))
                .collect(Collectors.toList());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        panel.onGuiClosed();
    }
}
