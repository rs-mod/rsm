package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Items;

import java.util.Comparator;

public class Order extends Term {

    public Order(String title) {
        super(title);
    }

    @Override
    public void solve() {
        packetItems.forEach((slot, item) -> {
            if (item.getItem() == Items.STAINED_GLASS_PANE.red()) {
                solution.add(new TermSol(slot, item.getCount()));
            }
        });

        solution.sort(Comparator.comparingInt(TermSol::getClicks));
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Order");
    }

    @Override
    public void render(GuiGraphicsExtractor gfx, float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            int index = solution.indexOf(sol);

            Color color = switch (index) {
                case 0 -> TerminalSolver.getInstance().getOrder().getValue();
                case 1 -> TerminalSolver.getInstance().getOrder2().getValue();
                case 2 -> TerminalSolver.getInstance().getOrder3().getValue();
                default -> null;
            };

            if (color == null) continue;

            var slotX = i % 9 * gap + x;
            var slotY = (float) Math.floor(i / 9f) * gap + y;

            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && index == 0 && canClick(i, 0)) {
                color = TerminalSolver.getInstance().getCanClickColor().getValue();
            }

            Render2D.rect(gfx, slotX, slotY, 32, 32, color);
            if (TerminalSolver.getInstance().getOrderNumbers().getValue()) {
                String text = Integer.toString(sol.getClicks());

                Fonts.text(gfx, Fonts.JOSEFIN,
                        text,
                        slotX + (32 - Fonts.width(text, 16)) / 2f,
                        slotY + 16,
                        16,
                        TerminalSolver.getInstance().getTextColor().getValue()
                );
            }
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || solution.indexOf(sol) != 0 || TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        long now = System.currentTimeMillis();
        if (now - Terminals.getClickedAt() < TerminalSolver.getInstance().getClickDelay().getValue().longValue()) return false;
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
        return TerminalSolver.getInstance().getOrderTitle().getValue();
    }
}
