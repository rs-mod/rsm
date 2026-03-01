package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.world.item.Items;

import java.util.Comparator;

public class Order extends Term {

    public Order(String title) {
        super(title);
    }

    @Override
    public void solve() {
        packetItems.forEach((slot, item) -> {
            if (item.getItem() == Items.RED_STAINED_GLASS_PANE) {
                solution.add(new TermSol(slot, item.getCount()));
            }
        });

        solution.sort(Comparator.comparingInt(TermSol::getClicks));
    }

    @Override
    public int getSlotCount() {
        return 9*4;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getOrderEnabled().getValue();
    }

    @Override
    public void render(float x, float y, float width, float height, float gap) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            int index = solution.indexOf(sol);

            Colour colour = switch (index) {
                case 0 -> TerminalSolver.getOrder().getValue();
                case 1 -> TerminalSolver.getOrder2().getValue();
                case 2 -> TerminalSolver.getOrder3().getValue();
                default -> null;
            };

            if (colour == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            if (TerminalSolver.getCanClick().getValue() && index == 0 && canClick(i, 0)) {
                colour = TerminalSolver.getCanClickColour().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
            if (TerminalSolver.getOrderNumbers().getValue()) {
                String text = Integer.toString(sol.getClicks());
                NVGUtils.drawTextShadow(text,
                        slotX + (32 - NVGUtils.getTextWidth(text, 24, NVGUtils.JOSEFIN)) / 2,
                        slotY + (32 - NVGUtils.getTextHeight(text, 24, NVGUtils.JOSEFIN)) / 2,
                        24,
                        TerminalSolver.getTextColour().getValue(),
                        NVGUtils.JOSEFIN
                );
            }
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || solution.indexOf(sol) != 0 || TerminalSolver.getBlockAll().getValue()) return false;
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
    protected void updateWithSol(TermSol sol) {
        if (rawSolution.contains(sol)) {
            clickedSlots.remove(sol.getSlot());
            if (!solution.contains(sol)) {
                solution.add(sol);
                solution.sort(Comparator.comparingInt(TermSol::getClicks));
            }
        } else {
            solution.remove(sol);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.ORDER;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getOrderTitle().getValue();
    }
}
