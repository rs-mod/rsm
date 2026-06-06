package com.ricedotwho.rsm.ui.visualwords;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWord;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

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

        if (NVGUtils.isHovering(mouseX, mouseY, GAP, GAP, PHRASE_WIDTH, BOX_HEIGHT)) {
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

        if (NVGUtils.isHovering(mouseX, mouseY, replacementX, GAP, REPLACEMENT_WIDTH, BOX_HEIGHT)) {
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

        if (button == 0 && NVGUtils.isHovering(mouseX, mouseY, enabledX, GAP, BUTTON_WIDTH, BOX_HEIGHT)) {
            visualWord.toggle();
            VisualWords.getData().save();
        }

        if (button == 0 && NVGUtils.isHovering(mouseX, mouseY, deleteX, GAP, DELETE_WIDTH, BOX_HEIGHT)) {
            VisualWords.getData().getValue().remove(phrase);
            VisualWords.getData().save();
            return true;
        }

        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
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
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                phraseInput.setValue(phrase);
                phraseInput.setWriting(false);
                selected = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
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
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                replacementInput.setValue(visualWord.replacement.getString());
                replacementInput.setWriting(false);
                selected = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                commitReplacement();
                replacementInput.setWriting(false);
                selected = null;
                return true;
            }
            if (replacementInput.keyTyped(event)) {
                return true;
            }
        }

        return false;
    }

    public void render(GuiGraphics gfx, float x, float y, double mouseX, double mouseY) {
        NVGUtils.drawOutlineRect(x, y, WIDTH, HEIGHT, 1f, FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawRect(x, y, WIDTH, HEIGHT, FatalityColours.GROUP_FILL);

        float replacementX = x + GAP + PHRASE_WIDTH + GAP;
        float enabledX = replacementX + REPLACEMENT_WIDTH + GAP;
        float deleteX = enabledX + BUTTON_WIDTH + GAP;

        Colour phraseColour = inputColour(phraseInput.isWriting(), NVGUtils.isHovering(mouseX, mouseY, x + GAP, y + GAP, PHRASE_WIDTH, BOX_HEIGHT));
        NVGUtils.drawRect(x + GAP, y + GAP, PHRASE_WIDTH, BOX_HEIGHT, phraseColour);
        phraseInput.render(x + GAP + 5f, y + HEIGHT / 2f - 4f);

        Colour replacementColour = inputColour(replacementInput.isWriting(), NVGUtils.isHovering(mouseX, mouseY, replacementX, y + GAP, REPLACEMENT_WIDTH, BOX_HEIGHT));
        NVGUtils.drawRect(replacementX, y + GAP, REPLACEMENT_WIDTH, BOX_HEIGHT, replacementColour);
        replacementInput.render(replacementX + 5f, y + HEIGHT / 2f - 4f);

        boolean enabledHovered = NVGUtils.isHovering(mouseX, mouseY, enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT);
        Colour enabledColour = visualWord.enabled
                ? (enabledHovered ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED)
                : (enabledHovered ? FatalityColours.GROUP_OUTLINE.brighter() : FatalityColours.GROUP_OUTLINE);

        NVGUtils.drawRect(enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT, 5f, enabledColour);
        String enabledText = visualWord.enabled ? "On" : "Off";
        NVGUtils.drawText(enabledText, enabledX + (BUTTON_WIDTH - NVGUtils.getTextWidth(enabledText, 12, NVGUtils.JOSEFIN)) / 2f,
                y + GAP + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        boolean deleteHovered = NVGUtils.isHovering(mouseX, mouseY, deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT);
        NVGUtils.drawRect(deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT, 5f,
                deleteHovered ? FatalityColours.SELECTED.brighter() : FatalityColours.SELECTED);
        String deleteText = "Delete";
        NVGUtils.drawText(deleteText, deleteX + (DELETE_WIDTH - NVGUtils.getTextWidth(deleteText, 12, NVGUtils.JOSEFIN)) / 2f,
                y + GAP + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);
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

        if (VisualWords.getData().getValue().containsKey(nextPhrase)) {
            phraseInput.setValue(phrase);
            return;
        }

        VisualWords.getData().getValue().remove(phrase);
        VisualWords.getData().getValue().put(nextPhrase, visualWord);
        phrase = nextPhrase;
        VisualWords.getData().save();
    }

    private void commitReplacement() {
        visualWord.replacement = Component.literal(replacementInput.getValue());
        VisualWords.getData().save();
    }

    private Colour inputColour(boolean writing, boolean hovering) {
        if (writing) {
            return FatalityColours.WRITING_TEXT;
        }
        if (hovering) {
            return FatalityColours.HOVERING_TEXT;
        }
        return FatalityColours.INPUT_TEXT;
    }
}
