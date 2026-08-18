package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.UndoStack;
import com.ricedotwho.rsm.ui.api.*;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;

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
        private Color textColor = Color.fromHex(0xFFFFFF);
        private Color highlightColor = Color.fromHex(0x3366FF, 0.4F);
        private Color placeHolderColor = Color.fromHex(0x808080);
        private TextAlignment align = TextAlignment.CenterLeft;
        private FontSizeSupplier fontSize = Palette.fontSize;
        private FontSupplier fontSupplier;
        private boolean shadow;
        private String forbiddenCharacters = null;
        private float[] rounding;
        private float thickness = -1f;
        private Color outlineColor = null;
        public Builder() {}

        public Builder rounding(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.rounding = new float[] { topLeft, topRight, bottomRight, bottomLeft };
            return this;
        }

        public Builder rounding(float radius) {
            this.rounding = new float[] { radius, radius, radius, radius };
            return this;
        }

        public Builder outline(float thickness, Color outlineColor) {
            this.thickness = thickness;
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder textSupplier(Supplier<String> v) { this.textSupplier = v; return this; }
        public Builder textConsumer(Consumer<String> v) { this.textConsumer = v; return this; }
        public Builder placeHolder(String v)      { this.placeHolder = v; return this; }
        public Builder onUnlisten(Runnable v)     { this.onUnlisten = v; return this; }
        public Builder textColor(Color v)             { this.textColor = v; return this; }
        public Builder highlightColor(Color v)    { this.highlightColor = v; return this; }
        public Builder placeHolderColor(Color v)  { this.placeHolderColor = v; return this; }
        public Builder textAlign(TextAlignment v)     { this.align = v; return this; }
        public Builder fontSize(FontSizeSupplier v)          { this.fontSize = v; return this; }
        public Builder fontSupplier(FontSupplier v) { this.fontSupplier = v; return this; }
        public Builder shadow(boolean v)          { this.shadow = v; return this; }
        public Builder forbiddenCharacters(String allowedCharacters) { this.forbiddenCharacters = allowedCharacters; return this; }


        public TextInputHandler build() {
            Objects.requireNonNull(textSupplier, "textSupplier");
            Objects.requireNonNull(textConsumer, "textConsumer");
            Objects.requireNonNull(align, "align");
            Objects.requireNonNull(fontSupplier, "fontSupplier");
            return new TextInputHandler(
                    buildYogaNode(), textSupplier, textConsumer, placeHolder, onUnlisten,
                    textColor, highlightColor, placeHolderColor, align, fontSize, fontSupplier, shadow, forbiddenCharacters,
                    color, rounding, thickness, outlineColor
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

    private final float[] rounding; // null, or {topLeft, topRight, bottomRight, bottomLeft}
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
    private final FontSupplier fontSupplier;
    private final boolean shadow;
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
            FontSupplier fontSupplier, boolean shadow, @Nullable String forbiddenCharacters,
            Color color, float[] rounding, float thickness, Color outlineColor
    ) {
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
        float textWidth = NVGUtils.getTextWidth(getText(), fontSize.getFontSize(), fontSupplier.getFont());
        if (textWidth > contentWidth) return 0f;
        float drawOrigin = align.calculateX(contentWidth);
        return drawOrigin - align.calculateX(textWidth);
    }

    /**
     * Must be called inside a frame to be accurate because of something with NanoVG.
     */
    public float getDrawY() {
        float drawOrigin = align.calculateY(layoutContentHeight());
        return drawOrigin - align.calculateY(NVGUtils.getTextHeight(fontSize.getFontSize(), fontSupplier.getFont()));
    }

    @Override
    public void mouseClickedUncancelable(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!listening || button != 0 || isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return;
        resetState();
        if (onUnlisten != null) onUnlisten.run();
    }

    @Override
    public boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != 0 || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;

        listening = true;
        dragging = true;
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickTime < 200) { clickCount += 1; } else { clickCount = 1; }
        lastClickTime = currentTimeMillis;

        switch (clickCount) {
            case 1 -> {
                caretFromMouse(getLocalMouseX(originX(parentX), mouseX));
                if (!Gui.hasShiftDown()) {
                    selection = caret;
                    updateCaretPosition();
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

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
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

        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
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

        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (caret != getText().length()) {
                setCaret(Gui.hasControlDown() ? getNextSpace() : nextClusterBoundary(caret));
                if (!Gui.hasShiftDown()) selection = caret;
                returnValue = true;
            } else {
                returnValue = false;
            }

        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (caret != 0) {
                setCaret(Gui.hasControlDown() ? getPreviousSpace() : previousClusterBoundary(caret));
                if (!Gui.hasShiftDown()) selection = caret;
                returnValue = true;
            } else {
                returnValue = false;
            }

        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            setCaret(0);
            if (!Gui.hasShiftDown()) selection = caret;
            returnValue = true;

        } else if (keyCode == GLFW.GLFW_KEY_END) {
            setCaret(getText().length());
            if (!Gui.hasShiftDown()) selection = caret;
            returnValue = true;

        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            resetState();
            if (onUnlisten != null) onUnlisten.run();
            returnValue = true;

        } else {
            if (Gui.hasControlDown() && Gui.hasShiftDown()) {
                if (keyCode == GLFW.GLFW_KEY_Z) {
                    redo();
                    returnValue = true;
                } else {
                    returnValue = false;
                }
            } else if (Gui.hasControlDown() && !Gui.hasShiftDown()) {
                if (keyCode == GLFW.GLFW_KEY_V) {
                    String clipboard = mc.keyboardHandler.getClipboard();
                    if (forbiddenCharacters != null) clipboard = clipboard.replaceAll(forbiddenCharacters, "");

                    if (!clipboard.isBlank()) insert(clipboard);
                    returnValue = true;
                } else if (keyCode == GLFW.GLFW_KEY_C) {
                    if (caret != selection) {
                        mc.keyboardHandler.setClipboard(substringSafe(getText(), caret, selection));
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                } else if (keyCode == GLFW.GLFW_KEY_X) {
                    if (caret != selection) {
                        mc.keyboardHandler.setClipboard(substringSafe(getText(), caret, selection));
                        deleteSelection();
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                } else if (keyCode == GLFW.GLFW_KEY_A) {
                    selection = 0;
                    setCaret(getText().length());
                    returnValue = true;
                } else if (keyCode == GLFW.GLFW_KEY_W) {
                    selectWord();
                    returnValue = true;
                } else if (keyCode == GLFW.GLFW_KEY_Z) {
                    undo();
                    returnValue = true;
                } else if (keyCode == GLFW.GLFW_KEY_Y) {
                    redo();
                    returnValue = true;
                } else {
                    returnValue = false;
                }
            } else {
                returnValue = false;
            }
        }

        updateCaretPosition();
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
        updateCaretPosition();
    }

    public void resetState() {
        listening = false;
        textOffset = 0f;
        clearSelection();
    }

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        RectangleNode.renderRectangleNode(
                parentX, parentY, mouseX, mouseY,
                color, outlineColor,
                layoutLeft(), layoutTop(), layoutWidth(), layoutHeight(),
                rounding, thickness
        );

        NVGUtils.push();
        if (listening) {
            if (previousMouseXCursor != mouseX || previousMouseYCursor != mouseY) {
                keyTyped = false;
            }
            if (keyTyped) MouseUtils.hideCursor();
        }

        final var localMouseX = getLocalMouseX(originX(parentX), mouseX);
        if (previousMousePos != localMouseX && dragging) caretFromMouse(localMouseX);
        previousMousePos = localMouseX;

        if (refreshPosition) {
            updateCaretPosition();
            refreshPosition = false;
        }

        var originX = originX(parentX) + layoutPaddingLeft();
        var originY = originY(parentY) + layoutPaddingTop();
        NVGUtils.translate(originX, originY);

        val width = layoutWidth();
        val height = layoutHeight();
        NVGUtils.pushScissor(0f, 0f, width, height);

        var drawX = getDrawX();
        var drawY = getDrawY();

        NVGUtils.translate(-textOffset, 0f);
        if (textOffset != 0) logger.info(textOffset);
        renderText();

        NVGUtils.translate(textOffset, 0f);

        val textHeight = NVGUtils.getTextHeight(fontSize.getFontSize(), fontSupplier.getFont());
        val renderHeight = textHeight * 1.25f;
        val difference = (renderHeight - textHeight) / 2;


        NVGUtils.translate(drawX + caretX - textOffset, drawY - difference);

        if (selectionWidth != 0f) {
            NanoVG.nvgBeginPath(vg);

            NanoVG.nvgRect(
                vg,
                0,
                0,
                selectionWidth,
                renderHeight
            );

            NVGUtils.color(highlightColor);
            NanoVG.nvgFillColor(vg, NVGUtils.getNvgColor());
            NanoVG.nvgFill(vg);
        }

        if (listening) {
            long time = System.currentTimeMillis();
            if (time - caretBlinkTime < 500) {
                drawLine(
                    renderHeight,
                    textColor
                );
            } else if (time - caretBlinkTime > 1000) {
                caretBlinkTime = time;
            }
        }

        NVGUtils.popScissor();
        NVGUtils.pop();
    }

    @Override
    protected void hover() {
        MouseUtils.setIBeamCursor();
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

    private void renderText() {
        float width = layoutContentWidth();
        float height = layoutContentHeight();
        float textWidth = NVGUtils.getTextWidth(getText(), fontSize.getFontSize(), fontSupplier.getFont());
        boolean overflowing = textWidth > width;

        float x = overflowing ? 0f : align.calculateX(width);
        float y = align.calculateY(height);

        NanoVG.nvgFontFaceId(vg, NVGUtils.getFontID(fontSupplier.getFont()));
        NanoVG.nvgFontSize(vg, fontSize.getFontSize());

        if (overflowing) {
            int vertical = switch (align) {
                case TopLeft, TopMiddle, TopRight -> NanoVG.NVG_ALIGN_TOP;
                case CenterLeft, CenterMiddle, CenterRight -> NanoVG.NVG_ALIGN_MIDDLE;
                case BottomLeft, BottomMiddle, BottomRight -> NanoVG.NVG_ALIGN_BOTTOM;
            };
            NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_LEFT | vertical);
        } else {
            align.setTextAlign();
        }

        var blank = isBlank(getText());
        var text = blank ? placeHolder : getText();

        if (shadow) {
            NVGUtils.color(TextNode.TEXT_SHADOW);
            NanoVG.nvgFillColor(vg, NVGUtils.getNvgColor());
            NanoVG.nvgText(vg, x + 1.0F, y + 2.0F, text);
        }


        NVGUtils.color(blank ? placeHolderColor : textColor);

        NanoVG.nvgFillColor(vg, NVGUtils.getNvgColor());
        NanoVG.nvgText(vg, x, y + 1, text);
        NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP);
    }

    private void drawLine(float y, Color color) {
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, 0, 0);
        NanoVG.nvgLineTo(vg, 0, y);
        NanoVG.nvgStrokeWidth(vg, 1f);
        NVGUtils.color(color);
        NanoVG.nvgStrokeColor(vg, NVGUtils.getNvgColor());
        NanoVG.nvgStroke(vg);
    }

    private void insert(String string) {
        if (caret != selection) {
            textConsumer.accept(removeRangeSafe(getText(), caret, selection));
            setCaret(Math.min(selection, caret));
        }

        int textLength = getText().length();
        textConsumer.accept(substringSafe(getText(), 0, caret) + string + getText().substring(caret));
        if (textLength != getText().length()) caret += string.length();
        clearSelection();
        updateCaretPosition();
        saveState();
    }

    private void deleteSelection() {
        if (caret == selection) return;
        textConsumer.accept(removeRangeSafe(getText(), caret, selection));
        setCaret(Math.min(selection, caret));
        saveState();
    }

    private void caretFromMouse(float mouseX) {
        String t = getText();
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(t);

        float currentWidth = 0f;
        int newCaret = 0;
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String cluster = t.substring(start, end);
            float clusterWidth = NVGUtils.getTextWidth(cluster, fontSize.getFontSize(), fontSupplier.getFont());
            if ((currentWidth + clusterWidth * 0.5f) > mouseX) break;
            currentWidth += clusterWidth;
            newCaret = end;
        }
        setCaret(newCaret);
        updateCaretPosition();
    }

    private void updateCaretPosition() {
        if (!NVGUtils.isDrawing()) {
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
        updateCaretPosition();
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
        return NVGUtils.getTextWidth(text, fontSize.getFontSize(), fontSupplier.getFont());
    }

    private void saveState() {
        String text = getText();
        if (!history.isEmpty() && text.equals(history.peek())) return;
        history.pushState(text);
    }

    private void selectAll() {
        selection = 0;
        caret = getText().length();
        updateCaretPosition();
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
