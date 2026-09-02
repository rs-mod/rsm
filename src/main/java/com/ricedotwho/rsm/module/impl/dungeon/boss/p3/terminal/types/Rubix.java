package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rubix extends Term {
    private static final List<Item> COLOUR_ORDER = List.of(Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE);

    private Item lastSolution = null;

    private boolean solved = false;

    public Rubix(String title) {
        super(title);
    }

    @Override
    public void onSlot(int slot, ItemStack item) {
        if (slot < 0) return;
        packetItems.put(slot, item);

        ChatUtils.chat("set slot {} {}", slot, item.getItem());

        if (solved && slot < getSlotCount()) {
            rawSolution.clear();
            solveSlot(slot, item.getItem());
            rawSolution.addAll(solution.stream().map(TermSol::copy).toList());
            updateSolutionWithPrediction();
            return;
        }

        if (canSolve()) {
            solution.clear();
            rawSolution.clear();
            solve();
            rawSolution.addAll(solution.stream().map(TermSol::copy).toList());
            updateSolutionWithPrediction();
            clicked = false;
            solved = true;
        }
    }

    private void solveSlot(int slot, Item item) {
        int lastIndex = COLOUR_ORDER.indexOf(lastSolution);
        int idx = COLOUR_ORDER.indexOf(item);
        if (idx != lastIndex) {
            TermSol sol = getBySlot(slot);
            if (sol == null) {
                solution.add(new TermSol(slot, dist(idx, lastIndex)));
            } else {
                sol.setClicks(dist(idx, lastIndex));
            }
        }
    }

    @Override
    public void solve() {
        if (lastSolution != null) {
            int lastIndex = COLOUR_ORDER.indexOf(lastSolution);

            packetItems.forEach((slot, item) -> {
                if (!item.isEmpty() && isRubixPane(item.getItem())) {
                    int idx =  COLOUR_ORDER.indexOf(item.getItem());
                    if (idx != lastIndex) {
                        solution.add(new TermSol(slot, dist(idx, lastIndex)));
                    }
                }
            });

        } else {
            List<TermSol> temp = new ArrayList<>();

            for (Item pane : COLOUR_ORDER) {
                int target =  COLOUR_ORDER.indexOf(pane);

                List<TermSol> temp2 = new ArrayList<>();

                packetItems.forEach((slot, item) -> {
                    if (!item.isEmpty() && isRubixPane(item.getItem())) {
                        int idx =  COLOUR_ORDER.indexOf(item.getItem());
                        if (idx != target) {
                            temp2.add(new TermSol(slot, dist(idx, target)));
                        }
                    }
                });

                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2;
                    lastSolution = pane;
                }
            }

            solution.addAll(temp);
        }
    }

    private int getRealSize(List<TermSol> list) {
        if (list.isEmpty()) return 100;
        int size = 0;
        List<Integer> uniqueSlots = new ArrayList<>();
        for (TermSol termSol : list) {
            if (!uniqueSlots.contains(termSol.getSlot())) {
                uniqueSlots.add(termSol.getSlot());
                int count = (int) list.stream().filter(ts -> Objects.equals(ts.getSlot(), termSol.getSlot())).count();
                size += (count >= 3) ? (5 - count) : count;
            }
        }
        return size;
    }

    private int dist(int pane, int most) {
        return pane > most ? (most + COLOUR_ORDER.size()) - pane : most - pane;
    }

    private boolean isRubixPane(Item item) {
        return COLOUR_ORDER.contains(item);
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Rubix");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            int realClicks = getRealClicks(sol);

            Color color;
            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && canClick(i)) {
                color = TerminalSolver.getInstance().getCanClickColor().getValue();
            } else {
                color = realClicks > 0 ? TerminalSolver.getInstance().getRubix().getValue() : TerminalSolver.getInstance().getOppRubix().getValue();
             }

            NVGUtils.drawRect(slotX, slotY, 32, 32, color);
            String text = Integer.toString(realClicks);
            Font font = NVGUtils.getFont(NVGUtils.JOSEFIN);
            NVGUtils.drawTextShadow(text,
                    slotX + (32 - NVGUtils.getTextWidth(text, 24, font)) / 2,
                    slotY + (32 - NVGUtils.getTextHeight(text, 24, font)) / 2,
                    24,
                    TerminalSolver.getInstance().getTextColor().getValue(),
                    font
            );
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol) || TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        if ((button != -1 && sol.getClicks() > 2) == (button != 1)) return false;
        long now = System.currentTimeMillis();
        if (now - Terminals.getClickedAt() < TerminalSolver.getInstance().getClickDelay().getValue().longValue()) return false;
        return this.getHoveredSlot() == slot;
    }

    @Override
    public void onZeroPingClick(int slot, int button, TermSol sol) {
        if (sol == null) return;
        clickedSlots.put(sol.getSlot(), new Pair<>(sol, EventDispatcher.getTotalWorldTime()));
        if (sol.getClicks() == 0 || sol.getClicks() == 5) {
            solution.removeIf(ts -> ts.getSlot() == slot);
        }
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;

        TermSol sol = getBySlot(slot);

        int realClicks = getRealClicks(sol);

        if (button == 1) {
            if (realClicks > 0) return;
            sol.setClicks(sol.getClicks() + 1);
        } else {
            if (realClicks < 0) return;
            sol.setClicks(sol.getClicks() - 1);
        }

        onZeroPingClick(slot, button, sol);

        clicked = true;
        this.click(slot, button);
    }

    @Override
    public void clickSlotBypass(int slot, int button) {
        TermSol sol = getBySlot(slot);

        int realClicks = getRealClicks(sol);

        if (button == 1) {
            if (realClicks > 0) return;
            sol.setClicks(sol.getClicks() + 1);
        } else {
            if (realClicks < 0) return;
            sol.setClicks(sol.getClicks() - 1);
        }

        onZeroPingClick(slot, button, sol);

        clicked = true;
        this.click(slot, button);
    }

    @Override
    protected void updateWithSol(TermSol sol) {
        TermSol raw = rawBySlot(sol.getSlot());
        TermSol real = getBySlot(sol.getSlot());
        clickedSlots.remove(sol.getSlot());
        if (raw == null) {
            solution.remove(getBySlot(sol.getSlot()));
        } else if (real == null) {
            sol.setClicks(raw.getClicks());
            solution.add(sol);
        } else {
            sol.setClicks(raw.getClicks());
        }
    }

    @Override
    public void updateSolutionWithPrediction() {
        if (solution.isEmpty()) return;
        clickedSlots.forEach((k, v) -> {
            if (v.getFirst().getClicks() == 0 || v.getFirst().getClicks() == 5) {
                solution.remove(getBySlot(v.getFirst().getSlot()));
            } else {
                TermSol ts = getBySlot(k);
                if (ts != null) ts.setClicks(v.getFirst().getClicks());
            }
        });
    }

    protected int getRealClicks(TermSol sol) {
        return sol.getClicks() > 2 ? sol.getClicks() - 5 : sol.getClicks();
    }

    private TermSol rawBySlot(int slot) {
        for (TermSol ts : rawSolution) {
            if (ts.getSlot() == slot) return ts;
        }
        return null;
    }

    @Override
    public TerminalType getType() {
        return TerminalType.RUBIX;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getInstance().getRubixTitle().getValue();
    }
}
