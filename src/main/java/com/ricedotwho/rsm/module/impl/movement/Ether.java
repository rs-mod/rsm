package com.ricedotwho.rsm.module.impl.movement;


import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Floor;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.NoRotateManager;
import com.ricedotwho.rsm.managers.SbStatTracker;
import com.ricedotwho.rsm.managers.WorldRenderer;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.managers.camera.CameraPositionProvider;
import com.ricedotwho.rsm.managers.dungeon.map.Map;
import com.ricedotwho.rsm.managers.dungeon.map.handler.Dungeon;
import com.ricedotwho.rsm.managers.dungeon.map.map.Room;
import com.ricedotwho.rsm.managers.dungeon.map.map.RoomType;
import com.ricedotwho.rsm.managers.dungeon.map.utils.ScanUtils;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@ModuleInfo(aliases = "Ether", id = "Ether", category = Category.MOVEMENT)
public class Ether extends Module implements CameraPositionProvider {

    @Getter
    private final static Ether instance = new Ether();
    private final BooleanSetting singleplayerEw = new BooleanSetting("Singleplayer", false);

    private final DefaultGroupSetting helperGroup = new DefaultGroupSetting("Helper", this);
    private final BooleanSetting helper = new BooleanSetting("Enabled", false);
    private final ColorSetting correctColor = new ColorSetting("Correct", Color.fromRGB(0, 255, 0, 0.35f));
    private final ColorSetting correctColorOutline = new ColorSetting("Correct Outline", Color.fromRGB(0, 255, 0));
    private final ColorSetting failColor = new ColorSetting("Fail", Color.fromRGB(255, 0, 0, 0.35f));
    private final ColorSetting failColorOutline = new ColorSetting("Fail Outline", Color.fromRGB(255, 0, 0));
    private final ModeSetting renderMode = new ModeSetting("Render Mode", "Filled Outline", List.of("Outline", "Filled Outline", "Filled"));
    private final BooleanSetting depth = new BooleanSetting("Depth", true);
    private final BooleanSetting serverPos = new BooleanSetting("Server Position", true);
    private final BooleanSetting fullBlock = new BooleanSetting("Full Block", false);
    private final BooleanSetting alwaysShow = new BooleanSetting("Show While Unsneaked", false);

    private final DefaultGroupSetting noRotateGroup = new DefaultGroupSetting("No Rotate", this);
    private final BooleanSetting noRotate = new BooleanSetting("Enabled", false);
    private final BooleanSetting teleportItem = new BooleanSetting("Teleport Items", true);
    private final BooleanSetting outbounds = new BooleanSetting("Outbounds", false);
    private final BooleanSetting alwaysNoRotate = new BooleanSetting("Always No Rotate", false);
    private final BooleanSetting noRotateFromPackets = new BooleanSetting("From Packets", false);
    @Getter private final NumberSetting<Integer> timeout = new NumberSetting<>("Timeout", 2, 40, 20, 1);

    private final DefaultGroupSetting zpewGroup = new DefaultGroupSetting("Zpew", this);
    private final BooleanSetting zpew = new BooleanSetting("Etherwarp", false);
    private final BooleanSetting zptp = new BooleanSetting("(WIP) Teleport", false);
    private final BooleanSetting zpInteract = new BooleanSetting("Zero Ping Interact", false);
    private final BooleanSetting assumeCancelInteract = new BooleanSetting("Assume Cancel Interact", false);
    @Getter
    private final SaveSetting<Set<String>> ignoredRooms = new SaveSetting<>("Ignored rooms", "dungeon/zpew", "default.json", HashSet::new, new TypeToken<@NotNull Set<String>>() {}.getType(), true);

    private final BooleanSetting etherwarpSound = new BooleanSetting("Etherwarp Sound", false);
    private final StringSetting etherwarpSoundId = new StringSetting("Sound", "block.note_block.pling", false, false, etherwarpSound::getValue);
    private final NumberSetting<Float> etherwarpSoundVolume = new NumberSetting<>("Volume", 0f, 10f, 1f, 0.1f, etherwarpSound::getValue);
    private final NumberSetting<Float> etherwarpSoundPitch = new NumberSetting<>("Pitch", 0f, 2f, 1f, 0.1f, etherwarpSound::getValue);
    private int soundQueue = 0;

    private Vec3 renderVec3;

    private final List<Long> noRotateSent = new ArrayList<>();
    private final List<Vec3> zpewSent = new ArrayList<>();
    private long lastWIMP = 0;
    private static final long WITHER_IMPACT_COOLDOWN_MS = 125L;

    private static final List<Class<?>> ignored = List.of(
            HopperBlock.class,
            AnvilBlock.class,
            ChestBlock.class,
            EnderChestBlock.class,
            TrappedChestBlock.class,
            DropperBlock.class,
            DispenserBlock.class,
            LeverBlock.class,
            ButtonBlock.class,
            CauldronBlock.class
    );

    private static final List<Class<?>> ignoredForCI = List.of(
            AnvilBlock.class,
            ChestBlock.class,
            EnderChestBlock.class,
            TrappedChestBlock.class,
            DropperBlock.class,
            DispenserBlock.class,
            LeverBlock.class,
            ButtonBlock.class,
            CauldronBlock.class
    );

    public Ether() {
        this.getGeneralGroup().add(singleplayerEw);

        helperGroup.add(
                helper,
                correctColor,
                correctColorOutline,
                failColor,
                failColorOutline,
                renderMode,
                depth,
                serverPos,
                fullBlock,
                alwaysShow
        );

        noRotateGroup.add(
                noRotate,
                teleportItem,
                noRotateFromPackets,
                outbounds,
                alwaysNoRotate,
                timeout
        );

        zpewGroup.add(
                zpew,
                zptp,
                zpInteract,
                assumeCancelInteract,
                ignoredRooms
        );
    }

    // singleplayer etherwarp
    public boolean onReceive(Packet<?> packet, ServerGamePacketListenerImpl packetListener) {
        if (!this.isEnabled() || !singleplayerEw.getValue() || !(packet instanceof ServerboundUseItemPacket useItemPacket)) return false;
        ServerPlayer player = packetListener.getPlayer();
        if (player.getInventory().getSelectedItem().getItem() != Items.DIAMOND_SHOVEL) return false;

        Vec3 vec3;
        if (player.isShiftKeyDown()) {
            BlockPos temp = EtherUtils.getEtherPosFromOrigin(player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d), useItemPacket.getYRot(), useItemPacket.getXRot(), 61).getFirst();
            vec3 = temp == null ? null : new Vec3(temp.getX() + 0.5d, temp.getY() + 1d, temp.getZ() + 0.5d);
        } else {
            vec3 = EtherUtils.predictTeleport(61, player.position(), useItemPacket.getYRot(),  useItemPacket.getXRot());
        }

        if (vec3 == null) {
            return false;
        }

        packetListener.teleport(vec3.x(), vec3.y(), vec3.z(), useItemPacket.getYRot(), useItemPacket.getXRot());
        playEtherwarpSound();
        return true;
    }

    @SubscribeEvent(receiveCancelled = true)
    public void onReceiveSound(PacketEvent.MainReceivePre event, ClientboundSoundPacket packet) {
        if (!this.isEnabled() || !this.etherwarpSound.getValue() || packet.getSound().value() != SoundEvents.ENDER_DRAGON_HURT) return;
        if (!zpew.getValue()) {
            playEtherwarpSound();
        }
        if (soundQueue <= 0) return;
        soundQueue--;
        event.setCancelled(true);
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (mc.screen != null || !helper.getValue() || mc.player == null || (!mc.player.getLastSentInput().shift() && !alwaysShow.getValue())) return;
        ItemStack held = mc.player.getMainHandItem();
        if (!ItemUtils.isEtherwarp(held)) return;

        net.minecraft.world.phys.Vec3 vec3 = (renderVec3 == null ? (serverPos.getValue() ? mc.player.oldPosition() : mc.player.position()) : renderVec3).add(0, EtherUtils.getSneakHeight(), 0);
        Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(vec3, 57 + ItemUtils.getTunerDistance(held));
        if (ether.getFirst() == null) return;

        boolean canInteract = true;
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            assert mc.level != null;
            canInteract = !isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock());
        }

        boolean canTp = ether.getSecond() &&  canInteract && isRoomAllowed() && isRoomAllowing(ScanUtils.getRoomFromPos(ether.getFirst().getX(), ether.getFirst().getZ()));

        Color color = canTp ? this.correctColor.getValue() : this.failColor.getValue();
        Color outline = canTp ? this.correctColorOutline.getValue() : this.failColorOutline.getValue();

         VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : Utils.getBlockShape(ether.getFirst()));
         AABB aabb = shape.bounds().move(ether.getFirst());
        switch (this.renderMode.getValue()) {
            case "Outline" -> WorldRenderer.outlineBox(aabb, outline, this.depth.getValue());
            case "Filled Outline" -> WorldRenderer.filledOutlineBox(aabb, color, outline, this.depth.getValue());
            default -> WorldRenderer.filledBox(aabb, color, this.depth.getValue());
        }
    }

    private boolean isRoomAllowed() {
        return Map.getCurrentRoom() == null || !Utils.equalsOneOf(Map.getCurrentRoom().getData().name(), "Boulder", "Teleport Maze") && Map.getCurrentRoom().getData().type() != RoomType.TRAP;
    }

    private boolean isRoomAllowing(Room room) {
        return room == null || !Utils.equalsOneOf(room.getData().name(), "Teleport Maze", "Boulder");
    }

    private boolean isRoomAllowedZPEW() {
        String room = Map.getCurrentRoom() == null ? null : Map.getCurrentRoom().getData().name();
        return room == null || !Utils.equalsOneOf(room, "Boulder", "Teleport Maze") && Map.getCurrentRoom().getData().type() != RoomType.TRAP || ignoredRooms.getValue().contains(room);
    }

    @SubscribeEvent
    private void onPlayerUse(PlayerInputEvent.Use event) {
        if (!this.noRotate.getValue() || !this.teleportItem.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7)) || !isRoomAllowed() || event.getHand() != InteractionHand.MAIN_HAND) return;
        assert mc.player != null;
        ItemStack stack = mc.player.getInventory().getSelectedItem();
        if (!isTpItem(stack)) return;

        if (event.getResult() instanceof BlockHitResult blockHitResult) {
            assert mc.level != null;
            if (isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) return;
        }


        if (!noRotateFromPackets.getValue()) noRotateSent.add(EventDispatcher.getTotalWorldTime());
        if (zpew.getValue() || zptp.getValue())
            checkZpew(stack, event.getYRot(), event.getXRot());
    }

    @SubscribeEvent
    private void onUseItem(PacketEvent.Send event) {
        if (!this.noRotate.getValue() || !this.teleportItem.getValue() || !noRotateFromPackets.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7)) || !isRoomAllowed()) return;
        if (event.getPacket() instanceof ServerboundUseItemPacket packet) {
            assert mc.player != null;
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            if (!isTpItem(stack)) return;
            noRotateSent.add(EventDispatcher.getTotalWorldTime());
            return;
        }

        if (event.getPacket() instanceof ServerboundUseItemOnPacket packet) {
            assert mc.player != null;
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            assert mc.level != null;
            Block block =  mc.level.getBlockState(packet.getHitResult().getBlockPos()).getBlock();
            if (!isIgnored(block) && isTpItem(stack)) {
                noRotateSent.add(EventDispatcher.getTotalWorldTime());
            }
        }
    }

    private void checkZpew(ItemStack stack, float yaw, float pitch) {
        if (mc.level == null || mc.player == null
                || !isTpItem(stack)
                || SbStatTracker.getStats().getMana().getCurrent() < 180
                || !isRoomAllowedZPEW()
        ) return;

        // tspmo
        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            if (isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) return;
        }

        boolean sneaking = mc.player.getLastSentInput().shift();
        Vec3 currentVec3 = renderVec3 == null ? mc.player.position() : renderVec3;
        net.minecraft.world.phys.Vec3 eyeVec3 = currentVec3.add(0.0d, EtherUtils.getEyeHeight(), 0.0d);
        if (sneaking && ItemUtils.isEtherwarp(stack) && zpew.getValue()) {

            Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(eyeVec3, yaw, pitch, 57 + ItemUtils.getTunerDistance(stack));
            if (ether.getFirst() == null || !ether.getSecond()) return;

            renderVec3 = ether.getFirst().toVec3().add(0.5d, 1.05d, 0.5d);
            playEtherwarpSound();
            CameraHandler.registerProvider(this);
            zpewSent.add(renderVec3);
        } else if (!sneaking && zptp.getValue()) {
            long now = System.currentTimeMillis();
            boolean wimp = isWitherImpactItem(stack);
            if (wimp && now - lastWIMP < WITHER_IMPACT_COOLDOWN_MS) {
                return;
            }

            float distance = getTpDistance(stack);
            if (distance == 0) return;
            Vec3 prediction = EtherUtils.predictTeleport((int) distance, currentVec3, yaw,  pitch);
//            Pos prediction = EtherUtils.predictTeleport(eyePos, yaw,  pitch, distance);
            if (prediction == null) return;

            Vec3 target = prediction.subtract(0.0d, 1.0d, 0.0d);
            target = resolveZptpTarget(target);
            if (target == null) return;
            if (isSameTeleportDestination(target, currentVec3)) {
                return;
            }
            renderVec3 = target;
            CameraHandler.registerProvider(this);
            zpewSent.add(renderVec3);

            if (wimp) {
                lastWIMP = now;
            }
        }
    }

    private Vec3 resolveZptpTarget(Vec3 target) {
        if (isSafeZptpTarget(target)) return target;

        Vec3 above = target.above();
        return isSafeZptpTarget(above) ? above : null;
    }

    private boolean isSafeZptpTarget(Vec3 target) {
        if (mc.level == null) return false;

        BlockPos feet = target.toBlockPos();
        if (!mc.level.hasChunk(feet.getX() >> 4, feet.getZ() >> 4)) return false;

        BlockPos head = feet.above();
        return mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()
                && mc.level.getBlockState(head).getCollisionShape(mc.level, head).isEmpty();
    }

    private boolean isWitherImpactItem(ItemStack item) {
        String itemId = ItemUtils.getID(item);
        if (!Utils.equalsOneOf(itemId, "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA")) {
            return false;
        }

        return ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
    }

    private boolean isSameTeleportDestination(Vec3 target, Vec3 currentVec3) {
        return target.toBlockPos().equals(currentVec3.toBlockPos());
    }

    private void playEtherwarpSound() {
        if (!etherwarpSound.getValue() || mc.level == null) return;
        Identifier sound = Identifier.tryParse(this.etherwarpSoundId.getValue());
        if (sound == null) return;
        soundQueue++;
        PlayerUtils.playSound(SoundEvent.createVariableRangeEvent(sound), etherwarpSoundPitch.getValue(), etherwarpSoundVolume.getValue());
    }

    // timeout stuff
    @SubscribeEvent
    private void onTick(TickEvent.Server event) {
        long now = event.getTime();
        noRotateSent.removeIf(t -> now - t >= timeout.getValue().longValue());
        if (noRotateSent.isEmpty() && renderVec3 != null) {
            renderVec3 = null;
        }
    }


    @SubscribeEvent
    private void onEnterBoss(DungeonEvent.EnterBoss event) {
        reset();
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    private boolean shouldNoRotate() {
        long now = EventDispatcher.getTotalWorldTime();
        noRotateSent.removeIf(t -> now - t >= timeout.getValue().longValue());

        if (this.alwaysNoRotate.getValue()) return true;
        if (!noRotateSent.isEmpty() && this.teleportItem.getValue()) {
            noRotateSent.removeFirst();
            return true;
        }

        return this.outbounds.getValue() && !Dungeon.isStarted() && Location.getArea().is(Island.Dungeon);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onTP(PacketEvent.MainReceivePre event, ClientboundPlayerPositionPacket packet) {
        if (!this.noRotate.getValue() || !this.isEnabled()) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        PositionMoveRotation startPos = PositionMoveRotation.of(player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());

        if (this.zpew.getValue() || this.zptp.getValue()) handleZpew(newPos);

        if (!shouldNoRotate()) return;
        if (!noRotateSent.isEmpty()) noRotateSent.removeFirst();
        //NoRotateManager.noRotateNext();
        NoRotateManager.addPacket(packet);
    }

    private void handleZpew(PositionMoveRotation newPos) {
        if (zpewSent.isEmpty()) {
            this.renderVec3 = null;
        } else {
            Vec3 old = zpewSent.removeFirst();
            boolean correct = old.x() == newPos.position().x()
                    && old.y() == newPos.position().y()
                    && old.z() == newPos.position().z();
            if (!correct || zpewSent.isEmpty()) {
                this.zpewSent.clear();
                this.renderVec3 = null;
            }
        }
    }

    @Override
    public void reset() {
        this.noRotateSent.clear();
        this.zpewSent.clear();
        this.renderVec3 = null;
        this.lastWIMP = 0;
        this.soundQueue = 0;
    }

    public static boolean isTpItem(ItemStack item) {
        String sbId = ItemUtils.getID(item);
        if (Utils.equalsOneOf(sbId, "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID", "ETHERWARP_CONDUIT", "ASPECT_OF_THE_LEECH_1", "ASPECT_OF_THE_LEECH_2", "ASPECT_OF_THE_LEECH_3")) return true;
        return Utils.equalsOneOf(sbId, "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA") && ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
    }

    public static int getTpDistance(ItemStack item) {
        return switch (ItemUtils.getID(item)) {
            case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> 8 + ItemUtils.getTunerDistance(item);
            case "ASPECT_OF_THE_LEECH_1" -> 3;
            case "ASPECT_OF_THE_LEECH_2" -> 4;
            case "ASPECT_OF_THE_LEECH_3" -> 5;
            case "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA" -> ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3 ? 10 : 0;
            case null, default -> 0;
        };
    }

    public static boolean isIgnored(Block block) {
        return (instance.assumeCancelInteract.getValue() ? ignoredForCI : ignored).stream().anyMatch(c -> c.isInstance(block));
    }

    @Override
    public boolean shouldOverridePosition() {
        return this.isEnabled() && this.renderVec3 != null && (zpew.getValue() || zptp.getValue());
    }

    @Override
    public boolean shouldOverrideHitPos() {
        return this.isEnabled()
                && this.renderVec3 != null && (zpew.getValue() || zptp.getValue())
                && this.zpInteract.getValue()
                && !shouldBlockZeroPingInteract();
    }

    private boolean shouldBlockZeroPingInteract() {
        if (mc.player == null || mc.level == null) return false;

        ItemStack held = mc.player.getMainHandItem();
        if (!isCaseFromTpRange(held)) return false;

        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            return !mc.level.getBlockState(blockHitResult.getBlockPos()).isAir();
        }

        return false;
    }

    private boolean isCaseFromTpRange(ItemStack item) {
        return switch (ItemUtils.getID(item)) {
            case "ASPECT_OF_THE_END",
                 "ASPECT_OF_THE_VOID",
                 "ASPECT_OF_THE_LEECH_1",
                 "ASPECT_OF_THE_LEECH_2",
                 "ASPECT_OF_THE_LEECH_3",
                 "NECRON_BLADE",
                 "SCYLLA",
                 "HYPERION",
                 "VALKYRIE",
                 "ASTRAEA" -> true;
            case null, default -> false;
        };
    }

    @Override
    public boolean shouldOverrideHitRot() {
        return false;
    }

    @Override
    public boolean shouldBlockKeyboardMovement() {
        return false;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getCameraPosition() {
        if (mc.player == null) return null;
        return this.renderVec3;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getPosForHit() {
        return this.getCameraPosition();
    }

    @Override
    public net.minecraft.world.phys.Vec3 getRotForHit() {
        return net.minecraft.world.phys.Vec3.ZERO;
    }
}
