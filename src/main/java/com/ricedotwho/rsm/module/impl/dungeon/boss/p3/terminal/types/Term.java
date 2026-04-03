package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Getter
public abstract class Term implements Accessor {
    protected static final Map<Integer, ItemStack> packetItems = new HashMap<>();
    public final Map<Integer, Pair<TermSol, Long>> clickedSlots = new HashMap<>();
    protected final List<TermSol> rawSolution = new ArrayList<>();
    protected final List<TermSol> solution = new ArrayList<>();
    private int windowCount = 0;
    protected boolean clicked = false;
    private final String guiTitle;
    protected TermSol lastClick = null;

    public Term(String title) {
        packetItems.clear();
        this.guiTitle = title;
    }

    public void onSlot(int slot, ItemStack item) {
        if (slot < 0) return;
        packetItems.put(slot, item);

        if (canSolve(slot)) {
            solution.clear();
            rawSolution.clear();
            solve();
            rawSolution.addAll(solution.stream().map(TermSol::copy).toList());
            updateSolutionWithPrediction();

            //ChatUtils.chat("New terminal window: %s. %s", solution, rawSolution);

            if (TerminalSolver.getMode().is("Queue") && !clickedSlots.isEmpty()) {
                if (!clickFromQueue()) {
                    clicked = false;
                }
            } else {
                clicked = false;
            }
        }
    }

    protected boolean canSolve(int slot) {
        return slot == this.getSlotCount() - 1;
    }

    public void onOpenContainer() {
        this.windowCount++;
    }

    protected boolean canClick(int slot) {
        return canClick(slot, -1);
    }

    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol) || TerminalSolver.getBlockAll().getValue()) return false;
        if (TerminalSolver.getMode().is("Queue")) return this.getHoveredSlot() == slot;
        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getFirstDelay().getValue().longValue()) return false;
        if (TerminalSolver.getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < TerminalSolver.getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    protected int getHoveredSlot() {
        double mouseX = MouseUtils.mouseX();
        double mouseY = MouseUtils.mouseY();
        Window win = mc.getWindow();

        float scale = TerminalSolver.getScale().getValue().floatValue();
        float screenWidth = win.getWidth() / scale;
        float screenHeight = win.getHeight() / scale;

        int gap = 32 + TerminalSolver.getGap().getValue().intValue();
        float windowSize = getSlotCount();
        float width = 9 * gap;
        float height = (windowSize / 9) * gap;

        float offsetX = screenWidth / 2 - width / 2 + 1;
        float offsetY = screenHeight / 2 - height / 2;

        float adjustedMouseX = (float) (mouseX / scale);
        float adjustedMouseY = (float) (mouseY / scale);

        int slotX = (int) ((adjustedMouseX - offsetX) / gap);
        int slotY = (int) ((adjustedMouseY - offsetY) / gap);

        if (slotX < 0 || slotX >= 9 || slotY < 0 || slotY * 9 >= windowSize) {
            return -1;
        }

        return slotX + slotY * 9;
    }

    public void mouseClick(int button) {
        int slot = getHoveredSlot();
        if (slot == -1) return;
        clickSlot(slot, button);
    }

    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;

        if (TerminalSolver.getMode().getIndex() != 0) {
            onZeroPingClick(slot, button, getBySlot(slot));
        }
        if (TerminalSolver.getMode().is("Queue")) {
            onQueueClick();
            return;
        }
        clicked = true;
        click(slot, button);
    }

    protected void click(int slot, int button) {
        if (mc.player == null || mc.gameMode == null || !(mc.screen instanceof ContainerScreen)) return;
        int wid = mc.player.containerMenu.containerId;
        if (wid < 0 || wid > 100 && wid != 127) return;
        int b = button == GLFW.GLFW_MOUSE_BUTTON_1 ? GLFW.GLFW_MOUSE_BUTTON_3 : button;
        ChatUtils.dev("Clicking: %s, last click was %sms ago", slot, System.currentTimeMillis() - Terminals.getClickedAt());
        mc.gameMode.handleInventoryMouseClick(wid, slot, b, b == GLFW.GLFW_MOUSE_BUTTON_3 ? ClickType.CLONE : ClickType.PICKUP, mc.player);
    }

    protected void onQueueClick() {
        if (!clicked && !clickedSlots.isEmpty()) {
            clickFromQueue();
        }
    }

    protected boolean clickFromQueue() {
        Optional<Pair<TermSol, Long>> opt = clickedSlots.values().stream().min(Comparator.comparing(Pair::getSecond));
        if (opt.isPresent()) {
            lastClick = opt.get().getFirst();
            clicked = true;
            long delay = calculateDelay();
            if (delay > 0) {
                int slot = lastClick.getSlot();
                TaskComponent.onMilli(delay, () -> {
                    click(slot, GLFW.GLFW_MOUSE_BUTTON_3);
                });
            } else {
                click(lastClick.getSlot(), GLFW.GLFW_MOUSE_BUTTON_3);
            }
            return true;
        }
        lastClick = null;
        return false;
    }

    protected long calculateDelay() {
        long now = System.currentTimeMillis();
        long firstDelay = TerminalSolver.getFirstDelay().getValue().longValue();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getFirstDelay().getValue().longValue()) {
            return firstDelay - (now - Terminals.getOpenedAt());
        } else {
            return TerminalSolver.getClickDelay().getValue().longValue() - (now - Terminals.getOpenedAt());
        }
    }

    protected void onZeroPingClick(int slot, int button, TermSol sol) {
        if (sol == null) return;
        clickedSlots.put(slot, new Pair<>(sol, System.currentTimeMillis()));
        solution.remove(sol);
    }

    public TermSol getBySlot(int slot) {
        for (TermSol ts : new ArrayList<>(solution)) {
            if (ts != null && ts.getSlot() == slot) return ts;
        }
        return null;
    }

    public TermSol getRawBySlot(int slot) {
        for (TermSol ts : new ArrayList<>(rawSolution)) {
            if (ts != null && ts.getSlot() == slot) return ts;
        }
        return null;
    }

    public void updateSolutionWithPrediction() {
        if (solution.isEmpty()) return;
        if (TerminalSolver.getMode().inRangeInclusive(1, 2)) {
            clickedSlots.forEach((k, v) -> solution.remove(v.getFirst()));
        } else if (TerminalSolver.getMode().is("Queue") && lastClick != null) {
            if (solution.contains(lastClick)) {
                clickedSlots.clear();
            } else {
                clickedSlots.remove(lastClick.getSlot());
                clickedSlots.forEach((k, v) -> solution.remove(v.getFirst()));
            }
            lastClick = null;
        }
    }

    /// Tick
    public void update() {
        if (TerminalSolver.getMode().is("Normal") || TerminalSolver.getMode().is("Queue") || clickedSlots.isEmpty() || rawSolution.isEmpty()) return;
        long now = System.currentTimeMillis();
        long timeout = TerminalSolver.getTimeout().getValue().longValue();
        List<TermSol> pendingUpdate = new ArrayList<>();
        clickedSlots.forEach((k, v) -> {
            if (now - v.getSecond() > timeout) {
                pendingUpdate.add(v.getFirst());
            }
        });
        pendingUpdate.forEach(this::updateWithSol);
    }

    protected void updateWithSol(TermSol sol) {
        if (rawSolution.contains(sol)) {
            clickedSlots.remove(sol.getSlot());
            if (!solution.contains(sol)) {
                solution.add(sol);
            }
        } else {
            solution.remove(sol);
        }
    }

    public void setupRender() {
        float scale = TerminalSolver.getScale().getValue().floatValue();
        Window w = mc.getWindow();

        float screenWidth = w.getWidth() / scale;
        float screenHeight = w.getHeight() / scale;

        float gap = 32 + TerminalSolver.getGap().getValue().floatValue();
        float width = 9 * gap   ;
        float height = this.getSlotCount() / 9f * gap;

        float offsetX = screenWidth / 2f - width / 2f + 1f;
        float offsetY = screenHeight / 2f - height / 2f;

        NVGUtils.scale(scale);

        NVGUtils.drawRect(offsetX - 4, offsetY - 4, width + 8, height + 8, TerminalSolver.getBackground().getValue());

        if (TerminalSolver.getTitles().getValue()) {
            String title = this.getTitle();
            NVGUtils.drawText(title.isBlank()
                            ? this.guiTitle
                            : title,
                    offsetX,
                    offsetY,
                    20,
                    TerminalSolver.getTextColour().getValue(),
                    NVGUtils.JOSEFIN);
        }

        this.render(offsetX, offsetY, gap);
    }

    public void setClicked() {
        clicked = true;
    }

    public abstract void solve();

    public abstract int getSlotCount();

    public abstract boolean shouldRender();

    public void render(float x, float y, float gap) {
        this.render(x, y, gap, false);
    }

    public abstract void render(float x, float y, float gap, boolean noInteraction);

    public abstract TerminalType getType();

    public abstract String getTitle();
}
