package com.ricedotwho.rsm.ui.termsim;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
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

import java.util.List;

public abstract class TermSimScreen extends ContainerScreen implements Accessor {
    protected static final ItemStack BLACK_PANE = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);

    static {
        BLACK_PANE.set(DataComponents.CUSTOM_NAME, Component.literal(""));
    }

    protected final int size;
    protected String name;
    protected boolean canClick = true;

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
        getSlots().forEach(slot -> set(slot, BLACK_PANE));
    }


    @Override
    protected void slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput) {
        if (!canClick || slot.container != this.menu.container || slot.getItem().getItem() == Items.BLACK_STAINED_GLASS_PANE) return;
        slotClick(slot, buttonNum);
    }

    @Override
    public void onClose() {
        canClick = true;
        RSM.getComponent(Terminals.class).onTermSimClose(true);
        super.onClose();
    }

    public void slotClick(int index, int button) {
        Slot slot = this.menu.getSlot(index);
        if (!canClick || slot.container != this.menu.container|| slot.getItem().getItem() == Items.BLACK_STAINED_GLASS_PANE) return;
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

    protected void reSend() {
        ClientboundOpenScreenPacket fakePacket = new ClientboundOpenScreenPacket(0, Utils.getMenuTypeByCount(getType().getSize()), Component.literal(name));
        RSM.getComponent(Terminals.class).openTermSim(fakePacket, getType());
        getSlots().forEach(slot -> Terminals.getCurrent().onSlot(slot.index, slot.getItem()));
    }

    protected void onComplete() {
        RSM.getComponent(Terminals.class).onTermSimClose(false);
        mc.setScreen(null);
        mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 4f);
    }

    protected void playSound() {
        mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    protected void onClicked() {
        RSM.getComponent(Terminals.class).simulateClick();
    }

    protected void sendOpenEvent() {
        RSM.getComponent(Terminals.class).onTermSimOpen(getType(), name);
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
            RSM.getComponent(Terminals.class).onTermSimClose(true);
        }
        TermSimScreen screen = switch (type) {
            case ORDER -> new OrderSim();
            default -> null;
        };
        if (screen == null) {
            ChatUtils.chat("Failed to create termsim for %s?", type);
            return;
        }

        ClientboundOpenScreenPacket fakePacket = new ClientboundOpenScreenPacket(0, Utils.getMenuTypeByCount(type.getSize()), Component.literal(screen.name));
        RSM.getComponent(Terminals.class).openTermSim(fakePacket, type);
        mc.setScreen(screen);
    }
}
