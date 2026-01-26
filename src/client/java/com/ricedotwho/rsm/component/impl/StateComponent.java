package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StateComponent extends Component {

    public static StateComponent INSTANCE;

    public List<Packet<?>> tickPackets = new ArrayList<>();

    public boolean attacking = false;
    public boolean interacted = false;
    public boolean interactedAt = false;
    public boolean swinging = false;
    public boolean digging = false;
    public boolean placing = false;
    public boolean swapping = false;
    public boolean clicking = false;

    public boolean sprinting = false;
    public boolean sneaking = false;
    public boolean blocked = false;
    public boolean inInventory = false;
    public int slot = 0;

    public short lastAction = 0;
    public short nextAction = 1;

    public int serverTicks = 0;

    public StateComponent() {
        super("StateComponent");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketEvent(PacketEvent event) {
        this.tickPackets.add(event.packet);

        if (event.packet instanceof C0APacketAnimation)
            this.swinging = true;

        if (event.packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.packet;
            switch (packet.getAction()) {
                case ATTACK:
                    this.attacking = true;
                    break;
                case INTERACT:
                    this.interacted = true;
                    break;
                case INTERACT_AT:
                    this.interactedAt = true;
                    break;
            }
        }

        if (event.packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.packet;
            this.placing = true;

            ItemStack itemStack = packet.getStack();
            if (itemStack == null) return;

            Item item = itemStack.getItem();
            if (!(item instanceof ItemSword)) return;

            if (packet.getPlacedBlockDirection() == 255)
                this.blocked = true;
        }

        if (event.packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;

            if (packet.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
                this.blocked = false;

            this.digging = true;
        }

        if (event.packet instanceof C09PacketHeldItemChange) {
            C09PacketHeldItemChange packet = (C09PacketHeldItemChange) event.packet;
            this.slot = packet.getSlotId();
            this.swapping = true;
            this.blocked = false;
        }

        if (event.packet instanceof C0EPacketClickWindow) {
            C0EPacketClickWindow packet = (C0EPacketClickWindow) event.packet;

            this.lastAction = packet.getActionNumber();
            this.nextAction = (short) (this.lastAction + 1);
            if (this.nextAction == 32767) this.nextAction = -32768;

            this.clicking = true;
        }

        if (event.packet instanceof C03PacketPlayer) {
            this.tickPackets.clear();
            this.attacking = false;
            this.interacted = false;
            this.interactedAt = false;
            this.swinging = false;
            this.digging = false;
            this.placing = false;
            this.swapping = false;
            this.clicking = false;
        }

        if (event.packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction packet = (C0BPacketEntityAction) event.packet;
            switch (packet.getAction()) {
                case START_SPRINTING:
                    this.sprinting = true;
                    break;
                case STOP_SPRINTING:
                    this.sprinting = false;
                    break;
                case START_SNEAKING:
                    this.sneaking = true;
                    break;
                case STOP_SNEAKING:
                    this.sneaking = false;
                    break;
            }
        }

        if (event.packet instanceof C16PacketClientStatus) {
            C16PacketClientStatus packet = (C16PacketClientStatus) event.packet;
            if (packet.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)
                inInventory = true;
        }

        if (event.packet instanceof C0DPacketCloseWindow) {
            inInventory = false;
        }

        if (event.packet instanceof S2DPacketOpenWindow) {
            inInventory = false;
        }

        if (event.packet instanceof S2EPacketCloseWindow) {
            inInventory = false;
        }

        if (event.packet instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange) event.packet;
            this.slot = packet.getHeldItemHotbarIndex();
        }

        if (event.packet instanceof S32PacketConfirmTransaction) {
            S32PacketConfirmTransaction packet = (S32PacketConfirmTransaction) event.packet;
            if (!packet.func_148888_e() && packet.getActionNumber() < 0)
                this.serverTicks++;
        }
    }

    @SubscribeEvent
    public void onWorldLoadEvent(WorldEvent.Unload event) {
        this.attacking = false;
        this.digging = false;
        this.placing = false;
        this.swapping = false;
        this.swinging = false;
        this.sprinting = false;
        this.sneaking = false;
        this.blocked = false;
        this.serverTicks = 0;
        this.lastAction = 0;
        this.nextAction = 1;
    }
}