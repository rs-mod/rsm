package com.ricedotwho.rsm.ui.clickgui;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import com.ricedotwho.rsm.module.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RSMConfig extends Screen implements Accessor {
    @Getter
    private final Panel panel;
    private boolean dragging;
    @Getter
    @Setter
    private Vector2d position;
    private Vector2d dragPosition;


    @Setter
    private int scale;

    @Getter
    public List<Mask> maskList;
    public List<CategoryComponent> categoryList;
    public List<ModuleComponent> moduleList;

    public RSMConfig() {
        super(Component.literal("RSM Config"));
        this.scale = 1;
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
//        if (dragging) {
//            this.position.set((mouseX - dragPosition.x()), (mouseY - dragPosition.y()));
//        }

        Window window = mc.getWindow();
        this.position = new Vector2d(window.getGuiScaledWidth() / 2.0 - this.panel.getWidth() / 2.0, window.getGuiScaledHeight() / 2.0 - this.panel.getHeight() / 2.0);

        float standardScale = getStandardGuiScale();
        NVGUtils.scale(standardScale, standardScale);

        // main rendering goes here
        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            NVGUtils.scale(1f / window.getGuiScaledWidth()); // 1000f IDK
            this.panel.render(gfx, mouseX, mouseY, partialTicks);
        });

    }

    public static float getStandardGuiScale() {
        float verticalScale = (mc.getWindow().getHeight() / 1080f) / NVGUtils.devicePixelRatio();
        float horizontalScale = (mc.getWindow().getWidth() / 1920f) / NVGUtils.devicePixelRatio();
        return  Math.max(1f, Math.min(Math.max(verticalScale, horizontalScale), 3f));
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        String name = GLFW.glfwGetKeyName(keyEvent.key(), keyEvent.scancode());
        if (name == null) return super.keyPressed(keyEvent);
        char typedChar = name.charAt(0);
        if (panel.key(typedChar, keyEvent.key())) return false; // cancel keypress
        return super.keyPressed(keyEvent);
    }

    private boolean clickHandled = false;
    private long lastClickTime = 0;

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        clickHandled = false;
        lastClickTime = System.currentTimeMillis();

        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        panel.click(mouseX, mouseY, button);

        if (!clickHandled && NVGUtils.isHovering(mouseX, mouseY, (int) getPosition().x, (int) getPosition().y, 425 * scale, 300 * scale) && button == 0) {
            for (Mask mask : maskList) {
                if (mask.contains(mouseX, mouseY)) {
                    clickHandled = true;
                    return false;
                }
            }

            if (!clickHandled && NVGUtils.isHovering(mouseX, mouseY, (int) position.x, (int) position.y, this.panel.getWidth() * scale, 25 * scale)) {
                dragging = true;
                dragPosition = new Vector2d(mouseX - position.x(), mouseY - position.y());
            }
        }
        return false;
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent click) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > 50) {
            dragging = false;
            panel.release(click.x(), click.y(), click.button());
        } else {
            dragging = false;
        }
        return false;
    }

    @Override
    public void init() {
        dragging = false;
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
