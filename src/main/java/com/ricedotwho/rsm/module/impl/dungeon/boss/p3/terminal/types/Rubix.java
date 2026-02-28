package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.Terminals;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return Terminals.getRubixEnabled().getValue();
    }

    @Override
    public void render(float x, float y, float width, float height, float gap) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            Colour colour;
            if (Terminals.getCanClick().getValue() && canClick(i)) {
                colour = Terminals.getCanClickColour().getValue();
            } else {
                colour = sol.getClicks() > 2 ? Terminals.getOppRubix().getValue() : Terminals.getRubix().getValue();
             }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
            String text = Integer.toString(sol.getClicks() > 2 ? sol.getClicks() - 5 : sol.getClicks());
            NVGUtils.drawTextShadow(text,
                    slotX + (32 - NVGUtils.getTextWidth(text, 24, NVGUtils.JOSEFIN)) / 2,
                    slotY + (32 - NVGUtils.getTextHeight(text, 24, NVGUtils.JOSEFIN)) / 2,
                    24,
                    Terminals.getTextColour().getValue(),
                    NVGUtils.JOSEFIN
            );
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol)) return false;
        if ((button != -1 && sol.getClicks() > 2) == (button != 1)) return false;

        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < Terminals.getFirstDelay().getValue().longValue() || now - Terminals.getClickedAt() < Terminals.getClickDelay().getValue().longValue()) return false;
        if (Terminals.getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < Terminals.getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    @Override
    protected void onZeroPingClick(int slot, int button, TermSol sol) {
        if (sol == null) return;
        clickedSlots.put(sol.getSlot(), new Pair<>(sol, System.currentTimeMillis()));
        if (sol.getClicks() == 0 || sol.getClicks() == 5) {
            solution.removeIf(ts -> ts.getSlot() == slot);
        }
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;
        clicked = true;

        if (Terminals.getMode().getIndex() != 0) {
            TermSol sol = getBySlot(slot);

            int realClicks = sol.getClicks() > 2 ? sol.getClicks() - 5 : sol.getClicks();

            if (button == 1) {
                if (realClicks > 0) return;
                sol.setClicks(sol.getClicks() + 1);
            } else {
                if (realClicks < 0) return;
                sol.setClicks(sol.getClicks() - 1);
            }

            onZeroPingClick(slot, button, sol);
        }

        this.click(slot, button);
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
        if (Terminals.getMode().getIndex() != 0 && !solution.isEmpty()) {
            clickedSlots.forEach((k, v) -> {
                if (v.getFirst().getClicks() == 0 || v.getFirst().getClicks() == 5) {
                    solution.remove(getBySlot(v.getFirst().getSlot()));
                } else {
                    TermSol ts = getBySlot(k);
                    if (ts != null) ts.setClicks(v.getFirst().getClicks());
                }
            });
        }
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
        return Terminals.getRubixTitle().getValue();
    }
}
