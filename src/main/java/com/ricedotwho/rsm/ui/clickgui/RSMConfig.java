package com.ricedotwho.rsm.ui.clickgui;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.render.animation.Easing;
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

    private long lastMouseTime = 0;
    private boolean clickHandled = false;

    private static final long OPEN_ANIMATION_DURATION_MS = 250L;
    private long openAnimationStartTime = -1L;

    public RSMConfig() {
        super(Component.literal("RSM Config"));
        this.panel = new Panel(this);
        this.maskList = new ArrayList<>();
        this.position = new Vector2d(1920 / 2.0 - this.panel.getWidth() / 2.0, 1080 / 2.0 - this.panel.getHeight() / 2.0);

        this.categoryList = Arrays.stream(Category.values())
                .map(category -> new CategoryComponent(this, category))
                .sorted(Comparator.comparing(component -> component.getCategory().name().toLowerCase()))
                .collect(Collectors.toList());

        this.moduleList = Arrays.stream(RSM.getInstance().getModuleManager().getMap().values().toArray(new Module[0]))
                .map(module -> new ModuleComponent(this, module))
                .sorted(Comparator.comparing(component -> component.getModule().getName().toLowerCase()))
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

            ClickGUI clickGUI = RSM.getModule(ClickGUI.class);
            boolean animateOpen = clickGUI != null && clickGUI.getOpenAnimation().getValue() && openAnimationStartTime > 0L;

            if (animateOpen) {
                float progress = Math.min(1.0f, (System.currentTimeMillis() - openAnimationStartTime) / (float) OPEN_ANIMATION_DURATION_MS);
                float eased = Easing.OUT_CUBIC.getFunction().apply((double) progress).floatValue();
                float alpha = eased;
                float scale = 1.2f - (0.2f * eased);

                float centerX = (float) (this.position.x + (this.panel.getWidth() / 2.0));
                float centerY = (float) (this.position.y + (this.panel.getHeight() / 2.0));

                NVGUtils.push();
                NVGUtils.translate(centerX, centerY);
                NVGUtils.scale(scale, scale);
                NVGUtils.translate(-centerX, -centerY);
                NVGUtils.globalAlpha(alpha);
                this.panel.render(gfx, scaledMouseX, scaledMouseY, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                NVGUtils.pop();
            } else {
                this.panel.render(gfx, scaledMouseX, scaledMouseY, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
            }
        });
        super.render(gfx, mouseX, mouseY, deltaTicks);
    }

    public void startOpenAnimation() {
        this.openAnimationStartTime = System.currentTimeMillis();
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
        if (panel.charTyped(typedChar, event.codepoint())) return true;
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (panel.keyTyped(input)) return false;
        return super.keyPressed(input);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        long now = System.currentTimeMillis();
        lastMouseTime = now;

        clickHandled = false;

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

        return super.mouseClicked(click, doubled);
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent click) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseTime > 50) {
            float scale = getStandardGuiScale();
            double mouseX = MouseUtils.mouseX() / scale;
            double mouseY = MouseUtils.mouseY() / scale;
            panel.release(mouseX, mouseY, click.button());
        }
        return super.mouseReleased(click);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void reloadModules(){
        this.moduleList = Arrays.stream(RSM.getInstance().getModuleManager().getMap().values().toArray(new Module[0]))
                .map(module -> new ModuleComponent(this, module))
                .sorted(Comparator.comparing(component -> component.getModule().getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        float scale = getStandardGuiScale();
        double x = MouseUtils.mouseX() / scale;
        double y = MouseUtils.mouseY() / scale;
        panel.scroll(x, y, (int) Math.signum(vScroll));
        return super.mouseScrolled(mouseX, mouseY, hScroll, vScroll);
    }

    @Override
    public void onClose() {
        super.onClose();
        panel.onGuiClosed();
    }
}
