package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class Rubix extends Term {
    private static final List<Item> COLOUR_ORDER = List.of(Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE);
    private static final List<MutableComponent> RUBIX_NAMES = List.of(
            Component.literal("Red").withStyle(ChatFormatting.GREEN),
            Component.literal("Orange").withStyle(ChatFormatting.GREEN),
            Component.literal("Yellow").withStyle(ChatFormatting.GREEN),
            Component.literal("Green").withStyle(ChatFormatting.GREEN),
            Component.literal("Blue").withStyle(ChatFormatting.GREEN)
    );
    private Item lastSolution = null;

    public Rubix(String title) {
        super(title);
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
        clickedSlots.forEach((k, v) -> {
            if (v.getFirst().getClicks() == 0 || v.getFirst().getClicks() == 5) {
                solution.remove(getBySlot(v.getFirst().getSlot()));
            } else {
                TermSol ts = getBySlot(k);
                if (ts != null) ts.setClicks(v.getFirst().getClicks());
            }
        });
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
        return TerminalSolver.getInstance().getRubixTitle().getValue();
    }
}
