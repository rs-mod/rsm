package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// PLEASE DON'T CRASH ME ANYMORE :(
@Getter
public class TextInput implements Accessor {
    private String value;
    private final int textSize;
    private Float fontHeight = null;
    private int selection = 0;
    private int pipe = 0;
    private float pipePos = 0f;
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private String lastSavedText = "";
    private long lastClick = 0;
    private int clickCount = 1;
    private final boolean secure;

    private static final String STRING_ALLOWED = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_=+[]{};:'\",.<>/?\\|`~!@#$%^&*() ";
    private static final String NUMBER_ALLOWED = "0123456789.kmbKMB";

    private final String allowed;

    private final Pair<Float, Float> selectionPos = new Pair<>(0f, 0f);

    private final int maxLength;

    public TextInput(String value, int textSize, String allowed, int maxLength, boolean secure) {
        this.value = value;
        this.textSize = textSize;
        this.allowed = allowed;
        this.maxLength = maxLength;
        this.secure = secure;
        saveState();
    }

    public TextInput(String value, int textSize, boolean number, boolean secure) {
        this(value, textSize, number ? NUMBER_ALLOWED : STRING_ALLOWED, 32, secure);
    }

    public TextInput(String value, int textSize, boolean number, int maxLength) {
        this(value, textSize, number ? NUMBER_ALLOWED : STRING_ALLOWED, maxLength, false);
    }

    public TextInput(String value, int textSize, boolean number) {
        this(value, textSize, number ? NUMBER_ALLOWED : STRING_ALLOWED, 32, false);
    }

    public void setValue(String newValue) {
        value = newValue;
        saveState();
    }

    public void render(float x, float y, boolean writing) {
        if (this.fontHeight == null) {
            this.fontHeight = NVGUtils.getTextHeight(this.textSize, NVGUtils.JOSEFIN);
        }

        String text = secure && !writing ? new String(new char[value.length()]).replace('\0', '*') : value;
        NVGUtils.drawTextShadow(text, x, y, 12, Colour.WHITE, NVGUtils.JOSEFIN);

        if (writing) {
            float f = fontHeight / 4f;
            if (System.currentTimeMillis() / 500 % 2 == 0) {
                NVGUtils.drawLine(x + pipePos, y - f, x + pipePos, y + (f * 4), 1, FatalityColours.PIPE);
            }

            if (!Objects.equals(selectionPos.getFirst(), selectionPos.getSecond())) {
                float start = x + selectionPos.getFirst();
                NVGUtils.drawRect(start, y - f, selectionPos.getSecond(), f * 5, FatalityColours.HIGHLIGHT);
            }
        }
    }

    public void click(float relX, int button) {
        if (button != 0) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < 200) {
            clickCount++;
        } else {
            clickCount = 1;
        }
        lastClick = now;

        switch (clickCount) {
            case 1 -> {
                pipeFromMouse(relX);
                resetSelection();
            }
            case 2 -> selectWord();
            case 3 -> selectAll();
            case 4 -> clickCount = 0;
        }
    }

    public boolean keyTyped(KeyEvent input) {
        boolean ret;

        ret = switch (input.key()) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (selection != pipe) {
                    deleteSelection();
                } else if (input.hasControlDown()) {
                    int space = getPreviousSpace();
                    if (space == -1) yield false;
                    value = removeRange(value, space, pipe);
                    pipe -= pipe > space ? pipe - space : 0;
                } else if (pipe != 0) {
                    value = removeRange(value, pipe - 1, pipe);
                    pipe--;
                }
                resetSelection();
                yield selection != pipe || input.hasControlDown() || pipe != 0;
            }

            case GLFW.GLFW_KEY_DELETE -> {
                if (selection != pipe) {
                    deleteSelection();
                } else if (input.hasControlDown()) {
                    int space = getNextSpace();
                    if (space == -1) yield false;
                    value = removeRange(value, space, pipe);
                    pipe -= pipe > space ? pipe - space : 0;
                } else if (pipe != value.length()) {
                    value = removeRange(value, pipe, pipe + 1);
                    pipe = Math.min(pipe, value.length());
                }
                resetSelection();
                yield selection != pipe || input.hasControlDown() || pipe != value.length();
            }

            case GLFW.GLFW_KEY_RIGHT -> {
                if (pipe != value.length()) {
                    pipe = input.hasControlDown() ? getNextSpace() : pipe + 1;
                    if (!input.hasShiftDown()) selection = pipe;
                    yield true;
                } else {
                    yield false;
                }
            }

            case GLFW.GLFW_KEY_LEFT -> {
                if (pipe != 0) {
                    pipe = input.hasControlDown() ? getPreviousSpace() : pipe - 1;
                    if (!input.hasShiftDown()) selection = pipe;
                    yield true;
                } else {
                    yield false;
                }
            }

            case GLFW.GLFW_KEY_HOME -> {
                pipe = 0;
                if (!input.hasShiftDown()) selection = pipe;
                yield true;
            }

            case GLFW.GLFW_KEY_END -> {
                pipe = value.length();
                if (!input.hasShiftDown()) selection = pipe;
                yield true;
            }
            default -> false;
        };

        if (input.hasControlDown() && !input.hasShiftDown()) {
            ret = switch (input.key()) {
                case GLFW.GLFW_KEY_V -> {
                    insert(mc.keyboardHandler.getClipboard());
                    yield true;
                }

                case GLFW.GLFW_KEY_C -> {
                    if (pipe != selection) {
                        mc.keyboardHandler.setClipboard(substring(value, selection, pipe));
                        yield true;
                    } else yield false;
                }

                case GLFW.GLFW_KEY_X -> {
                    if (pipe != selection) {
                        mc.keyboardHandler.setClipboard(substring(value, selection, pipe));
                        resetSelection();
                        yield true;
                    } else yield false;
                }

                case GLFW.GLFW_KEY_A -> {
                    selection = 0;
                    pipe = value.length();
                    updateSelection();
                    yield true;
                }

                case GLFW.GLFW_KEY_W -> {
                    selectWord();
                    yield true;
                }

                case GLFW.GLFW_KEY_Z -> {
                    undo();
                    yield true;
                }

                case GLFW.GLFW_KEY_Y -> {
                    redo();
                    yield true;
                }
                default -> false;
            };
        }

        updateSelection();
        return ret;
    }

    public boolean charTyped(char c) {
        if (allowed.indexOf(c) != -1 && value.length() < maxLength) {
            insert(String.valueOf(c));
            return true;
        }
        return false;
    }

    private void undo() {
        if (historyIndex <= 0) return;
        historyIndex--;
        value = history.get(historyIndex);
        pipe = value.length();
        selection = pipe;
        lastSavedText = value;
    }

    private void redo() {
        if (historyIndex >= history.size() - 1) return;

        historyIndex++;
        value = history.get(historyIndex);
        pipe = value.length();
        selection = pipe;
        lastSavedText = value;
    }

    private String substring(String s, int a, int b) {
        int min = Math.max(Math.min(a, b), 0);
        int max = Math.min(Math.max(a, b), s.length());
        return s.substring(min, max);
    }

    private String substring(String s, int a) {
        return substring(s, a, s.length());
    }

    private void insert(String string) {
        if (pipe != selection) {
            value = removeRange(value,  pipe, selection);
            pipe = Math.min(selection, pipe);
        }
        int vl = value.length();
        value = substring(value, 0, pipe) + string + substring(value, pipe);
        if (value.length() > maxLength) {
            int sub = maxLength - value.length();
            String newString = substring(string, 0, Math.min(0, string.length() - sub));
            value = substring(value, 0, pipe) + newString + substring(value, pipe);
            if (value.length() != vl) pipe += newString.length();
        } else {
            if (value.length() != vl) pipe += string.length();
        }
        resetSelection();
        updateSelection();
        saveState();
    }

    private int getPreviousSpace() {
        for (int i = pipe; i > 0; i--) {
            char c = value.charAt(i - 1);
            if (i != pipe && Character.isWhitespace(c)) return i;
        }
        return 0;
    }

    private int getNextSpace() {
        for (int i = pipe; i < value.length(); i++) {
            char c = value.charAt(i);
            if (i != pipe && Character.isWhitespace(c)) return i;
        }
        return value.length();
    }

    private void deleteSelection() {
        if (pipe == selection) return;
        value = removeRange(value, selection, pipe);
        pipe = Math.min(pipe, selection);
        saveState();
    }

    private String removeRange(String s, int a, int b) {
        int min = Math.min(Math.max(Math.min(a, b), 0), s.length());
        int max = Math.max(Math.min(Math.max(a, b), s.length()), 0);
        return new StringBuilder(s).delete(min, max).toString();
    }


    private void resetSelection() {
        selection = pipe;
        updateSelection();
    }

    private void pipeFromMouse(float mx) {
        var currWidth = 0f;
        var newPipe = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            float w = width(String.valueOf(c));
            if ((currWidth + w / 2) > mx) break;
            currWidth += w;
            newPipe = i + 1;
        }
        pipe = newPipe;
        updateSelection();
    }

    private void selectAll() {
        selection = 0;
        pipe = value.length();
        updateSelection();
    }

    private void saveState() {
        if (Objects.equals(value, lastSavedText)) return;

        if (historyIndex < history.size() - 1) history.subList(historyIndex + 1, history.size()).clear();

        history.add(value);
        historyIndex = history.size() - 1;
        lastSavedText = value;
    }

    public void updateSelection() {
        if (value.length() > maxLength) {
            value = substring(value, 0, maxLength);
            pipe = value.length();
        }

        pipePos = width(substring(value, 0, pipe));
        if (selection == pipe) {
            selectionPos.set(0f, 0f);
            return;
        }

        int min = Math.min(selection, pipe);
        int max = Math.max(selection, pipe);

        String before = substring(value, 0, min);
        String between = substring(value, min, max);

        float start = width(before);
        float end = width(between);
        selectionPos.set(start, end);
    }

    private float width(String s) {
        return NVGUtils.getTextWidth(s, textSize, NVGUtils.JOSEFIN);
    }

    private void selectWord() {
        var start = pipe;
        var end = pipe;
        while (start > 0 && !Character.isAlphabetic(value.charAt(start - 1))) start--;
        while (end < value.length() && !Character.isWhitespace(value.charAt(end))) end++;

        selection = start;
        pipe = end;
        updateSelection();
    }
}
