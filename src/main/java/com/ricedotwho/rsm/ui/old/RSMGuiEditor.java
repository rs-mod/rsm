package com.ricedotwho.rsm.ui.old;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.mouse.MouseUtils;
import manifold.ext.rt.api.auto;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class RSMGuiEditor extends Screen implements Accessor {
    private static final float HANDLE_SIZE = 12f;
    private static final double SNAP_DISTANCE = 6.0;
    private static final Color NONE = Color.WHITE;
    private static final Color HOVERING_DRAG = Color.GREEN.brighter(0.5f);
    private static final Color HOVERING_SCALE = Color.MINECRAFT_AQUA.brighter(0.5f);
    private static final Color DRAGGING = Color.GREEN;
    private static final Color RESIZING = Color.MINECRAFT_AQUA.brighter();
    private static final Color SNAP = RESIZING.brighter();
    private static final int DARKEN = Color.BLACK.getARGBWithAlpha(0.35f);
    private static final String HELP = "Left click and drag to move elements with snapping, use right click for no snapping.\nHold shift to snap to grid. Click and drag the light blue square to scale elements.";
    private static final float GRID_SIZE = 5f;
    private static final float SCALE_STEP = 0.01f;
    private static final float MOVE_STEP = 1f;

    private State dragging = null;
    private final ModuleManager moduleManager;

    private Float snapX = null;
    private Float snapY = null;

    public RSMGuiEditor(ModuleManager moduleManager) {
        super(Component.literal("RSM Gui Editor"));
        this.moduleManager = moduleManager;
    }

    public static void open() {
        if (mc.gui.screen() == null){
            mc.gui.setScreen(RSM.getInstance().getGUIEditor());
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        gfx.push();
        var window = mc.getWindow();
        gfx.pose().scale(1f / window.getGuiScale());
        gfx.fill(0, 0, window.getWidth(), window.getHeight(), DARKEN);

        for (var setting : moduleManager.getHudSettings()) {
            if (!setting.isShown()) continue;
            var x = setting.getValue().x;
            var y = setting.getValue().y;
            var dims = setting.draw(gfx, true);

            var drag = dragging != null && dragging.setting == setting;
            var name = setting.getName();

            Fonts.text(gfx, Fonts.SF_PRO, drag ? "$name | ${(int) Math.floor(x)} ${(int) Math.floor(y)} x${com.ricedotwho.rsm.utils.MathUtils.round(setting.getScale(), 2)}" : name,
                    x,
                    y - Fonts.height(Fonts.SF_PRO, 16),
                    16, Color.WHITE
            );

            var mX = MouseUtils.mouseX();
            var mY = MouseUtils.mouseY();

            var hovering = MouseUtils.isHovering(mX, mY, setting.getValue().x, setting.getValue().y, dims.x, dims.y);
            var scaling = isHoveringResize(setting, mX, mY);

            if (hovering) {
                Render2D.rect(
                        gfx,
                        x + dims.x - HANDLE_SIZE,
                        y + dims.y - HANDLE_SIZE,
                        HANDLE_SIZE,
                        HANDLE_SIZE,
                        Color.MINECRAFT_AQUA,
                        1f
                );
            }

            Render2D.outline(gfx,
                    x,
                    y,
                    dims.x,
                    dims.y, colour(setting, hovering, scaling), 2f, 1f
            );
        }

        if (dragging != null) {
            if (snapX != null) {
                Render2D.line(gfx, snapX, 0f, snapX, window.getHeight(), SNAP, 0.5f);
            }
            if (snapY != null) {
                Render2D.line(gfx, 0f, snapY, window.getWidth(), snapY, SNAP, 0.5f);
            }
        }

        // render over elements probably
        Fonts.centeredWrappedText(gfx, Fonts.SF_PRO, HELP,
                window.getWidth() / 2f,
                (window.getScreenHeight() * 0.9f) - Fonts.height(Fonts.SF_PRO, 24),
                24, 6767, Color.WHITE
        );

        gfx.pop();
    }

    private Color colour(HudSetting setting, boolean drag, boolean scale) {
        if (dragging == null) {
            if (scale) return HOVERING_SCALE;
            if (drag) return HOVERING_DRAG;
            return NONE;
        }
        if (dragging.setting != setting) return NONE;
        if (dragging.mode == Mode.MOVE) return DRAGGING;
        return RESIZING;
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        var mouseX = MouseUtils.mouseX();
        var mouseY = MouseUtils.mouseY();

        for (var setting : moduleManager.getHudSettings()) {
            if (!setting.isShown()) continue;
            if (MouseUtils.isHovering(mouseX, mouseY, setting.getValue().x, setting.getValue().y, setting.getLastSize().x, setting.getLastSize().y)) {
                switch (keyEvent.key()) {
                    case InputConstants.KEY_EQUALS -> setting.setScale(setting.getScale() + SCALE_STEP);
                    case InputConstants.KEY_MINUS -> setting.setScale(setting.getScale() - SCALE_STEP);
                    case InputConstants.KEY_LEFT -> moveStep(setting, -MOVE_STEP, true);
                    case InputConstants.KEY_RIGHT -> moveStep(setting, MOVE_STEP, true);
                    case InputConstants.KEY_UP-> moveStep(setting, -MOVE_STEP, false);
                    case InputConstants.KEY_DOWN -> moveStep(setting, MOVE_STEP, false);
                    default -> {
                        return super.keyPressed(keyEvent);
                    }
                }
                return false;
            }
        }

        return super.keyPressed(keyEvent);
    }

    private void moveStep(HudSetting setting, float amount, boolean horizontal) {
        if (horizontal) {
            setting.getValue().x = Math.round(setting.getValue().x + amount);
        } else {
            setting.getValue().y = Math.round(setting.getValue().y + amount);
        }

    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public final boolean mouseClicked(@NotNull MouseButtonEvent click, boolean doubled) {
        var mouseX = MouseUtils.mouseX();
        var mouseY = MouseUtils.mouseY();

        for (var setting : moduleManager.getHudSettings()) {
            if (!setting.isShown()) continue;
            if (isHoveringResize(setting, mouseX, mouseY)) {
                drag(setting, Mode.RESIZE, mouseX, mouseY, click.button());
                return false;
            }

            if (MouseUtils.isHovering(mouseX, mouseY, setting.getValue().x, setting.getValue().y, setting.getLastSize().x, setting.getLastSize().y)) {
                drag(setting, Mode.MOVE, mouseX, mouseY, click.button());
                return false;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void mouseMoved(double ignored, double ignored2) {
        if (dragging == null) return;
        var mouseX = MouseUtils.mouseX();
        var mouseY = MouseUtils.mouseY();

        float dx = (float) (mouseX - dragging.mouseStartX);
        float dy = (float) (mouseY - dragging.mouseStartY);

        if (dragging.mode == Mode.MOVE) {
            var x = dragging.startX + dx;
            var y = dragging.startY + dy;

            var snapped = snapPosition(x, y, dragging);

            dragging.setting.setValue(snapped);
        } else {
            if (dragging.startWidth <= 0 || dragging.startHeight <= 0) {
                return;
            }
            float newScale = dragging.startScale * (1f + dragging.factor(dx, dy));
            dragging.setting.setScale(newScale);
        }
    }

    @Override
    public boolean mouseScrolled(double mX, double mY, double hScroll, double vScroll) {
        super.mouseScrolled(mX, mY, hScroll, vScroll);

        var mouseX = MouseUtils.mouseX();
        var mouseY = MouseUtils.mouseY();

        int amount = (int) (Math.signum(vScroll) * 16);

        if (amount != 0) {
            for (var setting : moduleManager.getHudSettings()) {
                if (!setting.isShown()) continue;
                if (MouseUtils.isHovering(mouseX, mouseY, setting.getValue().x, setting.getValue().y, setting.getLastSize().x, setting.getLastSize().y)) {
                    var zoom = amount > 0 ? SCALE_STEP : -SCALE_STEP;
                    setting.setScale(setting.getScale() + zoom);
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public final boolean mouseReleased(@NonNull MouseButtonEvent click) {
        unDrag();
        return false;
    }

    @Override
    public void init() {
        unDrag();
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        unDrag();
        super.onClose();
    }

    private Vector2f snapPosition(float x, float y, State state) {
        if (!state.canSnap) return new Vector2f(x, y);
        var setting = state.setting;

        var sX = findBestSnap(x, setting.getLastSize().x, getSnaps(true, setting));
        var sY = findBestSnap(y, setting.getLastSize().y, getSnaps(false, setting));

        var hasX = sX.distance <= SNAP_DISTANCE;
        var hasY = sY.distance <= SNAP_DISTANCE;

        snapX = hasX ? sX.point : null;
        snapY = hasY ? sY.point : null;

        // grid snapping and whatever
        var rx = hasX ? sX.position : mc.hasShiftDown() ? Math.round(x / GRID_SIZE) * GRID_SIZE : x;
        var ry = hasY ? sY.position : mc.hasShiftDown() ? Math.round(y / GRID_SIZE) * GRID_SIZE : y;

        return new Vector2f(rx, ry);
    }

    private List<Float> getSnaps(boolean horizontal, HudSetting excluded) {
        List<Float> targets = new ArrayList<>();

        if (horizontal) {
            float width = mc.getWindow().getWidth();
            targets.add(0f);
            targets.add(width / 2f);
            targets.add(width);
        } else {
            float height = mc.getWindow().getHeight();
            targets.add(0f);
            targets.add(height / 2f);
            targets.add(height);
        }

        for (var setting : moduleManager.getHudSettings()) {
            if (!setting.isShown() || setting == excluded) continue;


            float position = horizontal ? setting.getValue().x : setting.getValue().y;
            float size = horizontal ? setting.getLastSize().x : setting.getLastSize().y;

            targets.add(position);
            targets.add(position + size / 2f);
            targets.add(position + size);
        }

        return targets;
    }

    private auto findBestSnap(float position, float size, List<Float> options) {
        float[] self = {position, position + size / 2f, position + size};

        var best = (distance:Float.MAX_VALUE, position:(Float)null, point:(Float)null);

        for (var point : self) {
            for (var option : options) {
                float diff = option - point;
                float dist = Math.abs(diff);

                if (dist < best.distance) {
                    best.distance = dist;
                    best.position = position + diff;
                    best.point = point + diff;
                }
            }
        }

        return best;
    }

    private void drag(HudSetting setting, Mode mode, double mouseX, double mouseY, int button) {
        dragging = new State(setting, mode, mouseX, mouseY, button != InputConstants.MOUSE_BUTTON_RIGHT);
    }

    private void unDrag() {
        dragging = null;
        snapX = null;
        snapY = null;
    }

    private boolean isHoveringResize(HudSetting setting, double mouseX, double mouseY) {
        double x = setting.getValue().x + setting.getLastSize().x;
        double y = setting.getValue().y + setting.getLastSize().y;
        return mouseX >= x - HANDLE_SIZE && mouseX <= x && mouseY >= y - HANDLE_SIZE && mouseY <= y;
    }

    private static class State {
        final HudSetting setting;
        final Mode mode;
        final float startX;
        final float startY;
        final boolean canSnap;
        final double mouseStartX;
        final double mouseStartY;
        final float startWidth;
        final float startHeight;
        final float startScale;

        public State(HudSetting setting, Mode mode, double mouseX, double mouseY, boolean canSnap) {
            this.setting = setting;
            this.mode = mode;
            this.startX = setting.getValue().x;
            this.startY = setting.getValue().y;
            this.canSnap = canSnap;
            this.mouseStartX = mouseX;
            this.mouseStartY = mouseY;
            this.startWidth = setting.getLastSize().x;
            this.startHeight = setting.getLastSize().y;
            this.startScale = setting.getScale();
        }

        float factor(float dx, float dy) {
            return (dx * startWidth + dy * startHeight) / (startWidth * startWidth + startHeight * startHeight);
        }
    }

    private enum Mode {
        MOVE,
        RESIZE
    }
}
