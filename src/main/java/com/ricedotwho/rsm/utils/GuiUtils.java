package com.ricedotwho.rsm.utils;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.mixins.accessor.AbstractContainerScreenAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.ricedotwho.rsm.utils.Accessor.mc;

@UtilityClass
public class GuiUtils {
    public void sendWindowClick(int slotNumber, Player player, AbstractContainerMenu abstractContainerMenu) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        NonNullList<Slot> nonNullList = abstractContainerMenu.slots;
        int l = nonNullList.size();
        List<ItemStack> list = Lists.newArrayListWithCapacity(l);

        for (Slot slot : nonNullList) {
            list.add(slot.getItem().copy());
        }

        abstractContainerMenu.clicked(slotNumber, 0, ClickType.CLONE, player);

        Int2ObjectMap<HashedStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();

        for (int m = 0; m < l; m++) {
            ItemStack itemStack = list.get(m);
            ItemStack itemStack2 = nonNullList.get(m).getItem();
            if (!ItemStack.matches(itemStack, itemStack2)) {
                int2ObjectMap.put(m, HashedStack.create(itemStack2, connection.decoratedHashOpsGenenerator()));
            }
        }

        HashedStack hashedStack = HashedStack.create(abstractContainerMenu.getCarried(), connection.decoratedHashOpsGenenerator());
        connection.send(new ServerboundContainerClickPacket(abstractContainerMenu.containerId, abstractContainerMenu.getStateId(), Shorts.checkedCast(slotNumber), SignedBytes.checkedCast(0), ClickType.CLONE, int2ObjectMap, hashedStack));
    }

    ///Bypasses direct slotClicked function call to have some events not happen
    public void clickSlot(Slot slot, int clickedSlotIndex, int clickedButtonIndex, ClickType clickType) {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) mc.screen;

        if (slot != null) {
            clickedSlotIndex = slot.index;
        }

        ((AbstractContainerScreenAccessor) container).invokeOnMouseClickAction(slot, clickType);
        mc.gameMode.handleInventoryMouseClick(container.getMenu().containerId, clickedSlotIndex, clickedButtonIndex, clickType, mc.player);
    }

    public void grabMouse(int type) {
        if (mc.isWindowActive()) {
            if (!mc.mouseHandler.isMouseGrabbed()) {
                if (InputQuirks.RESTORE_KEY_STATE_AFTER_MOUSE_GRAB) {
                    KeyMapping.setAll();
                }

                mc.mouseHandler.mouseGrabbed = true;
                mc.mouseHandler.xpos = mc.getWindow().getScreenWidth() / 2f;
                mc.mouseHandler.ypos = mc.getWindow().getScreenHeight() / 2f;
                InputConstants.grabOrReleaseMouse(mc.getWindow(), 212995, mc.mouseHandler.xpos, mc.mouseHandler.ypos);
                switch (type) {
                    case 1 -> mc.setScreen(null);
                    case 2 -> mc.screen = null;
                }
                mc.missTime = 10000;
                mc.mouseHandler.ignoreFirstMove = true;
            }
        }
    }

    public double getMouseX() {
         return mc.mouseHandler.getScaledXPos(mc.getWindow());
    }

    public double getMouseY() {
        return mc.mouseHandler.getScaledYPos(mc.getWindow());
    }

    public double scaleX(double x) {
        return MouseHandler.getScaledXPos(mc.getWindow(), x);
    }

    public double scaleY(double y) {
        return MouseHandler.getScaledYPos(mc.getWindow(), y);
    }
}
