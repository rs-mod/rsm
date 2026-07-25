package com.ricedotwho.rsm.ui.termsim;

import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderSim extends TermSimScreen {
    private final List<SolutionSlot> solution = new ArrayList<>();

    public OrderSim() {
        super(TerminalType.ORDER.getGuiName(), TerminalType.ORDER.getSize());
        for (int i = 0; i < 14; i++) {
            solution.add(null);
        }
        create();
    }

    @Override
    protected TerminalType getType() {
        return TerminalType.ORDER;
    }

    @Override
    public void slotClick(Slot slot, int button) {
        if (this.solution.getFirst().index == slot.index) {
            SolutionSlot sol = this.solution.removeFirst();
            this.onClicked();
            if (this.solution.isEmpty()) {
                onComplete();
                return;
            } else {
                this.playSound();
            }
            ItemStack stack = namedStack(Items.LIME_STAINED_GLASS_PANE, "");
            stack.setCount(sol.amount);
            set(slot, stack, false);
            reSend();
        } else {
            ChatUtils.chatClean(Component.literal("Wrong number!").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void create() {
        sendOpenEvent();
        List<Integer> unused = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            unused.add(i);
        }
        Collections.shuffle(unused);

        getSlots().forEach(slot -> {
            int row = slot.index / 9;
            int col = slot.index % 9;
            ItemStack stack;
            if ((row == 1 || row == 2) && col >= 1 && col <= 7) {
                int amount = unused.removeFirst();

                stack = namedStack(Items.RED_STAINED_GLASS_PANE, "");
                stack.setCount(amount);
                solution.set(amount - 1, new SolutionSlot(slot.index, amount));
            } else {
                stack = BLACK_PANE;
            }
            set(slot, stack);
        });
    }

    private record SolutionSlot(int index, int amount) {}
}
