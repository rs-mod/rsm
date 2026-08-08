package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Colour;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Order");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            int index = solution.indexOf(sol);

            Colour colour = switch (index) {
                case 0 -> TerminalSolver.getInstance().getOrder().getValue();
                case 1 -> TerminalSolver.getInstance().getOrder2().getValue();
                case 2 -> TerminalSolver.getInstance().getOrder3().getValue();
                default -> null;
            };

            if (colour == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && index == 0 && canClick(i, 0)) {
                colour = TerminalSolver.getInstance().getCanClickColour().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
            if (TerminalSolver.getInstance().getOrderNumbers().getValue()) {
                String text = Integer.toString(sol.getClicks());
                Font font = NVGUtils.getFont(NVGUtils.JOSEFIN);
                NVGUtils.drawTextShadow(text,
                        slotX + (32 - NVGUtils.getTextWidth(text, 24, font)) / 2,
                        slotY + (32 - NVGUtils.getTextHeight(text, 24, font)) / 2,
                        24,
                        TerminalSolver.getInstance().getTextColour().getValue(),
                        font
                );
            }
        }
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || solution.indexOf(sol) != 0 || TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        if (TerminalSolver.getInstance().getMode().is("Queue")) return this.getHoveredSlot() == slot;
        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getInstance().getFirstDelay().getValue().longValue() || now - Terminals.getClickedAt() < TerminalSolver.getInstance().getClickDelay().getValue().longValue()) return false;
        if (TerminalSolver.getInstance().getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < TerminalSolver.getInstance().getClickDelay().getValue().longValue()) return false;
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
        return TerminalSolver.getInstance().getOrderTitle().getValue();
    }

    @Override
    public int getPrediction(int slot, ContainerInput input) {
        Map<Integer, ItemStack> items = new HashMap<>(packetItems);
        ItemStack prev = items.get(slot);
        ItemStack pane = new ItemStack(Items.LIME_STAINED_GLASS_PANE.builtInRegistryHolder(), prev.getCount(), prev.getComponentsPatch());
        items.put(slot, pane);
        return this.slotsHashCode(items);
    }
}
