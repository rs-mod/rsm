package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class Rubix extends Term {
    private static final List<Item> COLOR_ORDER = List.of(Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE);
    private Item lastSolution = null;

    public Rubix(String title) {
        super(title);
    }

    @Override
    public void solve() {
        if (lastSolution != null) {
            int lastIndex = COLOR_ORDER.indexOf(lastSolution);

            packetItems.forEach((slot, item) -> {
                if (!item.isEmpty() && isRubixPane(item.getItem())) {
                    int idx =  COLOR_ORDER.indexOf(item.getItem());
                    if (idx != lastIndex) {
                        solution.add(new TermSol(slot, dist(idx, lastIndex)));
                    }
                }
            });

        } else {
            List<TermSol> temp = new ArrayList<>();

            for (Item pane : COLOR_ORDER) {
                int target =  COLOR_ORDER.indexOf(pane);

                List<TermSol> temp2 = new ArrayList<>();

                packetItems.forEach((slot, item) -> {
                    if (!item.isEmpty() && isRubixPane(item.getItem())) {
                        int idx =  COLOR_ORDER.indexOf(item.getItem());
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
        return pane > most ? (most + COLOR_ORDER.size()) - pane : most - pane;
    }

    private boolean isRubixPane(Item item) {
        return COLOR_ORDER.contains(item);
    }

    @Override
    public int getSlotCount() {
        return 9*5;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getTerminals().get("Rubix");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            int realClicks = getRealClicks(sol);

            Colour colour;
            if (!noInteraction && TerminalSolver.getCanClick().getValue() && canClick(i)) {
                colour = TerminalSolver.getCanClickColour().getValue();
            } else {
                colour = realClicks > 0 ? TerminalSolver.getRubix().getValue() : TerminalSolver.getOppRubix().getValue();
             }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
            String text = Integer.toString(realClicks);
            NVGUtils.drawTextShadow(text,
                    slotX + (32 - NVGUtils.getTextWidth(text, 24, NVGUtils.JOSEFIN)) / 2,
                    slotY + (32 - NVGUtils.getTextHeight(text, 24, NVGUtils.JOSEFIN)) / 2,
                    24,
                    TerminalSolver.getTextColour().getValue(),
                    NVGUtils.JOSEFIN
            );
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol) || TerminalSolver.getBlockAll().getValue()) return false;
        if ((button != -1 && sol.getClicks() > 2) == (button != 1)) return false;
        if (TerminalSolver.getMode().is("Queue")) return this.getHoveredSlot() == slot;
        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getFirstDelay().getValue().longValue() || now - Terminals.getClickedAt() < TerminalSolver.getClickDelay().getValue().longValue()) return false;
        if (TerminalSolver.getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < TerminalSolver.getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    @Override
    protected void onZeroPingClick(int slot, int button, TermSol sol) {
        if (sol == null) return;
        if (TerminalSolver.getMode().is("Queue")) {
            int dir = sol.getClicks() > 2 ? -1 : 1;
            if (clickedSlots.containsKey(sol.getSlot())) {
                TermSol existing = clickedSlots.get(sol.getSlot()).getFirst();
                existing.setClicks(existing.getClicks() + dir);
            } else {
                clickedSlots.put(sol.getSlot(), new Pair<>(new TermSol(sol.getSlot(), dir), System.currentTimeMillis()));
            }
        } else {
            clickedSlots.put(sol.getSlot(), new Pair<>(sol, System.currentTimeMillis()));
        }
        if (sol.getClicks() == 0 || sol.getClicks() == 5) {
            solution.removeIf(ts -> ts.getSlot() == slot);
        }
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;

        if (TerminalSolver.getMode().getIndex() != 0) {
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
        }
        if (TerminalSolver.getMode().is("Queue")) {
            onQueueClick();
            return;
        }
        clicked = true;
        this.click(slot, button);
    }

    @Override
    protected boolean clickFromQueue() {
        Optional<Pair<TermSol, Long>> opt = clickedSlots.values().stream().min(Comparator.comparing(Pair::getSecond));
        if (opt.isPresent()) {
            lastClick = opt.get().getFirst();
            int button = lastClick.getClicks() > 0 ? GLFW.GLFW_MOUSE_BUTTON_3 : GLFW.GLFW_MOUSE_BUTTON_2;
            clicked = true;
            long delay = calculateDelay();
            if (delay > 0) {
                int slot = lastClick.getSlot();
                TaskComponent.onMilli(delay, () -> click(slot, button));
            } else {
                click(lastClick.getSlot(), button);
            }
            return true;
        }
        lastClick = null;
        return false;
    }

    @Override
    protected void updateWithSol(TermSol sol) {
        TermSol raw = rawBySlot(sol.getSlot());
        TermSol real = getBySlot(sol.getSlot());
        if (raw == null) {
            solution.remove(getBySlot(sol.getSlot()));
        } else if (real == null) {
            clickedSlots.remove(sol.getSlot());
            sol.setClicks(raw.getClicks());
            solution.add(sol);
        } else {
            sol.setClicks(raw.getClicks());
        }
    }

    @Override
    public void updateSolutionWithPrediction() {
        if (solution.isEmpty()) return;
        if (TerminalSolver.getMode().inRangeInclusive(1, 2)) {
            clickedSlots.forEach((k, v) -> {
                if (v.getFirst().getClicks() == 0 || v.getFirst().getClicks() == 5) {
                    solution.remove(getBySlot(v.getFirst().getSlot()));
                } else {
                    TermSol ts = getBySlot(k);
                    if (ts != null) ts.setClicks(v.getFirst().getClicks());
                }
            });
        } else if (TerminalSolver.getMode().is("Queue") && lastClick != null) {
            if (solution.contains(lastClick)) {
                clickedSlots.clear();
            } else {
                decrement(lastClick);
                if (lastClick.getClicks() == 0) clickedSlots.remove(lastClick.getSlot());

                clickedSlots.forEach((k, v) -> {
                    if (v.getFirst().getClicks() == 0 || v.getFirst().getClicks() == 5) {
                        solution.remove(getBySlot(v.getFirst().getSlot()));
                    } else {
                        TermSol ts = getBySlot(k);
                        if (ts != null) {
                            ts.setClicks(ts.getClicks() - v.getFirst().getClicks());
                            if (ts.getClicks() == 0 || ts.getClicks() == 5)
                                solution.remove(getBySlot(v.getFirst().getSlot()));
                        }
                    }
                });
            }
            lastClick = null;
        }
    }

    private void decrement(TermSol sol) {
        int c = sol.getClicks();
        if (c > 0) {
            sol.setClicks(c - 1);
        } else {
            sol.setClicks(c + 1);
        }
    }

    private int getRealClicks(TermSol sol) {
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
        return TerminalSolver.getRubixTitle().getValue();
    }
}
