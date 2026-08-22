package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.ui.old.termsim.TermSimScreen;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Getter
public abstract class Term implements Accessor {
    @Getter
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

            //ChatUtils.chat("New terminal window: {}. {}", solution, rawSolution);

            if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.QUEUE) && !clickedSlots.isEmpty()) {
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
        if (sol == null || !solution.contains(sol) || TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.QUEUE)) return this.getHoveredSlot() == slot;
        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getInstance().getFirstDelay().getValue().longValue()) return false;
        if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.ZERO_PING)) {
            if (now - Terminals.getClickedAt() < TerminalSolver.getInstance().getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    protected int getHoveredSlot() {
        double mouseX = MouseUtils.mouseX();
        double mouseY = MouseUtils.mouseY();
        Window win = mc.getWindow();

        float scale = TerminalSolver.getInstance().getScale().getValue();
        float screenWidth = win.getScreenWidth() / scale;
        float screenHeight = win.getScreenHeight() / scale;

        int gap = 32 + TerminalSolver.getInstance().getGap().getValue().intValue();
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

        if (TerminalSolver.getInstance().getMode().getIndex() != 0) {
            onZeroPingClick(slot, button, getBySlot(slot));
        }
        if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.QUEUE)) {
            onQueueClick();
            return;
        }
        clicked = true;
        click(slot, button);
    }

    protected final void click(int slot, int button) {
        if (mc.player == null || mc.gameMode == null) return;

        if (mc.screen instanceof TermSimScreen sim) {
            sim.slotClick(slot, button);
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        int wid = menu.containerId;
        if (wid < 0 || wid > 100 && wid != 127 || menu.slots.size() < slot) return;
        int b = button == GLFW.GLFW_MOUSE_BUTTON_1 ? GLFW.GLFW_MOUSE_BUTTON_3 : button;
        ChatUtils.dev("Clicking: {}, last click was {}ms ago", slot, System.currentTimeMillis() - Terminals.getClickedAt());
        mc.gameMode.handleContainerInput(wid, slot, b, b == GLFW.GLFW_MOUSE_BUTTON_3 ? ContainerInput.CLONE : ContainerInput.PICKUP, mc.player);
    }

    protected void onQueueClick() {
        if (!clicked && !clickedSlots.isEmpty() && !(mc.screen instanceof TermSimScreen)) {
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
                Scheduler.scheduleMilliseconds(delay, () -> click(slot, GLFW.GLFW_MOUSE_BUTTON_3));
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
        long firstDelay = TerminalSolver.getInstance().getFirstDelay().getValue().longValue();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getInstance().getFirstDelay().getValue().longValue()) {
            return firstDelay - (now - Terminals.getOpenedAt());
        } else {
            return TerminalSolver.getInstance().getClickDelay().getValue().longValue() - (now - Terminals.getOpenedAt());
        }
    }

    protected void onZeroPingClick(int slot, int button, TermSol sol) {
        if (sol == null || mc.screen instanceof TermSimScreen) return;
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
        if (TerminalSolver.getInstance().getMode().inRangeInclusive(1, 2)) {
            clickedSlots.forEach((_, v) -> solution.remove(v.getFirst()));
        } else if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.QUEUE) && lastClick != null) {
            if (solution.contains(lastClick)) {
                clickedSlots.clear();
            } else {
                clickedSlots.remove(lastClick.getSlot());
                clickedSlots.forEach((_, v) -> solution.remove(v.getFirst()));
            }
            lastClick = null;
        }
    }

    /// Tick
    public void update() {
        if (TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.NORMAL) || TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.QUEUE) || clickedSlots.isEmpty() || rawSolution.isEmpty()) return;
        long now = System.currentTimeMillis();
        long timeout = TerminalSolver.getInstance().getTimeout().getValue().longValue();
        List<TermSol> pendingUpdate = new ArrayList<>();
        clickedSlots.forEach((_, v) -> {
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
        float scale = TerminalSolver.getInstance().getScale().getValue();
        Window w = mc.getWindow();

        float screenWidth = w.getScreenWidth() / scale;
        float screenHeight = w.getScreenHeight() / scale;

        float gap = 32 + TerminalSolver.getInstance().getGap().getValue();
        float width = 9 * gap   ;
        float height = this.getSlotCount() / 9f * gap;

        float offsetX = screenWidth / 2f - width / 2f + 1f;
        float offsetY = screenHeight / 2f - height / 2f;

        NVGUtils.scale(scale);

        NVGUtils.drawRect(offsetX - 4, offsetY - 4, width + 8, height + 8, TerminalSolver.getInstance().getBackground().getValue());

        if (TerminalSolver.getInstance().getTitles().getValue()) {
            String title = this.getTitle();
            NVGUtils.drawText(title.isBlank()
                            ? this.guiTitle
                            : title,
                    offsetX,
                    offsetY,
                    20,
                    TerminalSolver.getInstance().getTextColor().getValue(),
                    NVGUtils.getFont(NVGUtils.JOSEFIN));
        }

        this.render(offsetX, offsetY, gap);
    }

    public void setClicked() {
        clicked = true;
    }

    public abstract void solve();

    public int getSlotCount() {
        return getType().getSize();
    }

    public abstract boolean shouldRender();

    public void render(float x, float y, float gap) {
        this.render(x, y, gap, false);
    }

    public abstract void render(float x, float y, float gap, boolean noInteraction);

    public abstract TerminalType getType();

    public abstract String getTitle();

    public void onClose() {

    }

    public abstract int getPrediction(int slot, ContainerInput input);

    @Override
    public int hashCode() {
        return slotsHashCode(packetItems);
    }

    protected int slotsHashCode(Map<Integer, ItemStack> items) {
        int hash = 1;
        for (ItemStack stack : items.values()) {
            hash = hashStack(stack, hash);
        }
        return hash;
    }

    private int hashStack(ItemStack stack, int hash) {
        hash = 31 * hash + stack.getCount();
        hash = 31 * hash + stack.hashCode();
        hash = 31 * hash + (stack.isEnchanted() || Boolean.TRUE.equals(stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) ? 1 : 0);
        return hash;
    }
}
