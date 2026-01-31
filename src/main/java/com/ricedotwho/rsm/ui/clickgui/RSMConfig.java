package com.ricedotwho.rsm.ui.clickgui;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2d;
import com.ricedotwho.rsm.module.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RSMConfig extends Screen implements Accessor {
    @Getter
    private final Panel panel;
    @Getter
    @Setter
    private Vector2d position;

    @Getter
    public List<Mask> maskList;
    public List<CategoryComponent> categoryList;
    public List<ModuleComponent> moduleList;

    public RSMConfig() {
        super(Component.literal("RSM Config"));
        this.panel = new Panel(this);
        this.maskList = new ArrayList<>();
        this.position = new Vector2d(1920 / 2.0 - this.panel.getWidth() / 2.0, 1080 / 2.0 - this.panel.getHeight() / 2.0);

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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float deltaTicks) {
        Window window = mc.getWindow();
        float standardScale = getStandardGuiScale();
        this.position = new Vector2d(window.getWidth() / (2f * standardScale) - this.panel.getWidth() / 2f, window.getHeight() / (2f * standardScale) - this.panel.getHeight() / 2f);

        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            double scaledMouseX = (MouseUtils.mouseX() / standardScale);
            double scaledMouseY = (MouseUtils.mouseY() / standardScale);
            NVGUtils.scale(standardScale, standardScale);
            this.panel.render(gfx, scaledMouseX, scaledMouseY, deltaTicks);
        });
        super.render(gfx, mouseX, mouseY, deltaTicks);
    }

    public static float getStandardGuiScale() {
        float verticalScale = (mc.getWindow().getHeight() / 1080f) / NVGUtils.devicePixelRatio();
        float horizontalScale = (mc.getWindow().getWidth() / 1920f) / NVGUtils.devicePixelRatio();
        return Math.max(1f, Math.min(Math.max(verticalScale, horizontalScale), 3f));
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char typedChar = event.codepointAsString().charAt(0);
        if (panel.charTyped(typedChar, event.codepoint())) return false;
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (panel.keyTyped(input)) return false;
        return super.keyPressed(input);
    }

    private boolean clickHandled = false;
    private long lastClickTime = 0;

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        clickHandled = false;
        lastClickTime = System.currentTimeMillis();

        float scale = getStandardGuiScale();
        double mouseX = MouseUtils.mouseX() / scale;
        double mouseY = MouseUtils.mouseY() / scale;
        int button = click.button();

        panel.click(mouseX, mouseY, button);

        if (!clickHandled && NVGUtils.isHovering(mouseX, mouseY, (int) getPosition().x, (int) getPosition().y, width, height) && button == 0) {
            for (Mask mask : maskList) {
                if (mask.contains(mouseX, mouseY)) {
                    clickHandled = true;
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent click) {
        long currentTime = System.currentTimeMillis();
        float scale = getStandardGuiScale();
        double mouseX = MouseUtils.mouseX() / scale;
        double mouseY = MouseUtils.mouseY() / scale;
        if (currentTime - lastClickTime > 50) {
            panel.release(mouseX, mouseY, click.button());
        }
        return false;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void reloadModules(){
        moduleList = Arrays.stream(RSM.getInstance().getModuleManager().getMap().values().toArray(new Module[0]))
                .map(module -> new ModuleComponent(this, module))
                .collect(Collectors.toList());
    }

    @Override
    public void onClose() {
        super.onClose();
        panel.onGuiClosed();
    }
}
