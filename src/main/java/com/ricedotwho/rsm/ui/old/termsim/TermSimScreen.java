package com.ricedotwho.rsm.ui.old.termsim;

import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import com.ricedotwho.rsm.utils.Utils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class TermSimScreen extends ContainerScreen implements Accessor {
    protected static final ItemStack BLACK_PANE = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);

    static {
        BLACK_PANE.set(DataComponents.CUSTOM_NAME, Component.literal(""));
    }

    protected final int size;
    protected String name;

    public TermSimScreen(String name, int size) {
        assert mc.player != null;
        super(new ChestMenu(Utils.getMenuTypeByCount(size), 0, new Inventory(mc.player, new PlayerEquipment(mc.player)), new SimpleContainer(size), size / 9), new Inventory(mc.player, new PlayerEquipment(mc.player)), Component.literal(name));
        this.size = size;
        this.name = name;
    }

    protected abstract TerminalType getType();

    protected List<Slot> getSlots() {
        return menu.slots.subList(0, size);
    }

    public void create() {
        sendOpenEvent();
        getSlots().forEach(slot -> set(slot, BLACK_PANE));
    }


    @Override
    protected void slotClicked(Slot slot, int slotId, int buttonNum, @NonNull ContainerInput containerInput) {
        if (slot.container != this.menu.container || slot.getItem().getItem() == Items.BLACK_STAINED_GLASS_PANE) return;
        slotClick(slot, buttonNum);
    }

    @Override
    public void onClose() {
        Terminals.onTermSimClose(true);
        super.onClose();
    }

    public void slotClick(int index, int button) {
        Slot slot = this.menu.getSlot(index);
        if (slot.container != this.menu.container|| slot.getItem().getItem() == Items.BLACK_STAINED_GLASS_PANE) return;
        slotClick(slot, button);
    }

    public abstract void slotClick(Slot slot, int button);

    protected void set(Slot slot, ItemStack stack) {
        this.set(slot, stack, true);
    }

    protected void set(Slot slot, ItemStack stack, boolean send) {
        slot.set(stack);
        if (send) Terminals.getCurrent().onSlot(slot.index, stack);
    }

    protected void onComplete() {
        Terminals.onTermSimClose(false);
        mc.setScreen(null);
        TerminalSolver.getInstance().getCompleteSoundSound().play();
    }

    protected void playSound() {
        TerminalSolver.getInstance().getSound().play();
    }

    protected void onClicked() {
        Terminals.simulateClick();
    }

    protected void sendOpenEvent() {
        Terminals.onTermSimOpen(getType(), name);
    }

    protected ItemStack namedStack(Item item, String name, boolean glint) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
        return stack;
    }

    protected ItemStack namedStack(Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    public static void open(TerminalType type) {
        if (mc.screen != null && !(mc.screen instanceof TermSimScreen)) return;
        if (Terminals.getCurrent() != null) {
            Terminals.onTermSimClose(true);
        }
        TermSimScreen screen = switch (type) {
            case ORDER -> new OrderSim();
            default -> null;
        };
        if (screen == null) {
            ChatUtils.chat("Failed to create termsim for {}?", type);
            return;
        }
        mc.setScreen(screen);
        screen.create();
    }
}
