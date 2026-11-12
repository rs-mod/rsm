package com.ricedotwho.rsm.ui.old.visualwords;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWord;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.old.TextInput;
import com.ricedotwho.rsm.ui.old.api.FatalityColors;
import com.ricedotwho.rsm.utils.mouse.MouseUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

@Getter
public class VisualWordRow {
    private static final float WIDTH = 818f;
    private static final float HEIGHT = 30f;
    private static final float GAP = 5f;
    private static final float BOX_HEIGHT = 20f;
    private static final float PHRASE_WIDTH = 260f;
    private static final float REPLACEMENT_WIDTH = 400f;
    private static final float BUTTON_WIDTH = 70f;
    private static final float DELETE_WIDTH = 63f;

    private static VisualWordRow selected = null;

    private String phrase;
    private final VisualWord visualWord;

    private final TextInput phraseInput;
    private final TextInput replacementInput;

    public VisualWordRow(String phrase, VisualWord visualWord) {
        this.phrase = phrase;
        this.visualWord = visualWord;
        this.phraseInput = new TextInput(phrase, 12, false, 128);
        this.replacementInput = new TextInput(visualWord.replacement.getString(), 12, false, 256);
    }

    public boolean click(double mouseX, double mouseY, int button) {
        float replacementX = GAP + PHRASE_WIDTH + GAP;
        float enabledX = replacementX + REPLACEMENT_WIDTH + GAP;
        float deleteX = enabledX + BUTTON_WIDTH + GAP;

        if (MouseUtils.isHovering(mouseX, mouseY, GAP, GAP, PHRASE_WIDTH, BOX_HEIGHT)) {
            if (replacementInput.isWriting() && selected == this) {
                commitReplacement();
            }
            selected = this;
            phraseInput.setWriting(true);
            replacementInput.setWriting(false);
            phraseInput.click((float) (mouseX - (GAP + 5f)), button);
        } else if (phraseInput.isWriting() && selected == this) {
            commitPhrase();
            phraseInput.setWriting(false);
            selected = null;
        }

        if (MouseUtils.isHovering(mouseX, mouseY, replacementX, GAP, REPLACEMENT_WIDTH, BOX_HEIGHT)) {
            if (phraseInput.isWriting() && selected == this) {
                commitPhrase();
            }
            selected = this;
            replacementInput.setWriting(true);
            phraseInput.setWriting(false);
            replacementInput.click((float) (mouseX - (replacementX + 5f)), button);
        } else if (replacementInput.isWriting() && selected == this) {
            commitReplacement();
            replacementInput.setWriting(false);
            selected = null;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && MouseUtils.isHovering(mouseX, mouseY, enabledX, GAP, BUTTON_WIDTH, BOX_HEIGHT)) {
            visualWord.toggle();
            VisualWords.getInstance().getData().save();
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && MouseUtils.isHovering(mouseX, mouseY, deleteX, GAP, DELETE_WIDTH, BOX_HEIGHT)) {
            VisualWords.getInstance().getData().getValue().remove(phrase);
            VisualWords.getInstance().getData().save();
            return true;
        }

        return false;
    }

    public boolean charTyped(char typedChar) {
        boolean handled = false;
        if (phraseInput.isWriting()) {
            handled = phraseInput.charTyped(typedChar);
        }

        if (replacementInput.isWriting()) {
            handled = replacementInput.charTyped(typedChar) || handled;
        }

        return handled;
    }

    public boolean keyTyped(KeyEvent event) {
        int key = event.key();

        if (phraseInput.isWriting()) {
            if (key == InputConstants.KEY_ESCAPE) {
                phraseInput.setValue(phrase);
                phraseInput.setWriting(false);
                selected = null;
                return true;
            }
            if (key == InputConstants.KEY_RETURN) {
                commitPhrase();
                phraseInput.setWriting(false);
                selected = null;
                return true;
            }
            if (phraseInput.keyTyped(event)) {
                return true;
            }
        }

        if (replacementInput.isWriting()) {
            if (key == InputConstants.KEY_ESCAPE) {
                replacementInput.setValue(visualWord.replacement.getString());
                replacementInput.setWriting(false);
                selected = null;
                return true;
            }
            if (key == InputConstants.KEY_RETURN) {
                commitReplacement();
                replacementInput.setWriting(false);
                selected = null;
                return true;
            }
            return replacementInput.keyTyped(event);
        }

        return false;
    }

    public void render(GuiGraphicsExtractor gfx, float x, float y, double mouseX, double mouseY) {
        Render2D.rectOutline(gfx, x, y, WIDTH, HEIGHT, FatalityColors.GROUP_OUTLINE, FatalityColors.GROUP_FILL, 1F, 1F);

        float replacementX = x + GAP + PHRASE_WIDTH + GAP;
        float enabledX = replacementX + REPLACEMENT_WIDTH + GAP;
        float deleteX = enabledX + BUTTON_WIDTH + GAP;

        Color phraseColor = inputColor(phraseInput.isWriting(), MouseUtils.isHovering(mouseX, mouseY, x + GAP, y + GAP, PHRASE_WIDTH, BOX_HEIGHT));
        Render2D.rect(gfx, x + GAP, y + GAP, PHRASE_WIDTH, BOX_HEIGHT, phraseColor);
        phraseInput.render(gfx, x + GAP + 5f, y + HEIGHT / 2 - 4);

        Color replacementColor = inputColor(replacementInput.isWriting(), MouseUtils.isHovering(mouseX, mouseY, replacementX, y + GAP, REPLACEMENT_WIDTH, BOX_HEIGHT));
        Render2D.rect(gfx, replacementX, y + GAP, REPLACEMENT_WIDTH, BOX_HEIGHT, replacementColor);
        replacementInput.render(gfx, replacementX + 5f, y + HEIGHT / 2 - 4);

        boolean enabledHovered = MouseUtils.isHovering(mouseX, mouseY, enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT);
        Color enabledColor = visualWord.enabled
                ? (enabledHovered ? FatalityColors.SELECTED.darker() : FatalityColors.SELECTED)
                : (enabledHovered ? FatalityColors.GROUP_OUTLINE.brighter() : FatalityColors.GROUP_OUTLINE);

        Render2D.rect(gfx, enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT, enabledColor, 5F);
        String enabledText = visualWord.enabled ? "On" : "Off";
        Fonts.text(gfx, Fonts.JOSEFIN, enabledText, enabledX + (BUTTON_WIDTH - Fonts.width(enabledText, 12)) / 2f, y + GAP + Fonts.height(12), 12, FatalityColors.TEXT);

        boolean deleteHovered = MouseUtils.isHovering(mouseX, mouseY, deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT);
        Render2D.rect(gfx, deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT, deleteHovered ? FatalityColors.SELECTED.brighter() : FatalityColors.SELECTED, 5F);
        String deleteText = "Delete";
        Fonts.text(gfx, Fonts.JOSEFIN, deleteText, deleteX + (DELETE_WIDTH - Fonts.width(deleteText, 12)) / 2f,
                y + GAP + Fonts.height(12), 12, FatalityColors.TEXT);
    }

    public void commitPendingEdits() {
        if (phraseInput.isWriting()) {
            commitPhrase();
            phraseInput.setWriting(false);
        }

        if (replacementInput.isWriting()) {
            commitReplacement();
            replacementInput.setWriting(false);
        }

        if (selected == this) {
            selected = null;
        }
    }

    private void commitPhrase() {
        String nextPhrase = phraseInput.getValue();
        nextPhrase = nextPhrase.trim();
        phraseInput.setValue(nextPhrase);

        if (nextPhrase.isBlank() || nextPhrase.equals(phrase)) {
            phraseInput.setValue(phrase);
            return;
        }

        if (VisualWords.getInstance().getData().getValue().containsKey(nextPhrase)) {
            phraseInput.setValue(phrase);
            return;
        }

        VisualWords.getInstance().getData().getValue().remove(phrase);
        VisualWords.getInstance().getData().getValue().put(nextPhrase, visualWord);
        phrase = nextPhrase;
        VisualWords.getInstance().getData().save();
    }

    private void commitReplacement() {
        visualWord.replacement = Component.literal(replacementInput.getValue());
        VisualWords.getInstance().getData().save();
    }

    private Color inputColor(boolean writing, boolean hovering) {
        if (writing) {
            return FatalityColors.WRITING_TEXT;
        }
        if (hovering) {
            return FatalityColors.HOVERING_TEXT;
        }
        return FatalityColors.INPUT_TEXT;
    }
}