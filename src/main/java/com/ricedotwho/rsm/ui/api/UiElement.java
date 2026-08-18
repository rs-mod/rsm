package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public abstract class UiElement implements AutoCloseable, VGAccessor {
    private static final class Ansi {
        static final String RESET  = "\u001B[0m";
        static final String RED    = "\u001B[31m";
        static final String GREEN  = "\u001B[32m";
        static final String YELLOW = "\u001B[33m";
        static final String CYAN   = "\u001B[36m";
        static final String GRAY   = "\u001B[90m";
        static final String BOLD   = "\u001B[1m";

        static String wrap(String color, String text) {
            return color + text + RESET;
        }

        static String wrapBold(String color, String text) {
            return Ansi.BOLD + color + text + Ansi.RESET;
        }
    }

    public static final AtomicLong debugIdCounter = new AtomicLong();
    public static final ConcurrentHashMap<String, DebugNode> debugRegistry = new ConcurrentHashMap<>();
    public static final AtomicInteger yogaNodesCreated = new AtomicInteger(0);
    public static final AtomicInteger yogaNodesFreed = new AtomicInteger(0);

    private final String debugId;

    /** Debug-only bookkeeping entry. Strings only — never holds a reference to the actual UiElement. */
    public static final class DebugNode {
        final String className;
        final String creationStack;
        volatile boolean freed = false;
        volatile String freedStack = null;
        volatile String parentId = null;
        final java.util.List<String> childIds = new CopyOnWriteArrayList<>();

        DebugNode(String className, String creationStack) {
            this.className = className;
            this.creationStack = creationStack;
        }
    }

    private static String captureCreationStack() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        // skip getStackTrace/this constructor/UiElement's own frames, keep it short
        int shown = 0;
        for (int i = 3; i < trace.length && shown < 6; i++) {
            String cls = trace[i].getClassName();
            if (cls.startsWith("com.ricedotwho.rsm.ui.api.UiElement")) continue;
            sb.append("\n    at ").append(trace[i]);
            shown++;
        }
        return sb.toString();
    }

    protected final void markDebugFreed() {
        DebugNode entry = debugRegistry.get(debugId);
        if (entry != null) {
            entry.freed = true;
            entry.freedStack = captureCreationStack(); // reuse the same trimming logic
        }
    }

    private void addDebugEntry(UiElement child) {
        DebugNode parentEntry = debugRegistry.get(this.debugId);
        DebugNode childEntry = debugRegistry.get(child.debugId);
        if (parentEntry != null) parentEntry.childIds.add(child.debugId);
        if (childEntry != null) childEntry.parentId = this.debugId;
    }

    private void removeDebugEntry(UiElement child) {
        DebugNode parentEntry = debugRegistry.get(this.debugId);
        DebugNode childEntry = debugRegistry.get(child.debugId);
        if (parentEntry != null) parentEntry.childIds.remove(child.debugId);
        if (childEntry != null) childEntry.parentId = null;
    }

    public final String debugTreeString() {
        StringBuilder sb = new StringBuilder();
        appendDebugNode(sb, debugId, "", true, true);
        return sb.toString();
    }

    private static void appendDebugNode(StringBuilder sb, String id, String prefix, boolean isRoot, boolean isLast) {
        DebugNode entry = debugRegistry.get(id);
        if (entry == null) {
            sb.append(prefix).append(isRoot ? "" : (isLast ? "└─ " : "├─ "))
                    .append(Ansi.wrap(Ansi.RED, id + " [UNKNOWN]")).append('\n');
            return;
        }

        String status = entry.freed
                ? Ansi.wrap(Ansi.GRAY, "[FREED]")
                : Ansi.wrap(Ansi.GREEN, "[ALIVE]");
        String className = Ansi.wrap(Ansi.CYAN, entry.className);

        sb.append(prefix).append(isRoot ? "" : (isLast ? "└─ " : "├─ "))
                .append(className).append(" (").append(id).append(") ").append(status).append('\n');

        String childPrefix = prefix + (isRoot ? "" : (isLast ? "   " : "│  "));
        java.util.List<String> children = entry.childIds;
        for (int i = 0; i < children.size(); i++) {
            appendDebugNode(sb, children.get(i), childPrefix, false, i == children.size() - 1);
        }
    }

    public final void printDebugTree() {
        System.out.print(debugTreeString());
    }


    public static String debugLeakReport() {
        StringBuilder sb = new StringBuilder();
        int leaked = 0;
        for (Map.Entry<String, DebugNode> e : debugRegistry.entrySet()) {
            DebugNode entry = e.getValue();
            if (entry.freed) continue;
            leaked++;
            sb.append(Ansi.wrap(Ansi.YELLOW, e.getKey()))
                    .append(" (").append(Ansi.wrap(Ansi.CYAN, entry.className)).append(")")
                    .append(entry.parentId == null
                            ? Ansi.wrapBold(Ansi.RED, " [DETACHED/ROOT]")
                            : " parent=" + entry.parentId)
                    .append(Ansi.wrap(Ansi.GRAY, entry.creationStack))
                    .append('\n');
        }

        String header = leaked == 0
                ? Ansi.wrapBold(Ansi.GREEN, "Leaked nodes: 0\n")
                : Ansi.wrapBold(Ansi.RED, "Leaked nodes: " + leaked + "\n");

        sb.insert(0, header);
        return sb.toString();
    }


    public static void printLeaked() {
        System.out.println(debugLeakReport());

        val yogaNodesLeft = yogaNodesCreated.get() - yogaNodesFreed.get();
        if (yogaNodesLeft == 0) {
            System.out.println(Ansi.wrap(Ansi.GREEN, "No Dangling Nodes!"));
        } else {
            System.out.println(Ansi.wrap(Ansi.RED, "Dangling Nodes Left: " + yogaNodesLeft));
        }
        System.out.println();
    }

    protected UiElement() {
        this.debugId = this.getClass().getSimpleName() + "#" + debugIdCounter.incrementAndGet();
        debugRegistry.put(debugId, new DebugNode(this.getClass().getSimpleName(), captureCreationStack()));
    }

    protected final Logger logger = RSM.getLogger();
    public enum Align {
        AUTO(Yoga.YGAlignAuto), FLEX_START(Yoga.YGAlignFlexStart), CENTER(Yoga.YGAlignCenter), FLEX_END(Yoga.YGAlignFlexEnd), STRETCH(Yoga.YGAlignStretch),
        BASELINE(Yoga.YGAlignBaseline), SPACE_BETWEEN(Yoga.YGAlignSpaceBetween), SPACE_AROUND(Yoga.YGAlignSpaceAround);

        private final int yg;
        Align(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum JustifyContent {
        FLEX_START(Yoga.YGJustifyFlexStart), CENTER(Yoga.YGJustifyCenter), FLEX_END(Yoga.YGJustifyFlexEnd),
        SPACE_BETWEEN(Yoga.YGJustifySpaceBetween), SPACE_AROUND(Yoga.YGJustifySpaceAround), SPACE_EVENLY(Yoga.YGJustifySpaceEvenly);

        private final int yg;
        JustifyContent(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum FlexDirection {
        COLUMN(Yoga.YGFlexDirectionColumn), COLUMN_REVERSE(Yoga.YGFlexDirectionColumnReverse), ROW(Yoga.YGFlexDirectionRow), ROW_REVERSE(Yoga.YGFlexDirectionRowReverse);

        private final int yg;
        FlexDirection(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum Display {
        FLEX(Yoga.YGDisplayFlex), NONE(Yoga.YGDisplayNone), CONTENTS(Yoga.YGDisplayContents);

        private final int yg;
        Display(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum FlexWrap {
        NO_WRAP(Yoga.YGWrapNoWrap), WRAP(Yoga.YGWrapWrap), WRAP_REVERSE(Yoga.YGWrapReverse);

        private final int yg;
        FlexWrap(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum PositionType {
        STATIC(Yoga.YGPositionTypeStatic), RELATIVE(Yoga.YGPositionTypeRelative), ABSOLUTE(Yoga.YGPositionTypeAbsolute);

        private final int yg;
        PositionType(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum Overflow {
        VISIBLE(Yoga.YGOverflowVisible), HIDDEN(Yoga.YGOverflowHidden), SCROLL(Yoga.YGOverflowScroll);

        private final int yg;
        Overflow(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    public enum Direction {
        INHERIT(Yoga.YGDirectionInherit), LTR(Yoga.YGDirectionLTR), RTL(Yoga.YGDirectionRTL);

        private final int yg;
        Direction(int yg) { this.yg = yg; }
        public int yg() { return yg; }
    }

    protected static final int EDGE_LEFT = Yoga.YGEdgeLeft;
    protected static final int EDGE_TOP = Yoga.YGEdgeTop;
    protected static final int EDGE_RIGHT = Yoga.YGEdgeRight;
    protected static final int EDGE_BOTTOM = Yoga.YGEdgeBottom;
    protected static final int EDGE_ALL = Yoga.YGEdgeAll;

    protected static final int GUTTER_COLUMN = Yoga.YGGutterColumn;
    protected static final int GUTTER_ROW = Yoga.YGGutterRow;
    protected static final int GUTTER_ALL = Yoga.YGGutterAll;

    @Setter @Getter
    private boolean visible = true;
    @Setter @Getter
    private boolean interactable = true;

    public abstract long getYogaNode();
    protected abstract ArrayList<UiElement> getChildren();

    public final int getChildrenSize() {
        return getChildren().size();
    }

    public final UiElement removeLastChild() {
        val child = getChildren().getLast();
        getChildren().remove(child);
        return child;
    }

    public final UiElement removeFirst() {
        return getChildren().removeFirst();
    }

    public final UiElement removeChildAt(int index) {
        val child = getChildren().get(index);
        getChildren().remove(child);
        return child;
    }

    public final void removeChild(UiElement child) {
        Yoga.YGNodeRemoveChild(getYogaNode(), child.getYogaNode());
        getChildren().remove(child);
        removeDebugEntry(child);
    }

    public final void addChildAt(int index, UiElement child) {
        if (Yoga.YGNodeGetParent(child.getYogaNode()) != 0) {
            throw new IllegalStateException("Node already has a parent: " + child);
        }

        Yoga.YGNodeInsertChild(getYogaNode(), child.getYogaNode(), index);
        getChildren().add(index, child);
        addDebugEntry(child);
    }

    public final void addChild(UiElement child) {
        if (Yoga.YGNodeGetParent(child.getYogaNode()) != 0) {
            throw new IllegalStateException("Node already has a parent: " + child);
        }

        Yoga.YGNodeInsertChild(getYogaNode(), child.getYogaNode(), Yoga.YGNodeGetChildCount(getYogaNode()));
        getChildren().add(child);
        addDebugEntry(child);
    }

    public final void clearChildren() {
        val iterator = getChildren().iterator();
        while (iterator.hasNext()) {
            val next = iterator.next();
            Yoga.YGNodeRemoveChild(getYogaNode(), next.getYogaNode());
            removeDebugEntry(next);
            iterator.remove();
        }
    }

    @Override
    public void close() {
        freeRecursive();
    }

    private boolean freed = false;

    protected final void freeRecursive() {
        if (freed) return;
        DebugNode entry = debugRegistry.get(debugId);

        yogaNodesFreed.incrementAndGet();
        Yoga.YGNodeFree(getYogaNode());

        for (UiElement child : getChildren()) {
            child.close();
        }

        if (entry != null) {
            entry.freed = true;
            entry.freedStack = captureCreationStack();
        }
        freed = true;
    }

    /** Pass Float.NaN for an undefined dimension. */
    public final void calculateLayout(float width, float height) {
        Yoga.YGNodeCalculateLayout(getYogaNode(), width, height, Direction.LTR.yg());
    }

    public final void setWidth(float width) {
        Yoga.YGNodeStyleSetWidth(getYogaNode(), width);
    }
    public final void setHeight(float height) {
        Yoga.YGNodeStyleSetHeight(getYogaNode(), height);
    }
    public final void setWidthPercent(float percent) {
        Yoga.YGNodeStyleSetWidthPercent(getYogaNode(), percent);
    }
    public final void setHeightPercent(float percent) {
        Yoga.YGNodeStyleSetHeightPercent(getYogaNode(), percent);
    }

    public final void setLeft(float left) { Yoga.YGNodeStyleSetPosition(getYogaNode(), EDGE_LEFT, left); }
    public final void setTop(float top) { Yoga.YGNodeStyleSetPosition(getYogaNode(), EDGE_TOP, top); }

    public final float layoutLeft() { return Yoga.YGNodeLayoutGetLeft(getYogaNode()); }
    public final float layoutTop() {
        return Yoga.YGNodeLayoutGetTop(getYogaNode());
    }
    public final float layoutWidth() {return Yoga.YGNodeLayoutGetWidth(getYogaNode());}
    public final float layoutHeight() {return Yoga.YGNodeLayoutGetHeight(getYogaNode());}

    public final float layoutPaddingLeft() { return Yoga.YGNodeLayoutGetPadding(getYogaNode(), EDGE_LEFT); }
    public final float layoutPaddingTop() { return Yoga.YGNodeLayoutGetPadding(getYogaNode(), EDGE_TOP); }
    public final float layoutPaddingRight() { return Yoga.YGNodeLayoutGetPadding(getYogaNode(), EDGE_RIGHT); }
    public final float layoutPaddingBottom() { return Yoga.YGNodeLayoutGetPadding(getYogaNode(), EDGE_BOTTOM); }

    /** Layout width minus left+right padding — inner content box width. */
    public final float layoutContentWidth()  { return layoutWidth() - layoutPaddingLeft() - layoutPaddingRight(); }
    /** Layout height minus top+bottom padding — inner content box height. */
    public final float layoutContentHeight() { return layoutHeight() - layoutPaddingTop() - layoutPaddingBottom(); }


    /** Absolute X of this element's content box, given its parent's absolute origin. */
    protected final float originX(float parentX) { return parentX + layoutLeft(); }

    /** Absolute Y of this element's content box, given its parent's absolute origin. */
    protected final float originY(float parentY) {
        return parentY + layoutTop();
    }

    @Getter
    private boolean thumbHovered = false;
    @Getter
    private boolean dragging = false;
    private float localY = 0f;
    @Getter private float targetScrollY = 0f;
    @Getter private float scrollableElementY = 0f;

    public void resetScroll() {
        targetScrollY = 0f;
        scrollableElementY = 0f;
    }

    protected void setTargetScrollY(float targetScrollY) {
        this.targetScrollY = Math.round(targetScrollY);
    }

    public final boolean isHovered(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        float x = originX(parentX);
        float y = originY(parentY) + scrollY;
        float width = layoutWidth();
        float height = layoutHeight();

        return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
    }

    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) { return false; }

    protected void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        dragging = false;
    }
    protected boolean mouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (dragging || Yoga.YGNodeStyleGetOverflow(getYogaNode()) != Yoga.YGOverflowScroll || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;

        val up = Yoga.YGNodeStyleGetFlexDirection(getYogaNode()) == Yoga.YGFlexDirectionColumnReverse;
        float viewportHeight = layoutContentHeight();
        float space = Math.max(0f, contentExtentY() - viewportHeight);

        val min = up ? 0f : -space;
        val max = up ? space : 0f;

        setTargetScrollY(Math.clamp(targetScrollY + verticalAmount * 28f, min, max));
        return true;
    }
    protected boolean charTyped(String string, float mouseX, float mouseY, float scrollY) { return false; }
    protected boolean keyPressed(int keyCode, float mouseX, float mouseY, float scrollY) { return false; }
    protected void mouseClickedUncancelable(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) { }
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) { }
    protected void hover() {}

    /** True if any direct child's bounds extend past this node's content box (vertically). */
    public final boolean isVerticallyOverflowing() {
        float contentHeight = layoutContentHeight();
        for (UiElement child : getChildren()) {
            float childBottom = child.layoutTop() + child.layoutHeight();
            if (childBottom > contentHeight) return true;
        }
        return false;
    }

    /** True if any direct child's bounds extend past this node's content box (horizontally). */
    public final boolean isHorizontallyOverflowing() {
        float contentWidth = layoutContentWidth();
        for (UiElement child : getChildren()) {
            float childRight = child.layoutLeft() + child.layoutWidth();
            if (childRight > contentWidth) return true;
        }
        return false;
    }

    /** Total extent of a column-direction layout's content, i.e. how far scrolling could go. */
    public final float contentExtentY() {
        float max = 0f;
        for (UiElement child : getChildren()) {
            max = Math.max(max, child.layoutTop() + child.layoutHeight());
        }
        return max;
    }

    public final float contentExtentX() {
        float max = 0f;
        for (UiElement child : getChildren()) {
            max = Math.max(max, child.layoutLeft() + child.layoutWidth());
        }
        return max;
    }

    public boolean dispatchMouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return false;
        val children = getChildren();

        if (handleThumbInteractions(button, parentX, parentY, mouseX, mouseY, scrollY)) return true;

        if (children.isEmpty()) return mouseClicked(button, parentX, parentY, mouseX, mouseY, scrollY);

        for (UiElement child : new ArrayList<>(children)) {
            if (child.dispatchMouseClicked(button, originX(parentX), originY(parentY), mouseX, mouseY, scrollY + targetScrollY)) return true;
        }
        return mouseClicked(button, parentX, parentY, mouseX, mouseY, scrollY);
    }
    public void dispatchMouseClickedUncancelable(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return;
        val children = getChildren();
        if (children.isEmpty()) {
            mouseClickedUncancelable(button, parentX, parentY, mouseX, mouseY, scrollY);
            return;
        }

        for (UiElement child : new ArrayList<>(children)) {
            child.dispatchMouseClickedUncancelable(button, originX(parentX), originY(parentY), mouseX, mouseY, scrollY + targetScrollY);
        }
        mouseClickedUncancelable(button, parentX, parentY, mouseX, mouseY, scrollY);
    }
    public void dispatchMouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return;
        val children = getChildren();
        if (children.isEmpty()) {
            mouseReleased(button, parentX, parentY, mouseX, mouseY, scrollY);
            return;
        }

        for (UiElement child : new ArrayList<>(children)) {
            child.dispatchMouseReleased(button, originX(parentX), originY(parentY), mouseX, mouseY, scrollY + targetScrollY);
        }
        mouseReleased(button, parentX, parentY, mouseX, mouseY, scrollY);
    }
    public boolean dispatchMouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return false;
        val children = getChildren();
        if (children.isEmpty()) return mouseScrolled(verticalAmount, parentX, parentY, mouseX, mouseY, scrollY);

        for (UiElement child : new ArrayList<>(children)) {
            if (child.dispatchMouseScrolled(verticalAmount, originX(parentX), originY(parentY), mouseX, mouseY, scrollY + targetScrollY)) return true;
        }
        return mouseScrolled(verticalAmount, parentX, parentY, mouseX, mouseY, scrollY);
    }
    public boolean dispatchCharTyped(String string, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return false;
        val children = getChildren();
        if (children.isEmpty()) return charTyped(string, mouseX, mouseY, scrollY);

        for (UiElement child : new ArrayList<>(children)) {
            if (child.dispatchCharTyped(string, mouseX, mouseY, scrollY + targetScrollY)) return true;
        }
        return charTyped(string, mouseX, mouseY, scrollY);
    }
    public final boolean dispatchKeyPressed(int keyCode, float mouseX, float mouseY, float scrollY) {
        if (!this.interactable || !visible) return false;
        val children = getChildren();
        if (children.isEmpty()) return keyPressed(keyCode, mouseX, mouseY, scrollY);

        for (UiElement child : new ArrayList<>(children)) {
            if (child.dispatchKeyPressed(keyCode, mouseX, mouseY, scrollY + targetScrollY)) return true;
        }
        return keyPressed(keyCode, mouseX, mouseY, scrollY);
    }

    long lastMs = 0;
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!visible) return;
        if (isHovered(parentX, parentY, mouseX, mouseY, scrollY)) hover();
        frame(parentX, parentY, mouseX, mouseY, scrollY);

        val children = getChildren();
        if (children.isEmpty()) return;

        val scroll = Yoga.YGNodeStyleGetOverflow(getYogaNode()) == Yoga.YGOverflowScroll;
        val clip = scroll || Yoga.YGNodeStyleGetOverflow(getYogaNode()) == Yoga.YGOverflowHidden;
        val x = originX(parentX);
        val y = originY(parentY);
        if (clip) {
            NVGUtils.pushScissor(x, y, layoutWidth(), layoutHeight());
            if (scroll) {
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastMs) / 1000.0f;
                lastMs = currentTime;

                float smoothingSpeed = dragging ? 24.0f : 12.0f;
                this.scrollableElementY += (targetScrollY - scrollableElementY) * Math.min(1.0f, deltaTime * smoothingSpeed);
            }
        }

        val scrollValue = getScrollableElementY();
        if (scroll) {
            NVGUtils.translate(0f, scrollValue);
        }

        for (UiElement child : new ArrayList<>(children)) {
            child.dispatchFrame(x, y, mouseX, mouseY, scrollY + scrollValue);
        }
        if (clip) {
            if (scroll) {
                NVGUtils.translate(0f, -scrollValue);
                handleThumbFrame(x, y, mouseX, mouseY);
            }
            NVGUtils.popScissor();
        }
    }

    private static final float MIN_THUMB_HEIGHT = 28f;
    private static final float THUMB_BAR_WIDTH = 4f;

    protected boolean handleThumbInteractions(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != 0 || Yoga.YGNodeStyleGetOverflow(getYogaNode()) != Yoga.YGOverflowScroll) return false;

        val x = originX(parentX) - THUMB_BAR_WIDTH + layoutContentWidth();
        val y = originY(parentY) + scrollY;

        float viewportHeight = layoutContentHeight();
        float contentHeight = Math.max(viewportHeight, contentExtentY());
        float trackHeight = layoutContentHeight();
        float space = Math.max(0f, contentExtentY() - viewportHeight);
        if (space == 0f) return false;

        float thumbHeight = trackHeight * (viewportHeight / contentHeight);
        thumbHeight = Math.max(thumbHeight, MIN_THUMB_HEIGHT);

        float scrollProgress = space > 0f ? (-getScrollableElementY() / space) : 0f;
        float trackTravel = trackHeight - thumbHeight;
        float thumbY = trackTravel * scrollProgress;

        val thumbHovered = mouseX >= x && mouseX <= (x + THUMB_BAR_WIDTH) && mouseY >= (y + thumbY) && mouseY <= (y + thumbY + thumbHeight);
        if (thumbHovered) {
            dragging = true;
            localY = mouseY - y - thumbY;
            return true;
        }

        return false;
    }


    protected void handleThumbFrame(float x, float y, float mouseX, float mouseY) {
        float viewportHeight = layoutContentHeight();
        float contentHeight = Math.max(viewportHeight, contentExtentY());
        float trackHeight = layoutContentHeight();
        float space = Math.max(0f, contentExtentY() - viewportHeight);
        if (space == 0f) return;

        float thumbHeight = trackHeight * (viewportHeight / contentHeight);
        thumbHeight = Math.max(thumbHeight, MIN_THUMB_HEIGHT);

        float scrollProgress = space > 0f ? (-getScrollableElementY() / space) : 0f;
        float trackTravel = trackHeight - thumbHeight;
        float thumbY = trackTravel * scrollProgress;

        val drawX = x + layoutWidth() - THUMB_BAR_WIDTH;
        val drawY = y + thumbY;
        int color;
        if (mouseX >= drawX && mouseX <= (drawX + THUMB_BAR_WIDTH) && mouseY >= drawY && mouseY <= (drawY + thumbHeight)) {
            MouseUtils.setHandCursor();
            color = Palette.stroke.brighter(0.1f);
            thumbHovered = true;
        } else {
            thumbHovered = false;
            if (dragging) {
                color = Palette.stroke.brighter(0.1f);
            } else {
                color = Palette.stroke.getARGB();
            }
        }


        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, drawX, drawY, THUMB_BAR_WIDTH, thumbHeight);
        NVGUtils.color(color);
        NanoVG.nvgFillColor(vg, NVGUtils.getNvgColor());
        NanoVG.nvgFill(vg);

        if (dragging) {
            float drawYNow = mouseY - localY;
            float progress = Math.clamp((drawYNow - y) / trackTravel, 0f, 1f);
            setTargetScrollY(progress * -space);
        }
    }

    public String treeString() {
        StringBuilder sb = new StringBuilder();
        sb.append("w: ").append(layoutWidth())
                .append(", h: ").append(layoutHeight()).append('\n');
        appendChildren(sb, this, "");
        return sb.toString();
    }

    public void printTree() {
        System.out.print(treeString());
    }

    private static void appendChildren(StringBuilder sb, UiElement node, String prefix) {
        int count = node.getChildren().size();
        for (int i = 0; i < count; i++) {
            appendBranch(sb, node.getChildren().get(i), prefix, i == count - 1);
        }
    }

    private static void appendBranch(StringBuilder sb, UiElement node, String prefix, boolean isLast) {
        sb.append(prefix)
                .append(isLast ? "└─ " : "├─ ")
                .append("w: ").append(node.layoutWidth())
                .append(", h: ").append(node.layoutHeight())
                .append('\n');
        appendChildren(sb, node, prefix + (isLast ? "   " : "│  "));
    }

    /**
     * Self-typed style builder shared by all node types. Subclasses add their own
     * fields/setters, call {@link #buildYogaNode()} to create the styled Yoga node,
     * and wrap it in their concrete {@link Node} via {@link #build()}.
     */

    public static abstract class Builder<B extends Builder<B>> extends AbstractYogaNodeBuilder<B> {
        protected Color color;

        /** Build the concrete node. Implementations should call {@link #buildYogaNode()}. */
        public abstract UiElement build();

        public final B color(Color v) { this.color = v; return self(); }
        public final B color(int hex) { this.color = Color.fromHex(hex); return self(); }
        public final B color(int hex, float alpha) { this.color = Color.fromHex(hex, alpha); return self(); }
        public final B color(byte r, byte g, byte b, float alpha) { this.color = Color.fromRGB(r, g, b, alpha); return self(); }
    }

    public static final class YogaNodeBuilder extends AbstractYogaNodeBuilder<YogaNodeBuilder> {
        public long build() {
            return buildYogaNode();
        }
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static abstract class AbstractYogaNodeBuilder<B extends AbstractYogaNodeBuilder<B>> {
        protected FlexDirection flexDirection;
        protected FlexWrap flexWrap;
        protected Display display;

        protected Float flex, flexGrow, flexShrink, flexBasis;

        protected JustifyContent justifyContent;
        protected Align alignItems, alignSelf, alignContent;

        protected Float width, maxWidth, minWidth, height, maxHeight, minHeight, aspectRatio;
        protected Float widthPercent, maxWidthPercent, minWidthPercent, heightPercent, maxHeightPercent, minHeightPercent;
        protected boolean widthAuto, heightAuto, flexBasisAuto;
        protected Float flexBasisPercent;
        protected Float padding, paddingTop, paddingBottom, paddingLeft, paddingRight;
        protected Float paddingPercent, paddingTopPercent, paddingBottomPercent, paddingLeftPercent, paddingRightPercent;
        protected Float margin, marginTop, marginBottom, marginLeft, marginRight;
        protected Float marginPercent, marginTopPercent, marginBottomPercent, marginLeftPercent, marginRightPercent;
        protected boolean marginAuto, marginTopAuto, marginBottomAuto, marginLeftAuto, marginRightAuto;
        protected Float border, borderTop, borderBottom, borderLeft, borderRight;
        protected Float gap, rowGap, columnGap;

        protected PositionType positionType;
        protected Float top, bottom, left, right;
        protected Float topPercent, bottomPercent, leftPercent, rightPercent;

        protected Overflow overflow;
        protected Direction direction;

        protected final B self() { return (B) this; }

        public final B flexDirection(FlexDirection v) { this.flexDirection = v; return self(); }
        public final B flexWrap(FlexWrap v) { this.flexWrap = v; return self(); }
        public final B display(Display v) { this.display = v; return self(); }

        public final B flex(float v) { this.flex = v; return self(); }
        public final B flexGrow(float v) { this.flexGrow = v; return self(); }
        public final B flexShrink(float v) { this.flexShrink = v; return self(); }
        public final B flexBasis(float v) { this.flexBasis = v; return self(); }
        public final B flexBasisPercent(float v) { this.flexBasisPercent = v; return self(); }
        public final B flexBasisAuto() { this.flexBasisAuto = true; return self(); }

        public final B justifyContent(JustifyContent v) { this.justifyContent = v; return self(); }
        public final B alignItems(Align v) { this.alignItems = v; return self(); }
        public final B alignSelf(Align v) { this.alignSelf = v; return self(); }
        public final B alignContent(Align v) { this.alignContent = v; return self(); }

        public final B width(float v) { this.width = v; return self(); }
        public final B maxWidth(float v) { this.maxWidth = v; return self(); }
        public final B minWidth(float v) { this.minWidth = v; return self(); }
        public final B height(float v) { this.height = v; return self(); }
        public final B maxHeight(float v) { this.maxHeight = v; return self(); }
        public final B minHeight(float v) { this.minHeight = v; return self(); }
        public final B aspectRatio(float v) { this.aspectRatio = v; return self(); }

        public final B widthPercent(float v) { this.widthPercent = v; return self(); }
        public final B maxWidthPercent(float v) { this.maxWidthPercent = v; return self(); }
        public final B minWidthPercent(float v) { this.minWidthPercent = v; return self(); }
        public final B heightPercent(float v) { this.heightPercent = v; return self(); }
        public final B maxHeightPercent(float v) { this.maxHeightPercent = v; return self(); }
        public final B minHeightPercent(float v) { this.minHeightPercent = v; return self(); }
        public final B widthAuto() { this.widthAuto = true; return self(); }
        public final B heightAuto() { this.heightAuto = true; return self(); }

        public final B padding(float v) { this.padding = v; return self(); }
        public final B paddingTop(float v) { this.paddingTop = v; return self(); }
        public final B paddingBottom(float v) { this.paddingBottom = v; return self(); }
        public final B paddingLeft(float v) { this.paddingLeft = v; return self(); }
        public final B paddingRight(float v) { this.paddingRight = v; return self(); }

        public final B paddingPercent(float v) { this.paddingPercent = v; return self(); }
        public final B paddingTopPercent(float v) { this.paddingTopPercent = v; return self(); }
        public final B paddingBottomPercent(float v) { this.paddingBottomPercent = v; return self(); }
        public final B paddingLeftPercent(float v) { this.paddingLeftPercent = v; return self(); }
        public final B paddingRightPercent(float v) { this.paddingRightPercent = v; return self(); }

        public final B margin(float v) { this.margin = v; return self(); }
        public final B marginTop(float v) { this.marginTop = v; return self(); }
        public final B marginBottom(float v) { this.marginBottom = v; return self(); }
        public final B marginLeft(float v) { this.marginLeft = v; return self(); }
        public final B marginRight(float v) { this.marginRight = v; return self(); }

        public final B marginPercent(float v) { this.marginPercent = v; return self(); }
        public final B marginTopPercent(float v) { this.marginTopPercent = v; return self(); }
        public final B marginBottomPercent(float v) { this.marginBottomPercent = v; return self(); }
        public final B marginLeftPercent(float v) { this.marginLeftPercent = v; return self(); }
        public final B marginRightPercent(float v) { this.marginRightPercent = v; return self(); }

        public final B marginAuto() { this.marginAuto = true; return self(); }
        public final B marginTopAuto() { this.marginTopAuto = true; return self(); }
        public final B marginBottomAuto() { this.marginBottomAuto = true; return self(); }
        public final B marginLeftAuto() { this.marginLeftAuto = true; return self(); }
        public final B marginRightAuto() { this.marginRightAuto = true; return self(); }

        public final B border(float v) { this.border = v; return self(); }
        public final B borderTop(float v) { this.borderTop = v; return self(); }
        public final B borderBottom(float v) { this.borderBottom = v; return self(); }
        public final B borderLeft(float v) { this.borderLeft = v; return self(); }
        public final B borderRight(float v) { this.borderRight = v; return self(); }

        public final B gap(float v) { this.gap = v; return self(); }
        public final B rowGap(float v) { this.rowGap = v; return self(); }
        public final B columnGap(float v) { this.columnGap = v; return self(); }

        public final B positionType(PositionType v) { this.positionType = v; return self(); }
        public final B top(float v) { this.top = v; return self(); }
        public final B bottom(float v) { this.bottom = v; return self(); }
        public final B left(float v) { this.left = v; return self(); }
        public final B right(float v) { this.right = v; return self(); }
        public final B topPercent(float v) { this.topPercent = v; return self(); }
        public final B bottomPercent(float v) { this.bottomPercent = v; return self(); }
        public final B leftPercent(float v) { this.leftPercent = v; return self(); }
        public final B rightPercent(float v) { this.rightPercent = v; return self(); }

        public final B overflow(Overflow v) { this.overflow = v; return self(); }
        public final B direction(Direction v) { this.direction = v; return self(); }


        /** Create a Yoga node and apply every set style property. */
        protected final long buildYogaNode() {
            long n = Yoga.YGNodeNew();
            yogaNodesCreated.incrementAndGet();

            if (flexDirection != null) Yoga.YGNodeStyleSetFlexDirection(n, flexDirection.yg());
            if (flexWrap != null)      Yoga.YGNodeStyleSetFlexWrap(n, flexWrap.yg());
            if (display != null)       Yoga.YGNodeStyleSetDisplay(n, display.yg());

            if (flex != null)       Yoga.YGNodeStyleSetFlex(n, flex);
            if (flexGrow != null)   Yoga.YGNodeStyleSetFlexGrow(n, flexGrow);
            if (flexShrink != null) Yoga.YGNodeStyleSetFlexShrink(n, flexShrink);
            if (flexBasis != null)        Yoga.YGNodeStyleSetFlexBasis(n, flexBasis);
            if (flexBasisPercent != null) Yoga.YGNodeStyleSetFlexBasisPercent(n, flexBasisPercent);
            if (flexBasisAuto)            Yoga.YGNodeStyleSetFlexBasisAuto(n);

            if (justifyContent != null) Yoga.YGNodeStyleSetJustifyContent(n, justifyContent.yg());
            if (alignItems != null)     Yoga.YGNodeStyleSetAlignItems(n, alignItems.yg());
            if (alignSelf != null)      Yoga.YGNodeStyleSetAlignSelf(n, alignSelf.yg());
            if (alignContent != null)   Yoga.YGNodeStyleSetAlignContent(n, alignContent.yg());

            if (width != null)       Yoga.YGNodeStyleSetWidth(n, width);
            if (widthPercent != null) Yoga.YGNodeStyleSetWidthPercent(n, widthPercent);
            if (widthAuto)           Yoga.YGNodeStyleSetWidthAuto(n);
            if (minWidth != null)    Yoga.YGNodeStyleSetMinWidth(n, minWidth);
            if (minWidthPercent != null) Yoga.YGNodeStyleSetMinWidthPercent(n, minWidthPercent);
            if (maxWidth != null)    Yoga.YGNodeStyleSetMaxWidth(n, maxWidth);
            if (maxWidthPercent != null) Yoga.YGNodeStyleSetMaxWidthPercent(n, maxWidthPercent);
            if (height != null)      Yoga.YGNodeStyleSetHeight(n, height);
            if (heightPercent != null) Yoga.YGNodeStyleSetHeightPercent(n, heightPercent);
            if (heightAuto)          Yoga.YGNodeStyleSetHeightAuto(n);
            if (minHeight != null)   Yoga.YGNodeStyleSetMinHeight(n, minHeight);
            if (minHeightPercent != null) Yoga.YGNodeStyleSetMinHeightPercent(n, minHeightPercent);
            if (maxHeight != null)   Yoga.YGNodeStyleSetMaxHeight(n, maxHeight);
            if (maxHeightPercent != null) Yoga.YGNodeStyleSetMaxHeightPercent(n, maxHeightPercent);
            if (aspectRatio != null) Yoga.YGNodeStyleSetAspectRatio(n, aspectRatio);

            if (padding != null)       Yoga.YGNodeStyleSetPadding(n, EDGE_ALL, padding);
            if (paddingTop != null)    Yoga.YGNodeStyleSetPadding(n, EDGE_TOP, paddingTop);
            if (paddingBottom != null) Yoga.YGNodeStyleSetPadding(n, EDGE_BOTTOM, paddingBottom);
            if (paddingLeft != null)   Yoga.YGNodeStyleSetPadding(n, EDGE_LEFT, paddingLeft);
            if (paddingRight != null)  Yoga.YGNodeStyleSetPadding(n, EDGE_RIGHT, paddingRight);

            if (paddingPercent != null)       Yoga.YGNodeStyleSetPaddingPercent(n, EDGE_ALL, paddingPercent);
            if (paddingTopPercent != null)    Yoga.YGNodeStyleSetPaddingPercent(n, EDGE_TOP, paddingTopPercent);
            if (paddingBottomPercent != null) Yoga.YGNodeStyleSetPaddingPercent(n, EDGE_BOTTOM, paddingBottomPercent);
            if (paddingLeftPercent != null)   Yoga.YGNodeStyleSetPaddingPercent(n, EDGE_LEFT, paddingLeftPercent);
            if (paddingRightPercent != null)  Yoga.YGNodeStyleSetPaddingPercent(n, EDGE_RIGHT, paddingRightPercent);

            if (margin != null)       Yoga.YGNodeStyleSetMargin(n, EDGE_ALL, margin);
            if (marginTop != null)    Yoga.YGNodeStyleSetMargin(n, EDGE_TOP, marginTop);
            if (marginBottom != null) Yoga.YGNodeStyleSetMargin(n, EDGE_BOTTOM, marginBottom);
            if (marginLeft != null)   Yoga.YGNodeStyleSetMargin(n, EDGE_LEFT, marginLeft);
            if (marginRight != null)  Yoga.YGNodeStyleSetMargin(n, EDGE_RIGHT, marginRight);

            if (marginPercent != null)       Yoga.YGNodeStyleSetMarginPercent(n, EDGE_ALL, marginPercent);
            if (marginTopPercent != null)    Yoga.YGNodeStyleSetMarginPercent(n, EDGE_TOP, marginTopPercent);
            if (marginBottomPercent != null) Yoga.YGNodeStyleSetMarginPercent(n, EDGE_BOTTOM, marginBottomPercent);
            if (marginLeftPercent != null)   Yoga.YGNodeStyleSetMarginPercent(n, EDGE_LEFT, marginLeftPercent);
            if (marginRightPercent != null)  Yoga.YGNodeStyleSetMarginPercent(n, EDGE_RIGHT, marginRightPercent);

            if (marginAuto)       Yoga.YGNodeStyleSetMarginAuto(n, EDGE_ALL);
            if (marginTopAuto)    Yoga.YGNodeStyleSetMarginAuto(n, EDGE_TOP);
            if (marginBottomAuto) Yoga.YGNodeStyleSetMarginAuto(n, EDGE_BOTTOM);
            if (marginLeftAuto)   Yoga.YGNodeStyleSetMarginAuto(n, EDGE_LEFT);
            if (marginRightAuto)  Yoga.YGNodeStyleSetMarginAuto(n, EDGE_RIGHT);

            if (border != null)       Yoga.YGNodeStyleSetBorder(n, EDGE_ALL, border);
            if (borderTop != null)    Yoga.YGNodeStyleSetBorder(n, EDGE_TOP, borderTop);
            if (borderBottom != null) Yoga.YGNodeStyleSetBorder(n, EDGE_BOTTOM, borderBottom);
            if (borderLeft != null)   Yoga.YGNodeStyleSetBorder(n, EDGE_LEFT, borderLeft);
            if (borderRight != null)  Yoga.YGNodeStyleSetBorder(n, EDGE_RIGHT, borderRight);

            if (gap != null)       Yoga.YGNodeStyleSetGap(n, GUTTER_ALL, gap);
            if (rowGap != null)    Yoga.YGNodeStyleSetGap(n, GUTTER_ROW, rowGap);
            if (columnGap != null) Yoga.YGNodeStyleSetGap(n, GUTTER_COLUMN, columnGap);

            if (positionType != null) Yoga.YGNodeStyleSetPositionType(n, positionType.yg());
            if (top != null)    Yoga.YGNodeStyleSetPosition(n, EDGE_TOP, top);
            if (bottom != null) Yoga.YGNodeStyleSetPosition(n, EDGE_BOTTOM, bottom);
            if (left != null)   Yoga.YGNodeStyleSetPosition(n, EDGE_LEFT, left);
            if (right != null)  Yoga.YGNodeStyleSetPosition(n, EDGE_RIGHT, right);
            if (topPercent != null)    Yoga.YGNodeStyleSetPositionPercent(n, EDGE_TOP, topPercent);
            if (bottomPercent != null) Yoga.YGNodeStyleSetPositionPercent(n, EDGE_BOTTOM, bottomPercent);
            if (leftPercent != null)   Yoga.YGNodeStyleSetPositionPercent(n, EDGE_LEFT, leftPercent);
            if (rightPercent != null)  Yoga.YGNodeStyleSetPositionPercent(n, EDGE_RIGHT, rightPercent);

            if (overflow != null)  Yoga.YGNodeStyleSetOverflow(n, overflow.yg());
            if (direction != null) Yoga.YGNodeStyleSetDirection(n, direction.yg());

            return n;
        }
    }
}