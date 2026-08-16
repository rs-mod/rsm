package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class Panes extends Term {

    public Panes(String title) {
        super(title);
    }

    @Override
    public void solve() {
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && item.getItem() == Items.RED_STAINED_GLASS_PANE) {
                solution.add(new TermSol(slot));
            }
        });
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Panes");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            Color color;
            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && canClick(i)) {
                color = TerminalSolver.getInstance().getCanClickColor().getValue();
            } else {
                color = TerminalSolver.getInstance().getPanesColor().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, color);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.PANES;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getInstance().getPanesTitle().getValue();
    }

    @Override
    public int getPrediction(int slot, ContainerInput input) {
        Map<Integer, ItemStack> items = new HashMap<>(packetItems);
        ItemStack prev = items.get(slot);
        ItemStack pane = new ItemStack(Items.LIME_STAINED_GLASS_PANE.builtInRegistryHolder(), prev.getCount(), prev.getComponentsPatch());
        // Why is the empty component italic, what is hypixel cooking
        pane.set(DataComponents.CUSTOM_NAME, Component.empty().withStyle(ChatFormatting.ITALIC).append(Component.literal("On").withStyle(ChatFormatting.GREEN)));
        items.put(slot, pane);
        return this.slotsHashCode(items);
    }
}
