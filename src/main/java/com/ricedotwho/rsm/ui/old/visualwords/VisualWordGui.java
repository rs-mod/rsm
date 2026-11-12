package com.ricedotwho.rsm.ui.old.visualwords;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWord;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.ui.old.api.FatalityColors;
import com.ricedotwho.rsm.ui.old.api.Mask;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VisualWordGui extends Screen implements Accessor {
    @Getter
    @Setter
    private Vector2f position;

    @Getter
    public List<Mask> maskList;
    private final int WIDTH = 850;
    private final int HEIGHT = 600;
    private final int RENDER_SECTION_HEIGHT = 475;

    private float scroll = 0f;
    private boolean clickHandled = false;

    private final List<VisualWordRow> rows = new ArrayList<>();

    public VisualWordGui() {
        super(Component.literal("RSM Config"));
        this.maskList = new ArrayList<>();
        this.position = new Vector2f(1920 / 2f - WIDTH / 2f, 1080 / 2f - HEIGHT / 2f);
    }

    public static void open() {
        if (mc.gui.screen() == null) {
            mc.gui.setScreen(RSM.getInstance().getVisualWordGui());
            mc.onTextInputFocusChange(RSM.getInstance().getVisualWordGui(), true);
        }
    }

    @Override
    public void init() {
        reloadRows();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float deltaTicks) {
        Window window = mc.getWindow();
        gfx.push();
        gfx.pose().scale(1f / window.getGuiScale());
        this.position = new Vector2f(window.getWidth() / 2f - WIDTH / 2f, window.getHeight() / 2f - HEIGHT / 2f);

        double scaledMouseX = MouseUtils.mouseX();
        double scaledMouseY = MouseUtils.mouseY();

        Render2D.rect(gfx, getPosition().x, getPosition().y, WIDTH, HEIGHT, FatalityColors.BACKGROUND, 4f);

        float x = getPosition().x;
        float y = getPosition().y + 50;
        float w = WIDTH;
        float h = 525f;
        Render2D.rect(gfx, x, y, w, h, FatalityColors.PANEL);

        Render2D.line(gfx, getPosition().x, getPosition().y + 50, getPosition().x + WIDTH, getPosition().y + 50, FatalityColors.LINE);
        Render2D.line(gfx, getPosition().x, getPosition().y + HEIGHT - 25f, getPosition().x + WIDTH, getPosition().y + HEIGHT - 25f,  FatalityColors.LINE);
        Fonts.text(gfx, Fonts.JOSEFIN, RSM.getName(), getPosition().x + 20f, getPosition().y + 20.5f, 18, FatalityColors.NAME1);

        float buttonX = getPosition().x + 16f;
        float buttonY = getPosition().y + 67f; // six sevennnnnnnnnnn
        boolean hoveringButton = MouseUtils.isHovering(scaledMouseX, scaledMouseY, buttonX, buttonY, 95f, 25);
        Render2D.rect(gfx, buttonX, buttonY,  94f, 25f, hoveringButton ? FatalityColors.SELECTED.darker() : FatalityColors.SELECTED, 3F);
        Fonts.text(gfx, Fonts.JOSEFIN, "New Visual Word", buttonX + (94f - Fonts.width("New Visual Word", 12)) / 2, buttonY + Fonts.height(12) / 2 + 2, 12, FatalityColors.TEXT);

        drawRows(gfx, scaledMouseX, scaledMouseY);

        gfx.pop();
        super.extractRenderState(gfx, mouseX, mouseY, deltaTicks);
    }

    private void drawRows(GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        if (rows.size() * 40f < RENDER_SECTION_HEIGHT && scroll != 0f) {
            scroll = 0f;
        }

        float x = (float) (getPosition().x + 16f);
        float start = (float) (getPosition().y + 100f);
        float y = start - scroll;

        gfx.pushScissor(x, start, x + WIDTH, start + RENDER_SECTION_HEIGHT);

        for (VisualWordRow row : rows) {
            if (y < start - 40f) {
                y += 40f;
                continue;
            }

            row.render(gfx, x, y, mouseX, mouseY);
            y += 40f;
            if (y > start + RENDER_SECTION_HEIGHT + 20f) {
                break;
            }
        }

        gfx.popScissor();
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char typedChar = event.codepointAsString().charAt(0);
        boolean handled = false;

        for (VisualWordRow row : rows) {
            handled = row.charTyped(typedChar) || handled;
        }

        if (handled) {
            return true;
        }

        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        boolean handled = false;

        for (VisualWordRow row : rows) {
            handled = row.keyTyped(input) || handled;
        }

        if (handled) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        clickHandled = false;

        double mouseX = MouseUtils.mouseX();
        double mouseY = MouseUtils.mouseY();
        int button = click.button();

        float buttonX = getPosition().x + 16f;
        float buttonY = getPosition().y + 67f;
        boolean hoveringButton = MouseUtils.isHovering(mouseX, mouseY, buttonX, buttonY, 115f, 25f);
        if (hoveringButton) {
            addVisualWord();
            return false;
        }

        float x = getPosition().x + 16f;
        float start = getPosition().y + 100f;
        float y = start - scroll;

        for (int i = 0; i < rows.size(); i++) {
            VisualWordRow row = rows.get(i);
            if (y < start - 40f) {
                y += 40f;
                continue;
            }

            float relY = (float) (mouseY - y);
            float relX = (float) (mouseX - x);
            if (row.click(relX, relY, button)) {
                rows.remove(i);
                break;
            }

            y += 40f;
            if (y > getPosition().y + 100f + RENDER_SECTION_HEIGHT + 20f) {
                break;
            }
        }

        if (!clickHandled && MouseUtils.isHovering(mouseX, mouseY, (int) getPosition().x, (int) getPosition().y, width, height) && button == InputConstants.MOUSE_BUTTON_LEFT) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        float amount = (float) (Math.signum(vScroll) * 20f);
        float totalHeight = rows.size() * 40f;

        if (totalHeight < RENDER_SECTION_HEIGHT && scroll != 0f) {
            return false;
        }

        float nextScroll = scroll - amount;
        if (nextScroll < 0f) {
            return false;
        }

        float bottom = totalHeight - nextScroll;
        if (bottom < RENDER_SECTION_HEIGHT) {
            return false;
        }

        scroll = nextScroll;
        return super.mouseScrolled(mouseX, mouseY, hScroll, vScroll);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        mc.onTextInputFocusChange(this, false);
        rows.forEach(VisualWordRow::commitPendingEdits);
        super.onClose();
    }

    private void reloadRows() {
        rows.clear();
        VisualWords.getInstance().getData().getValue().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toLowerCase()))
                .forEach(entry -> rows.add(new VisualWordRow(entry.getKey(), entry.getValue())));
    }

    private void addVisualWord() {
        String phrase = nextPhrase();
        VisualWord word = new VisualWord(Component.literal("replacement"), true);
        VisualWords.getInstance().getData().getValue().put(phrase, word);
        VisualWords.getInstance().getData().save();
        rows.add(new VisualWordRow(phrase, word));
    }

    private String nextPhrase() {
        String base = "phrase";
        if (!VisualWords.getInstance().getData().getValue().containsKey(base)) {
            return base;
        }

        int index = 2;
        while (VisualWords.getInstance().getData().getValue().containsKey(base + index)) {
            index++;
        }

        return base + index;
    }
}

