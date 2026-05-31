package com.ricedotwho.rsm.ui.keyshortcuts;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.KeyShortcuts;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.Shortcut;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
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

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

public class KeyShortcutGui extends Screen implements Accessor {
    @Getter
    @Setter
    private Vector2d position;

    @Getter
    public List<Mask> maskList;
    private final int WIDTH = 850;
    private final int HEIGHT = 600;
    private final int RENDER_SECTION_HEIGHT = 475;

    private long lastMouseTime = 0;
    private float scroll = 0;
    private boolean clickHandled = false;

    public KeyShortcutGui() {
        super(Component.literal("RSM Config"));
        this.maskList = new ArrayList<>();
        this.position = new Vector2d(1920 / 2.0 - WIDTH / 2.0, 1080 / 2.0 - HEIGHT / 2.0);
    }

    public static void open() {
        if (mc.screen == null){
            mc.setScreen(RSM.getInstance().getShortcutGui());
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float deltaTicks) {
        Window window = mc.getWindow();
        float standardScale = RSMConfig.getStandardGuiScale();
        this.position = new Vector2d(window.getWidth() / (2f * standardScale) - WIDTH / 2f, window.getHeight() / (2f * standardScale) - HEIGHT / 2f);

        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            double scaledMouseX = (MouseUtils.mouseX() / standardScale);
            double scaledMouseY = (MouseUtils.mouseY() / standardScale);
            NVGUtils.scale(standardScale, standardScale);


            NVGUtils.drawRect(getPosition().x, getPosition().y, WIDTH, HEIGHT, 4, FatalityColours.BACKGROUND);

            float x = (float) getPosition().x, y = (float) (getPosition().y + 50), w = WIDTH, h = 525f;
            NVGUtils.drawRect(x, y, w, h, FatalityColours.PANEL);

            NVGUtils.pushScissor(x, y, (int) w, (int) h);

            nvgBeginPath(NVGUtils.getVg());

            for (int i = 0; i < w + h; i += 4) {
                nvgMoveTo(NVGUtils.getVg(), x + i, y);
                nvgLineTo(NVGUtils.getVg(), x, y + i);
            }

            nvgStrokeWidth(NVGUtils.getVg(), 2f);
            NVGUtils.colour(FatalityColours.PANEL_LINES);
            nvgStrokeColor(NVGUtils.getVg(), NVGUtils.getNvgColor());
            nvgStroke(NVGUtils.getVg());

            NVGUtils.popScissor();

            NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + 50),
                    (float) getPosition().x + WIDTH, (float) (getPosition().y + 50), 1f, FatalityColours.LINE);
            NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + HEIGHT - 25f),
                    (float) getPosition().x + WIDTH, (float) (getPosition().y + HEIGHT - 25f), 1f, FatalityColours.LINE);

            NVGUtils.drawText(RSM.getName(), (float) (getPosition().x + 20f), (float) (getPosition().y + 20.5), 18, FatalityColours.NAME1, ClickGUI.getFont());

            // add button
            float buttonX = (float) (getPosition().x + 16f);
            float buttonY = (float) (getPosition().y + 67f); // six sevennnnnnnnnnn
            boolean hoveringButton = NVGUtils.isHovering(scaledMouseX, scaledMouseY, buttonX, buttonY, 95f, 25);
            NVGUtils.drawRect(buttonX, buttonY, 94f, 25f, 3f, hoveringButton ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED);
            NVGUtils.drawText("New Shortcut", buttonX + (94f - NVGUtils.getTextWidth("New Shortcut", 12, NVGUtils.JOSEFIN)) / 2, buttonY + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2 + 2, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

            drawShortCuts(gfx, scaledMouseX, scaledMouseY);
        });
        super.render(gfx, mouseX, mouseY, deltaTicks);
    }

    private void drawShortCuts(GuiGraphics gfx, double mouseX, double mouseY) {
        List<Shortcut> list = KeyShortcuts.getData().getValue();
        if (list.size() * 40f < RENDER_SECTION_HEIGHT && scroll != 0) scroll = 0;
        float x = (float) (getPosition().x + 16f);
        float start = (float) (getPosition().y + 100f);
        float y = start - scroll;

        NVGUtils.pushScissor(x, start, WIDTH, RENDER_SECTION_HEIGHT);

        for (Shortcut s : list) {
            if (y < start - 40) {
                y += 40;
                continue;
            }
            s.render(gfx, x, y, mouseX, mouseY);
            y += 40;
            if (y > start + RENDER_SECTION_HEIGHT + 20) break;
        }

        NVGUtils.popScissor();
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {

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
    public boolean keyPressed(KeyEvent input) {
        boolean ret = false;
        for (Shortcut s : KeyShortcuts.getData().getValue()) {
            if (s.keyTyped(input)) ret = true;
        }
        if (ret) return true;
        return super.keyPressed(input);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        lastMouseTime = System.currentTimeMillis();

        clickHandled = false;

        float scale = RSMConfig.getStandardGuiScale();
        double mouseX = MouseUtils.mouseX() / scale;
        double mouseY = MouseUtils.mouseY() / scale;
        int button = click.button();

        float buttonX = (float) (getPosition().x + 16f);
        float buttonY = (float) (getPosition().y + 67f);
        boolean hoveringSearch = NVGUtils.isHovering(mouseX, mouseY, buttonX, buttonY, 95f, 25);
        if (hoveringSearch) {
            KeyShortcuts.add(new Shortcut());
            return false;
        }

        float x = (float) (getPosition().x + 16f);
        float start = (float) (getPosition().y + 100f);
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
        super.onClose();
    }
}
