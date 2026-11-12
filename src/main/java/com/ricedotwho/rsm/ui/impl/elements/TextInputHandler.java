package com.ricedotwho.rsm.ui.impl.elements;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.render.render2d.Corners;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.UndoStack;
import com.ricedotwho.rsm.ui.api.*;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import com.ricedotwho.rsm.utils.mouse.CursorTypes;
import com.ricedotwho.rsm.utils.mouse.MouseUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ricedotwho.rsm.type.Accessor.mc;


@SuppressWarnings("unused")
public class TextInputHandler extends Node {
    public static class Builder extends UiElement.Builder<Builder> {
        private Supplier<String> textSupplier;
        private Consumer<String> textConsumer;
        private String placeHolder = "";
        private Runnable onUnlisten;
        private int maxCharacters = 35;
        private Color textColor = Color.fromHex(0xFFFFFF);
        private Color highlightColor = Color.fromHex(0x3366FF, 0.4F);
        private Color placeHolderColor = Color.fromHex(0x808080);
        private TextAlignment align = TextAlignment.CenterLeft;
        private FontSizeSupplier fontSize = Palette.fontSize;
        private Supplier<String> fontSupplier;
        private boolean shadow;
        private String forbiddenCharacters = null;
        private Corners rounding = Corners.NONE;
        private float thickness = -1f;
        private Color outlineColor = null;
        public Builder() {}

        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new Corners(topLeft, topRight, bottomLeft, bottomRight);
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new Corners(radius);
            return this;
        }

        public Builder maxCharacters(int maxCharacters) {
            this.maxCharacters = maxCharacters;
            return this;
        }

        public Builder outline(float thickness, Color outlineColor) {
            this.thickness = thickness;
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder textSupplier(Supplier<String> v) { this.textSupplier = v; return this; }
        public Builder textConsumer(Consumer<String> v) { this.textConsumer = v; return this; }
        public Builder placeHolder(String v) { this.placeHolder = v; return this; }
        public Builder onUnlisten(Runnable v) { this.onUnlisten = v; return this; }
        public Builder textColor(Color v) { this.textColor = v; return this; }
        public Builder highlightColor(Color v) { this.highlightColor = v; return this; }
        public Builder placeHolderColor(Color v) { this.placeHolderColor = v; return this; }
        public Builder textAlign(TextAlignment v) { this.align = v; return this; }
        public Builder fontSize(FontSizeSupplier v) { this.fontSize = v; return this; }
        public Builder fontSupplier(Supplier<String> v) { this.fontSupplier = v; return this; }
        public Builder shadow(boolean v) { this.shadow = v; return this; }
        public Builder forbiddenCharacters(String allowedCharacters) { this.forbiddenCharacters = allowedCharacters; return this; }


        public TextInputHandler build() {
            Objects.requireNonNull(textSupplier, "textSupplier");
            Objects.requireNonNull(textConsumer, "textConsumer");
            Objects.requireNonNull(align, "align");
            Objects.requireNonNull(fontSupplier, "fontSupplier");
            return new TextInputHandler(
                    buildYogaNode(), textSupplier, textConsumer, placeHolder, onUnlisten,
                    textColor, highlightColor, placeHolderColor, align, fontSize, fontSupplier, shadow, forbiddenCharacters,
                    color, rounding, thickness, outlineColor, maxCharacters
            );
        }
    }

    @Nullable
    public String forbiddenCharacters;

    private static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }
    private static boolean isBlank(String s) {
        if (s.isEmpty()) return true;
        for (int i = 0; i < s.length(); i++) {
            if (!isWhitespace(s.charAt(i))) return false;
        }
        return true;
    }

    private final @NotNull Corners rounding;
    private final float thickness;
    private final Color outlineColor;

    @Setter
    private Consumer<String> textConsumer;
    @Setter
    private Supplier<String> textSupplier;
    private final String placeHolder;
    @Setter
    private Runnable onUnlisten;
    private final Color textColor;
    private final Color highlightColor;
    private final Color placeHolderColor;
    private final TextAlignment align;
    private final FontSizeSupplier fontSize;
    private final Supplier<String> fontSupplier;
    private final boolean shadow;
    private final int maxCharacters;
    private long lastClickTime = 0L;
    @Getter
    private boolean listening = false;

    private boolean dragging = false;
    private int clickCount = 1;
    private int caret;

    private long caretBlinkTime = System.currentTimeMillis();
    private float caretX = 0f;
    private int selection;

    private float selectionWidth = 0f;

    private float textOffset = 0f;
    private final UndoStack<String> history = new UndoStack<>();

    private float previousMousePos = Float.NaN;

    private boolean refreshPosition = false;

    private boolean keyTyped = false;

    private float previousMouseXCursor = -1f;
    private float previousMouseYCursor = -1f;
    protected TextInputHandler(
            long yogaNode, Supplier<String> textSupplier, Consumer<String> textConsumer,
            String placeHolder, Runnable onUnlisten, Color textColor, Color highlightColor,
            Color placeHolderColor, TextAlignment align, FontSizeSupplier fontSize,
            Supplier<String> fontSupplier, boolean shadow, @Nullable String forbiddenCharacters,
            Color color, @NonNull Corners rounding, float thickness, Color outlineColor,
            int maxCharacters
    ) {
        Objects.requireNonNull(rounding);
        super(yogaNode, color);
        this.textSupplier = textSupplier;
        this.textConsumer = textConsumer;
        this.placeHolder = placeHolder;
        this.onUnlisten = onUnlisten;
        this.textColor = textColor;
        this.highlightColor = highlightColor;
        this.placeHolderColor = placeHolderColor;
        this.align = align;
        this.fontSize = fontSize;
        this.fontSupplier = fontSupplier;
        this.shadow = shadow;
        this.forbiddenCharacters = forbiddenCharacters;
        this.rounding = rounding;
        this.thickness = thickness;
        this.outlineColor = outlineColor;
        this.maxCharacters = maxCharacters;

        initState();
    }

    protected void initState() {
        int len = (textSupplier != null) ? getText().length() : 0;
        this.caret = len;
        this.selection = len;

        if (textSupplier != null) saveState();
    }

    /**
     * Must be called inside a frame to be accurate because of something with NanoVG.
     */
    public float getDrawX() {
        float contentWidth = layoutContentWidth();
        float textWidth = Fonts.width(fontSupplier.get(), getText(), fontSize.getFontSize());
        if (textWidth > contentWidth) return 0f;
        float drawOrigin = align.calculateX(contentWidth);
        return drawOrigin - align.calculateX(textWidth);
    }

    /**
     * Must be called inside a frame to be accurate because of something with NanoVG.
     */
    public float getDrawY() {
        float drawOrigin = align.calculateY(layoutContentHeight());
        return drawOrigin - align.calculateY(Fonts.height(fontSupplier.get(), fontSize.getFontSize()));
    }

    @Override
    public void mouseClickedUncancelable(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!listening || button != InputConstants.MOUSE_BUTTON_LEFT || isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return;
        resetState();
        if (onUnlisten != null) onUnlisten.run();
    }

    @Override
    public boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;
        listening = true;
        dragging = true;
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickTime < 200) { clickCount += 1; } else { clickCount = 1; }
        lastClickTime = currentTimeMillis;

        switch (clickCount) {
            case 1 -> {
                caretFromMouse(getLocalMouseX(originX(parentX), mouseX), false);
                if (!Gui.hasShiftDown()) {
                    selection = caret;
                    updateCaretPosition(false);
                }
            }
            case 2 -> selectWord();
            case 3 -> selectAll();
            case 4 -> clickCount = 0;
            default -> { }
        }

        return true;
    }

    @Override
    public void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        dragging = false;
    }


    @Override
    public boolean keyPressed(int keyCode, float mouseX, float mouseY, float scrollY) {
        if (!listening) return false;
        previousMouseXCursor = mouseX;
        previousMouseYCursor = mouseY;
        keyTyped = true;

        boolean returnValue;

        if (keyCode == InputConstants.KEY_BACKSPACE) {
            if (selection != caret) {
                deleteSelection();
            } else if (Gui.hasControlDown()) {
                int previousSpace = getPreviousSpace();
                textConsumer.accept(removeRangeSafe(getText(), previousSpace, caret));
                int delta = caret > previousSpace ? caret - previousSpace : 0;
                setCaret(caret - delta);
            } else if (caret != 0) {
                int start = previousClusterBoundary(caret);
                textConsumer.accept(removeRangeSafe(getText(), start, caret));
                setCaret(start);
            }
            clearSelection();
            returnValue = selection != caret || Gui.hasControlDown() || caret != 0;

        } else if (keyCode == InputConstants.KEY_DELETE) {
            if (selection != caret) {
                deleteSelection();
            } else if (Gui.hasControlDown()) {
                int nextSpace = getNextSpace();
                textConsumer.accept(removeRangeSafe(getText(), caret, nextSpace));
                setCaret(Math.min(caret, nextSpace));
            } else if (caret != getText().length()) {
                int end = nextClusterBoundary(caret);
                textConsumer.accept(removeRangeSafe(getText(), caret, end));
                setCaret(caret);
            }
            clearSelection();
            returnValue = selection != caret || Gui.hasControlDown() || caret != getText().length();

        } else if (keyCode == InputConstants.KEY_RIGHT) {
            if (caret != getText().length()) {
                setCaret(Gui.hasControlDown() ? getNextSpace() : nextClusterBoundary(caret));
                if (!Gui.hasShiftDown()) selection = caret;
                returnValue = true;
            } else {
                returnValue = false;
            }

        } else if (keyCode == InputConstants.KEY_LEFT) {
            if (caret != 0) {
                setCaret(Gui.hasControlDown() ? getPreviousSpace() : previousClusterBoundary(caret));
                if (!Gui.hasShiftDown()) selection = caret;
                returnValue = true;
            } else {
                returnValue = false;
            }

        } else if (keyCode == InputConstants.KEY_HOME) {
            setCaret(0);
            if (!Gui.hasShiftDown()) selection = caret;
            returnValue = true;

        } else if (keyCode == InputConstants.KEY_END) {
            setCaret(getText().length());
            if (!Gui.hasShiftDown()) selection = caret;
            returnValue = true;

        } else if (keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_RETURN) {
            resetState();
            if (onUnlisten != null) onUnlisten.run();
            returnValue = true;

        } else {
            if (Gui.hasControlDown() && Gui.hasShiftDown()) {
                if (keyCode == InputConstants.KEY_Z) {
                    redo();
                    returnValue = true;
                } else {
                    returnValue = false;
                }
            } else if (Gui.hasControlDown() && !Gui.hasShiftDown()) {
                if (keyCode == InputConstants.KEY_V) {
                    String clipboard = mc.keyboardHandler.getClipboard();
                    if (forbiddenCharacters != null) clipboard = clipboard.replaceAll(forbiddenCharacters, "");

                    if (!clipboard.isBlank()) insert(clipboard);
                    returnValue = true;
                } else if (keyCode == InputConstants.KEY_C) {
                    if (caret != selection) {
                        mc.keyboardHandler.setClipboard(substringSafe(getText(), caret, selection));
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                } else if (keyCode == InputConstants.KEY_X) {
                    if (caret != selection) {
                        mc.keyboardHandler.setClipboard(substringSafe(getText(), caret, selection));
                        deleteSelection();
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                } else if (keyCode == InputConstants.KEY_A) {
                    selection = 0;
                    setCaret(getText().length());
                    returnValue = true;
                } else if (keyCode == InputConstants.KEY_W) {
                    selectWord();
                    returnValue = true;
                } else if (keyCode == InputConstants.KEY_Z) {
                    undo();
                    returnValue = true;
                } else if (keyCode == InputConstants.KEY_Y) {
                    redo();
                    returnValue = true;
                } else {
                    returnValue = false;
                }
            } else {
                returnValue = false;
            }
        }

        updateCaretPosition(false);
        return returnValue;
    }

    @Override
    public boolean charTyped(String string, float mouseX, float mouseY, float scrollY) {
        if (!listening) return false;

        if (forbiddenCharacters != null) string = string.replaceAll(forbiddenCharacters, "");
        if (string.isEmpty()) return true;

        previousMouseXCursor = mouseX;
        previousMouseYCursor = mouseY;
        keyTyped = true;

        insert(string);
        return true;
    }

    public void beginEditing() { beginEditing(false); }

    public void beginEditing(boolean selectAll) {
        textOffset = 0f;
        clearSelection();
        listening = true;
        if (selectAll) {
            selectAll();
        } else {
            caret = 0;
            selection = caret;
        }
        updateCaretPosition(false);
    }

    public void resetState() {
        listening = false;
        textOffset = 0f;
        clearSelection();
    }

    @Override
    protected void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        RectangleNode.renderRectangleNode(gfx,
                parentX, parentY, mouseX, mouseY,
                this.color, outlineColor,
                layoutLeft(), layoutTop(), layoutWidth(), layoutHeight(),
                rounding, thickness
        );

        gfx.push();
        if (listening) {
            if (previousMouseXCursor != mouseX || previousMouseYCursor != mouseY) {
                keyTyped = false;
            }
            if (keyTyped) MouseUtils.requestCursor(CursorTypes.HIDDEN);
        }

        final var localMouseX = getLocalMouseX(originX(parentX), mouseX);
        if (previousMousePos != localMouseX && dragging) caretFromMouse(localMouseX, true);
        previousMousePos = localMouseX;

        if (refreshPosition) {
            updateCaretPosition(true);
            refreshPosition = false;
        }

        var originX = originX(parentX) + layoutPaddingLeft();
        var originY = originY(parentY) + layoutPaddingTop();
        gfx.pose().translate(originX, originY);

        val width = layoutWidth();
        val height = layoutHeight();
        gfx.pushScissor(0, 0, width, height);

        var drawX = getDrawX();
        var drawY = getDrawY();

        gfx.pose().translate(-textOffset, 0f);
        if (textOffset != 0) logger.info(textOffset);
        renderText(gfx);

        gfx.pose().translate(textOffset, 0f);

        val textHeight = Fonts.height(fontSupplier.get(), fontSize.getFontSize());
        val renderHeight = textHeight * 1.25f;
        val difference = (renderHeight - textHeight) / 2;

        gfx.pose().translate(drawX + caretX - textOffset, drawY - difference);

        if (selectionWidth != 0f) {
            Render2D.rect(gfx, 0, 0, selectionWidth, renderHeight, highlightColor);
        }

        if (listening) {
            long time = System.currentTimeMillis();
            if (time - caretBlinkTime < 500) {
                drawLine(gfx,
                    renderHeight,
                    textColor
                );
            } else if (time - caretBlinkTime > 1000) {
                caretBlinkTime = time;
            }
        }

        gfx.popScissor();
        gfx.pop();
    }

    @Override
    protected void hover() {
        MouseUtils.requestCursor(CursorTypes.IBEAM);
    }


    private String getText() {
        return textSupplier.get();
    }

    protected void setCaretAndSelection() {
        setCaret(1);
        selection = caret;
    }

    private void setCaret(int value) {
        if (caret == value) return;
        caret = Math.clamp(value, 0, getText().length());

        caretBlinkTime = System.currentTimeMillis();
    }

    private int nextClusterBoundary(int pos) {
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(getText());
        int b = it.following(pos);
        return b == BreakIterator.DONE ? getText().length() : b;
    }

    private int previousClusterBoundary(int pos) {
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(getText());
        int b = it.preceding(pos);
        return b == BreakIterator.DONE ? 0 : b;
    }

    private float getLocalMouseX(float originX, float mouseX) {
        return mouseX - textOffset - getDrawX() - originX - layoutPaddingLeft();
    }

    private void renderText(GuiGraphicsExtractor gfx) {
        float width = layoutContentWidth();
        float height = layoutContentHeight();
        float textWidth = Fonts.width(fontSupplier.get(), getText(), fontSize.getFontSize());
        boolean overflowing = textWidth > width;

        float x = overflowing ? 0f : align.calculateX(width);
        float y = align.calculateY(height);

        if (overflowing) {
            Fonts.align(align);
        } else {
            align.setTextAlign();
        }

        var blank = isBlank(getText());
        var text = blank ? placeHolder : getText();

        Fonts.text(gfx, fontSupplier.get(), text, x, y + 1, fontSize.getFontSize(), blank ? placeHolderColor : textColor, shadow:shadow, shadowOffset:1F, shadowColour:TextNode.TEXT_SHADOW);
        Fonts.align(TextAlignment.TopLeft);
    }

    private void drawLine(GuiGraphicsExtractor gfx, float y, Color colour) {
        Render2D.line(gfx, 0, 0, 0, y, colour, 1F);
    }

    private void insert(String string) {
        if (caret != selection) {
            textConsumer.accept(removeRangeSafe(getText(), caret, selection));
            setCaret(Math.min(selection, caret));
        }
        val length = getText().length();
        val subLength = string.length() + length;
        if (subLength > maxCharacters) {
            string = string.substring(0, Math.min(maxCharacters - length, 0));
        }

        textConsumer.accept(substringSafe(getText(), 0, caret) + string + getText().substring(caret));
        if (length != getText().length()) caret += string.length();
        clearSelection();
        updateCaretPosition(false);
        saveState();
    }

    private void deleteSelection() {
        if (caret == selection) return;
        textConsumer.accept(removeRangeSafe(getText(), caret, selection));
        setCaret(Math.min(selection, caret));
        saveState();
    }

    private void caretFromMouse(float mouseX, boolean draw) {
        String t = getText();
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(t);

        float currentWidth = 0f;
        int newCaret = 0;
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String cluster = t.substring(start, end);
            float clusterWidth = Fonts.width(fontSupplier.get(), cluster, fontSize.getFontSize());
            if ((currentWidth + clusterWidth * 0.5f) > mouseX) break;
            currentWidth += clusterWidth;
            newCaret = end;
        }
        setCaret(newCaret);
        updateCaretPosition(draw);
    }

    private void updateCaretPosition(boolean draw) {
        if (!draw) {
            refreshPosition = true;
            return;
        }

        if (selection != caret) {
            selectionWidth = textWidth(substringSafe(getText(), selection, caret));
            if (selection <= caret) selectionWidth *= -1;
        } else {
            selectionWidth = 0f;
        }

        float contentWidth = layoutContentWidth();
        float fullTextWidth = textWidth(getText());

        if (fullTextWidth <= contentWidth) {
            textOffset = 0f;
            caretX = caret != 0 ? textWidth(substringSafe(getText(), 0, caret)) : 0f;
            return;
        }

        if (caret != 0) {
            float previousX = caretX;
            caretX = textWidth(substringSafe(getText(), 0, caret));
            if (previousX < caretX) {
                if (caretX - textOffset >= contentWidth) textOffset = caretX - contentWidth;
            } else {
                if (caretX - textOffset <= 0f) textOffset = textWidth(substringSafe(getText(), 0, caret - 1));
            }
            if (textOffset > 0 && fullTextWidth - textOffset < contentWidth) {
                textOffset = Math.max(fullTextWidth - contentWidth, 0);
            }
        } else {
            caretX = 0f;
            textOffset = 0f;
        }
    }

    private void clearSelection() {
        selection = caret;
        selectionWidth = 0f;
    }

    private void selectWord() {
        int start = caret;
        int end = caret;
        String text = getText();
        while (start > 0 && !isWhitespace(text.charAt(start - 1))) {
            start -= 1;
        }
        while (end < text.length() && !isWhitespace(text.charAt(end))) {
            end += 1;
        }

        selection = start;
        caret = end;
        updateCaretPosition(false);
    }

    private int getPreviousSpace() {
        int start = caret;
        String text = getText();
        while (start > 0) {
            if (start != caret && isWhitespace(text.charAt(start - 1))) break;
            start -= 1;
        }
        return start;
    }

    private int getNextSpace() {
        int end = caret;
        String text = getText();
        while (end < text.length()) {
            if (end != caret && isWhitespace(text.charAt(end))) break;
            end += 1;
        }
        return end;
    }

    private float textWidth(String text) {
        return Fonts.width(fontSupplier.get(), text, fontSize.getFontSize());
    }

    private void saveState() {
        String text = getText();
        if (!history.isEmpty() && text.equals(history.peek())) return;
        history.pushState(text);
    }

    private void selectAll() {
        selection = 0;
        caret = getText().length();
        updateCaretPosition(false);
    }

    private void undo() {
        if (history.isEmpty()) return;
        textConsumer.accept(history.undo());
        caret = getText().length();
        selection = caret;
    }

    private void redo() {
        if (history.isEmpty()) return;
        textConsumer.accept(history.redo());
        caret = getText().length();
        selection = caret;
    }

    private String substringSafe(String s, int from, int to) {
        int f = Math.max(Math.min(from, to), 0);
        int t = Math.max(to, from);
        if (t > s.length()) return s.substring(f);
        return s.substring(f, t);
    }

    private String removeRangeSafe(String s, int from, int to) {
        int lo = Math.min(from, to);
        int hi = Math.max(to, from);
        return s.substring(0, lo) + s.substring(hi); // removeRange(lo, hi): hi is exclusive
    }

    private String dropAt(String s, int at, int amount) {
        return removeRangeSafe(s, at, at + amount);
    }

}
