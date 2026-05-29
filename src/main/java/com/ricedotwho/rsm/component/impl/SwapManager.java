package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.IMixin.IConnection;
import com.ricedotwho.rsm.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.function.Predicate;

public class SwapManager extends ModComponent {
    @Getter
    private static int serverSlot;

    private static int lastSentServerSlot;
    private static boolean swappedThisTick = false;

    private static RequiredSwap requireSwap = new RequiredSwap(-1, false);

    //just for silent swapping
    public static int clientSlot = -1;

    public static boolean inGui = false;

    public SwapManager() {
        super("SwapManager");
    }

    private record RequiredSwap(int slot, boolean silent) {}

    public static void onPreTickStart() {
        swappedThisTick = false;
        requireSwap = new RequiredSwap(-1, false);
    }

    public static boolean onPostSendPacket(Packet<?> packet) {
        if (!(packet instanceof ServerboundSetCarriedItemPacket slotPacket)) return true;

        if (swappedThisTick || slotPacket.getSlot() == lastSentServerSlot) {
            ChatUtils.chat("Prevented packet 0 tick swap! This shouldn't happen, " +
                    "this CAN ban, you are probably using a conflicting mod");
            return false;
        }

        swappedThisTick = true;
        serverSlot = slotPacket.getSlot();
        lastSentServerSlot = slotPacket.getSlot();
        return true;
    }

    public static void onHandleLogin() {
        // The Minecraft.MultiPlayerGameMode is reset here, so its server slot is also reset
        serverSlot = 0;
        lastSentServerSlot = 0; // Scary but should be fine
    }

    // Cancels call if returns false
    public static boolean onEnsureHasSentCarriedItem(int managerServerSlot) {
        if (Minecraft.getInstance().player == null) return false;
        if (serverSlot != managerServerSlot) {
            ChatUtils.chat("Slot mismatch! This can ban and probably means you are using a conflicting mod!");
            ChatUtils.chat("SwapManger : " + serverSlot);
            ChatUtils.chat("GameMode : " + managerServerSlot);
        }
        int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (!swappedThisTick && requireSwap.slot > -1 && i != requireSwap.slot) {
            if (requireSwap.slot == managerServerSlot) return false;
            Minecraft.getInstance().player.getInventory().setSelectedSlot(requireSwap.slot);
            i = requireSwap.slot;
        }

        if (i != managerServerSlot && !swappedThisTick) {
            serverSlot = i;
            return true;
        }
        return false;
    }

    private static boolean reserveSwap0(int index, boolean silent) {
        if (index < 0 || index > 8) return false;

        if (!canSwap()) {
            // Should already be reserved or we already swapped so we can't swap off anyways
            return index == getNextUpdateIndex(); // Already on this item
        }
        requireSwap = new RequiredSwap(index, silent);
        return true;
    }


    public static boolean reserveSwap(int index, boolean silent) {
        if (!reserveSwap0(index, silent)) return false;
        swapSlot(index);
        return true;
    }

    public static boolean reserveSwap(int index) {
        return reserveSwap(index, false);
    }

    public static int getNextUpdateIndex() {
        if (swappedThisTick) return serverSlot;
        if (requireSwap.slot > -1) return requireSwap.slot;
        if (Minecraft.getInstance().player == null) return 0;
        return Minecraft.getInstance().player.getInventory().getSelectedSlot();
    }

    public static boolean canSwap() {
        return !swappedThisTick && requireSwap.slot < 0;
    }

    public static boolean sendAirC08(float yaw, float pitch, boolean syncSlots, boolean swing) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return false;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return false;

        IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);

        int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (syncSlots) manager.syncSlot();
        if (syncSlots && !checkServerSlot(i)) {
            ChatUtils.chat("Failed to swap to slot : " + i);
            return false;
        }

        manager.sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
        if (swing) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    public static boolean sendAirC08RightFuckingNow(float yaw, float pitch, boolean syncSlots, boolean swing) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return false;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null || Minecraft.getInstance().getConnection() == null) return false;

        IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);

        int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (syncSlots) manager.syncSlot();
        if (syncSlots && !checkServerSlot(i)) {
            ChatUtils.chat("Failed to swap to slot : " + i);
            return false;
        }

        IConnection connection = (IConnection) Minecraft.getInstance().getConnection().getConnection();

        connection.sendPacketImmediately(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, pitch));
        //manager.sendPacketSequenced(Minecraft.getInstance().level, sequence -> );
        if (swing) connection.sendPacketImmediately(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        //if (swing) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    public static boolean isDesynced() {
        return getNextUpdateIndex() != serverSlot;
    }

    public static boolean sendAirC08(float yaw, float pitch, boolean syncSlots) {
        return sendAirC08(yaw, pitch, syncSlots, false);
    }

    public static boolean sendAirC08(Rotation rot, boolean syncSlots) {
        return sendAirC08(rot.getYaw(), rot.getPitch(), syncSlots, false);
    }

    public static boolean sendAirC08(Rotation rot, boolean syncSlots, boolean swing) {
        return sendAirC08(rot.getYaw(), rot.getPitch(), syncSlots, swing);
    }

    public static boolean sendBlockC08(BlockHitResult result, boolean swing, boolean syncSlot) {
        return sendBlockC08(result, swing, syncSlot, InteractionHand.MAIN_HAND);
    }

    public static boolean sendBlockC08(BlockHitResult result, boolean swing, boolean syncSlot, InteractionHand hand) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return false;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return false;

        if (syncSlot) {
            IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);
            int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
            manager.syncSlot();
            if (!checkServerSlot(i)) {
                ChatUtils.chat("Failed to swap to slot : " + i);
                return false;
            }
        }

        ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode).sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundUseItemOnPacket(hand, result, sequence));
        if (swing) Minecraft.getInstance().player.swing(hand);
        return true;
    }

    public static boolean sendBlockC08(float yaw, float pitch, boolean swing, boolean syncSlot) {
        HitResult result = RotationUtils.getBlockHitResult(Minecraft.getInstance().player.getContainerInteractionRange(), yaw, pitch, Minecraft.getInstance().player.position().add(0d, EtherUtils.SNEAK_EYE_HEIGHT, 0d));
        if (result.getType() != HitResult.Type.BLOCK) {
            ChatUtils.chat("Failed to send block C08!");
        }
        return sendBlockC08((BlockHitResult) result, swing, syncSlot);
    }

    // Haven't implement syncSlots because I haven't found the need
    public static boolean sendBlockC08(Vec3 pos, Direction direction, boolean swing, boolean syncSlot) {
        return sendBlockC08(new BlockHitResult(pos, direction, BlockPos.containing(pos), false), swing, syncSlot);
    }

    ///  The only Actions passed should be START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, and STOP_DESTROY_BLOCK
    public static boolean sendC07(BlockPos result, ServerboundPlayerActionPacket.Action action, Direction face, boolean swing, boolean syncSlot) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return false;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return false;

        if (syncSlot) {
            IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);
            int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
            manager.syncSlot();
            if (!checkServerSlot(i)) {
                ChatUtils.chat("Failed to swap to slot : " + i);
                return false;
            }
        }

        if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            Minecraft.getInstance().getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, result, Direction.DOWN, 0));
        } else {
            ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode).sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundPlayerActionPacket(action, result, face, sequence));
        }
        if (swing) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    public static boolean reserveSwap(Item item) {
        return reserveSwap(item, false);
    }

    public static boolean reserveSwap(Item item, boolean silent) {

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return false;
        if (!canSwap()) {
            // Should already be reserved or we already swapped so we can't swap off anyways
            return item == player.getInventory().getItem(getNextUpdateIndex()).getItem(); // Already on this item
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            boolean bl = swapSlot(i, silent);
            if (bl) reserveSwap0(i, silent);
            return bl;
        }
        return false;
    }

    public static boolean reserveSwap(Predicate<ItemStack> predicate) {
        return reserveSwap(predicate, false);
    }

    public static boolean reserveSwap(Predicate<ItemStack> predicate, boolean silent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        if (!canSwap()) {
            // Should already be reserved or we already swapped so we can't swap off anyways
            return predicate.test((player.getInventory().getItem(getNextUpdateIndex()))); // Already on this item
        }

        for (int i = 0; i < 9; i++) {
            if (!predicate.test(player.getInventory().getItem(i))) continue;
            boolean bl = swapSlot(i, silent);
            if (bl) reserveSwap0(i, silent);
            return bl;
        }
        return false;
    }

    public static boolean reserveSwap(String ...sbId) {
        return reserveSwap(false, sbId);
    }

    public static boolean reserveSwap(boolean silent, String ...sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sbId == null || sbId.length == 0) return false;
        if (!canSwap()) {
            // Should already be reserved or we already swapped so we can't swap off anyways
            String next = stripStarred(ItemUtils.getID(player.getInventory().getItem(getNextUpdateIndex())));
            return Arrays.stream(sbId).anyMatch(id -> !id.isBlank() && next.equals(stripStarred(id))); // Already on this item
        }

        for (int i = 0; i < 9; i++) {
            String id = stripStarred(ItemUtils.getID(player.getInventory().getItem(i)));
            if (Arrays.stream(sbId).noneMatch(id1 -> !id1.isBlank() && id.equals(stripStarred(id1)))) continue;
            boolean bl = swapSlot(i, silent);
            if (bl) reserveSwap0(i, silent);
            return bl;
        }
        return false;
    }

    public static boolean swapItem(Item item) {
        return swapItem(item, false);
    }

    public static boolean swapItem(Item item, boolean silent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return false;

        if (item == player.getInventory().getItem(getNextUpdateIndex()).getItem()) return true; // Already on this item
        if (!canSwap()) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            return swapSlot(i, silent);
        }
        return false;
    }

    public static boolean swapItem(String ...sbId) {
        return swapItem(false, sbId);
    }

    /// Swap to an item with the specified SkyBlock ID
    public static boolean swapItem(boolean silent, String ...sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sbId == null || sbId.length == 0) return false;

        String heldId = stripStarred(ItemUtils.getID(player.getInventory().getItem(getNextUpdateIndex())));
        if (Arrays.stream(sbId).anyMatch(id -> !id.isBlank() && heldId.equals(stripStarred(id)))) return true;

        if (!canSwap()) return false;
        for (int i = 0; i < 9; i++) {
            String id = stripStarred(ItemUtils.getID(player.getInventory().getItem(i)));
            if (Arrays.stream(sbId).noneMatch(id1 -> !id1.isBlank() && id.equals(id1))) continue;
            return swapSlot(i, silent);
        }
        return false;
    }

    public static String stripStarred(String sbId) {
        if (sbId.startsWith("STARRED_")) {
            return sbId.substring(8);
        }
        return sbId;
    }

    public static boolean swapItem(Predicate<ItemStack> predicate) {
        return swapItem(predicate, false);
    }

    public static boolean swapItem(Predicate<ItemStack> predicate, boolean silent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        if (predicate.test(player.getInventory().getItem(getNextUpdateIndex()))) return true;

        if (!canSwap()) return false;
        for (int i = 0; i < 9; i++) {
            if (!predicate.test(player.getInventory().getItem(i))) continue;
            return swapSlot(i, silent);
        }
        return false;
    }

    public static boolean swapSlot(int slot) {
        return swapSlot(slot, false);
    }

    public static boolean swapSlot(int slot, boolean silent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (slot == getNextUpdateIndex()) return true;
        if (player == null || swappedThisTick) return false;
        if (slot < 0 || slot > 8) {
            RSM.getLogger().error("Invalid swap slot! : {}", slot);
            return false;
        }

        if (silent && clientSlot == -1) clientSlot = player.getInventory().getSelectedSlot();

        if (slot == clientSlot) clientSlot = -1;

        player.getInventory().setSelectedSlot(slot);
        return true;
    }

    public static boolean checkServerSlot(int slot) {
        return serverSlot == slot;
    }

    public static boolean checkServerItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || serverSlot < 0 || serverSlot > 8) return false;

        ItemStack stack = player.getInventory().getItem(serverSlot);
        return stack.getItem() == item;
    }

    public static boolean checkServerItem(String ...sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || serverSlot < 0 || serverSlot > 8 || sbId.length == 0) return false;

        String heldId = ItemUtils.getID(player.getInventory().getItem(serverSlot));
        return Arrays.stream(sbId).anyMatch(id -> !id.isBlank() && heldId.equals(id));
    }

    public static boolean checkClientItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        ItemStack stack = player.getInventory().getItem(player.getInventory().getSelectedSlot());
        return stack.getItem() == item;
    }

    public static boolean checkClientItem(String ...sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sbId.length == 0) return false;

        String heldId = ItemUtils.getID(player.getInventory().getItem(player.getInventory().getSelectedSlot()));
        return Arrays.stream(sbId).anyMatch(id -> !id.isBlank() && heldId.equals(id));
    }

    public static int getItemSlot(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int getItemInvSlot(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int getItemSlot(String ...id) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || id == null || id.length == 0) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (Arrays.stream(id).anyMatch(s -> s.equals(ItemUtils.getID(stack)))) return i;
        }
        return -1;
    }

    public static ItemStack getClientHeldItem(ItemStack original) {
        return clientSlot != -1 ? mc.player.getInventory().getItem(clientSlot) : original;
    }

    public static void swapClientSlot(int newItemSlot, Inventory inventory) {
        if (clientSlot != -1) {
            if (newItemSlot == mc.player.getInventory().getSelectedSlot()) clientSlot = -1;
            else clientSlot = newItemSlot;
        }
        else inventory.setSelectedSlot(newItemSlot);
    }

    public static boolean stopSilentSwap() {
        if (clientSlot == -1) return true;

        boolean success = swapSlot(clientSlot);
        clientSlot = -1;
        return success;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        clientSlot = -1;
        inGui = false;
    }

    @SubscribeEvent
    public void onMouse(MouseInputEvent.Click event) {
        if (clientSlot != -1 && event.isDown()) stopSilentSwap();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCancelled = true) //listen i NEEEED this packet (ik i dont need highest prio technically)
    public void onGuiServer(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket) inGui = true;
        else if (event.getPacket() instanceof ClientboundContainerClosePacket) inGui = false;
    }

    @SubscribeEvent
    public void onGuiClient(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket) inGui = false;
        else if (event.getPacket() instanceof ServerboundContainerClickPacket) inGui = true;
    }

    public static boolean getFromInv(Item item) {
        if (inGui || mc.screen != null) return false;
        int slot = getItemInvSlot(item);
        if (slot == -1) return false;

        if (slot < 9) slot += 36;

        InventoryScreen inv = new InventoryScreen(mc.player);
        mc.setScreen(inv);

        com.ricedotwho.rsm.utils.GuiUtils.clickSlot(inv.getMenu().getSlot(slot), slot, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP);
        inv.onClose();
        return true;
    }

    // TODO
    // Hook these functions at a lower level
}
