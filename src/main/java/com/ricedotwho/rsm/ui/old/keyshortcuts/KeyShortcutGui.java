package com.ricedotwho.rsm.ui.old.keyshortcuts;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.KeyShortcuts;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.Shortcut;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.ui.old.api.FatalityColors;
import com.ricedotwho.rsm.utils.mouse.MouseUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class KeyShortcutGui extends Screen implements Accessor {
    @Getter
    @Setter
    private Vector2f position;

    private final float WIDTH = 850;
    private final float HEIGHT = 600;
    private final int RENDER_SECTION_HEIGHT = 400;
    private float scroll = 0;

    public KeyShortcutGui() {
        super(Component.literal("Key Shortcut Screen"));
        this.position = new Vector2f(1920 / 2F - WIDTH / 2F, 1080 / 2F - HEIGHT / 2F);
    }

    public static void open() {
        if (mc.gui.screen() == null){
            mc.gui.setScreen(RSM.getInstance().getShortcutGui());
            mc.onTextInputFocusChange(RSM.getInstance().getShortcutGui(), true);
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float deltaTicks) {
        Window window = mc.getWindow();
        gfx.push();
        gfx.pose().scale(1f / window.getGuiScale());
        this.position = new Vector2f(window.getWidth() / 2f - WIDTH / 2f, window.getHeight() / 2f - HEIGHT / 2f);

        double scaledMouseX = MouseUtils.mouseX();
        double scaledMouseY = MouseUtils.mouseY();

        Render2D.rect(gfx, (float) getPosition().x, (float) getPosition().y, WIDTH, HEIGHT, FatalityColors.BACKGROUND, 4f);

        float x = (float) getPosition().x, y = (float) (getPosition().y + 50), w = WIDTH, h = 525f;
        Render2D.rect(gfx, x, y, w, h, FatalityColors.PANEL);

        Render2D.line(gfx, getPosition().x, getPosition().y + 50, getPosition().x + WIDTH, getPosition().y + 50, FatalityColors.LINE);
        Render2D.line(gfx, getPosition().x, getPosition().y + HEIGHT - 25f, getPosition().x + WIDTH, getPosition().y + HEIGHT - 25f,  FatalityColors.LINE);
        Fonts.text(gfx, Fonts.JOSEFIN, RSM.getName(), getPosition().x + 20f, getPosition().y + 20.5f, 18, FatalityColors.NAME1);

        // add button
        float buttonX = getPosition().x + 16f;
        float buttonY = getPosition().y + 67f; // six sevennnnnnnnnnn
        boolean hoveringButton = MouseUtils.isHovering(scaledMouseX, scaledMouseY, buttonX, buttonY, 95f, 25);
        Render2D.rect(gfx, buttonX, buttonY, 94f, 25f, hoveringButton ? FatalityColors.SELECTED.darker() : FatalityColors.SELECTED, 3f);
        Fonts.text(gfx, Fonts.JOSEFIN, "New Shortcut", buttonX + (94f - Fonts.width("New Shortcut", 12)) / 2, buttonY + Fonts.height(12), 12, FatalityColors.TEXT);

        drawShortCuts(gfx, scaledMouseX, scaledMouseY);

        gfx.pop();
        super.extractRenderState(gfx, mouseX, mouseY, deltaTicks);
    }

    private void drawShortCuts(GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        List<Shortcut> list = KeyShortcuts.getData().getValue();
        if (list.size() * 40f < RENDER_SECTION_HEIGHT && scroll != 0) scroll = 0;
        float x = getPosition().x + 16f;
        float start = getPosition().y + 100f;
        float y = start - scroll;

        gfx.pushScissor(x, start, x + WIDTH, start + RENDER_SECTION_HEIGHT);

        for (Shortcut s : list) {
            if (y < start - 40) {
                y += 40;
                continue;
            }
            s.render(gfx, x, y, mouseX, mouseY);
            y += 40;
            if (y > start + RENDER_SECTION_HEIGHT + 20) break;
        }

        gfx.popScissor();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char typedChar = event.codepointAsString().charAt(0);
        boolean ret = false;
        for (Shortcut s : KeyShortcuts.getData().getValue()) {
            if (s.charTyped(typedChar, event.codepoint())) ret = true;
        }
        if (ret) return true;
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        boolean ret = false;
        for (Shortcut s : KeyShortcuts.getData().getValue()) {
            if (s.keyTyped(input)) ret = true;
        }
        if (ret) return true;
        return super.keyPressed(input);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = MouseUtils.mouseX();
        double mouseY = MouseUtils.mouseY();
        int button = click.button();

        float buttonX = getPosition().x + 16f;
        float buttonY = getPosition().y + 67f;
        boolean hoveringSearch = MouseUtils.isHovering(mouseX, mouseY, buttonX, buttonY, 95f, 25);
        if (hoveringSearch) {
            KeyShortcuts.add(new Shortcut());
            return false;
        }

        float x = getPosition().x + 16f;
        float start = getPosition().y + 100f;
        float y = start - scroll;

        for (Shortcut sc : KeyShortcuts.getData().getValue()) {
            if (y < start - 40) {
                y += 40;
                continue;
            }
            float relY = (float) (mouseY - y), relX = (float) (mouseX - x);
            if (sc.click(relX, relY, button)) {
                break;
            }
            y += 40;
            if (y > getPosition().y + 100f + RENDER_SECTION_HEIGHT + 20) break;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public final boolean mouseReleased(@NonNull MouseButtonEvent click) {
        return super.mouseReleased(click);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        float amount = (float) (Math.signum(vScroll) * 20f);
        float h = KeyShortcuts.getData().getValue().size() * 40f;
        if (h < RENDER_SECTION_HEIGHT && scroll != 0) return false;
        float nextScroll = scroll - amount;
        if (nextScroll < 0) return false;
        float bottom = h - (nextScroll);
        if (bottom < RENDER_SECTION_HEIGHT) return false;
        scroll = nextScroll;
        return super.mouseScrolled(mouseX, mouseY, hScroll, vScroll);
    }

    @Override
    public void onClose() {
        mc.onTextInputFocusChange(this, false);
        super.onClose();
    }
}
