package com.ricedotwho.rsm.module.impl.movement;


import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.component.impl.NoRotateManager;
import com.ricedotwho.rsm.component.impl.PacketOrderManager;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.SbStatTracker;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.camera.CameraPositionProvider;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.TPMaze;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Ether", id = "Ether", category = Category.MOVEMENT)
public class Ether extends Module implements CameraPositionProvider {

    private final BooleanSetting singleplayerEw = new BooleanSetting("Singleplayer", false);

    private final DefaultGroupSetting helperGroup = new DefaultGroupSetting("Helper", this);
    private final BooleanSetting helper = new BooleanSetting("Enabled", false);
    private final ColourSetting correctColour = new ColourSetting("Correct", new Colour(0, 255, 0, 90));
    private final ColourSetting correctColourOutline = new ColourSetting("Correct Outline", new Colour(0, 255, 0));
    private final ColourSetting failColour = new ColourSetting("Fail", new Colour(255, 0, 0, 90));
    private final ColourSetting failColourOutline = new ColourSetting("Fail Outline", new Colour(255, 0, 0));
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
    @Getter private static final NumberSetting timeout = new NumberSetting("Timeout", 250, 2000, 1000, 25);

    private final DefaultGroupSetting zpewGroup = new DefaultGroupSetting("Zpew", this);
    private final BooleanSetting zpew = new BooleanSetting("Etherwarp", false);
    private final BooleanSetting zptp = new BooleanSetting("(WIP) Teleport", false);
    private final BooleanSetting zpInteract = new BooleanSetting("Zero Ping Interact", false);
    private final BooleanSetting assumeCancelInteract = new BooleanSetting("Assume Cancel Interact", false);
    @Getter
    private static final SaveSetting<Set<String>> ignoredRooms = new SaveSetting<>("Ignored rooms", "dungeon/zpew", "default.json", HashSet::new, new TypeToken<@NotNull Set<String>>() {}.getType(), true);

    private final BooleanSetting etherwarpSound = new BooleanSetting("Etherwarp Sound", false);
    private final StringSetting etherwarpSoundId = new StringSetting("Sound", "block.note_block.pling", false, false, etherwarpSound::getValue);
    private final NumberSetting etherwarpSoundVolume = new NumberSetting("Volume", 0, 10, 1, 0.1, etherwarpSound::getValue);
    private final NumberSetting etherwarpSoundPitch = new NumberSetting("Pitch", 0, 2, 1, 0.1, etherwarpSound::getValue);
    private int soundQueue = 0;

    private Pos renderPos;

    private final List<Long> noRotateSent = new ArrayList<>();
    private final List<Pos> zpewSent = new ArrayList<>();
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
        this.registerProperty(
                helperGroup,
                noRotateGroup,
                zpewGroup
        );

        this.getGroup().add(singleplayerEw);

        helperGroup.add(
                helper,
                correctColour,
                correctColourOutline,
                failColour,
                failColourOutline,
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
                ignoredRooms,
                etherwarpSound,
                etherwarpSoundId,
                etherwarpSoundVolume,
                etherwarpSoundPitch
        );
    }

    // singleplayer etherwarp
    public boolean onReceive(Packet<?> packet, ServerGamePacketListenerImpl packetListener) {
        if (!this.isEnabled() || !singleplayerEw.getValue() || !(packet instanceof ServerboundUseItemPacket useItemPacket)) return false;
        ServerPlayer player = packetListener.getPlayer();
        if (player.getInventory().getSelectedItem().getItem() != Items.DIAMOND_SHOVEL) return false;

        Pos pos;
        if (player.isShiftKeyDown()) {
            BlockPos temp = EtherUtils.getEtherPosFromOrigin(player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d), useItemPacket.getYRot(), useItemPacket.getXRot(), 61).getFirst();
            pos = temp == null ? null : new Pos(temp.getX() + 0.5d, temp.getY() + 1d, temp.getZ() + 0.5d);
        } else {
            pos = EtherUtils.predictTeleport(61, new Pos(player.position()), useItemPacket.getYRot(),  useItemPacket.getXRot());
        }

        if (pos == null) {
            return false;
        }

        packetListener.teleport(pos.x(), pos.y(), pos.z(), useItemPacket.getYRot(), useItemPacket.getXRot());
        playEtherwarpSound(player, pos.asVec3());
        return true;
    }

    @SubscribeEvent(receiveCancelled = true)
    public void onReceiveSound(PacketEvent.Receive event) {
        if (!this.isEnabled() || !this.etherwarpSound.getValue() || soundQueue <= 0) return;

        Packet<?> packet = event.getPacket();
        boolean dragonHurt = false;

        if (packet instanceof ClientboundSoundPacket soundPacket) {
            dragonHurt = soundPacket.getSound().value() == SoundEvents.ENDER_DRAGON_HURT;
        } else if (packet instanceof ClientboundSoundEntityPacket soundEntityPacket) {
            dragonHurt = soundEntityPacket.getSound().value() == SoundEvents.ENDER_DRAGON_HURT;
        }

        if (!dragonHurt) return;

        soundQueue--;
        event.setCancelled(true);
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (mc.screen != null || !helper.getValue() || mc.player == null || (!mc.player.isShiftKeyDown() && !alwaysShow.getValue())) return;
        ItemStack held = mc.player.getMainHandItem();
        if (!ItemUtils.isEtherwarp(held)) return;

        Vec3 pos = (renderPos == null ? (serverPos.getValue() ? mc.player.oldPosition() : mc.player.position()) : renderPos.asVec3()).add(0, EtherUtils.getSneakHeight(), 0);
        Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(pos, 57 + ItemUtils.getTunerDistance(held));
        if (ether.getFirst() == null) return;

        boolean canInteract = true;
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            canInteract = !isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock());
        }

        boolean canTp = ether.getSecond() && SbStatTracker.getStats().getMana().getCurrent() > 90 && canInteract && isRoomAllowed() && isRoomAllowing(ScanUtils.getRoomFromPos(ether.getFirst().getX(), ether.getFirst().getZ()));

        Colour colour = canTp ? this.correctColour.getValue() : this.failColour.getValue();
        Colour outline = canTp ? this.correctColourOutline.getValue() : this.failColourOutline.getValue();

        // VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : Utils.getBlockShape(ether.getFirst()));
        // AABB aabb = shape.bounds().move(ether.getFirst());
        AABB aabb = new AABB(0, 0, 0, 1, 1, 1).move(ether.getFirst());

        Renderer3D.addTask(switch (this.renderMode.getValue()) {
            case "Outline" -> new OutlineBox(aabb, outline, this.depth.getValue());
            case "Filled Outline" -> new FilledOutlineBox(aabb, colour, outline, this.depth.getValue());
            default -> new FilledBox(aabb, colour, this.depth.getValue());
        });
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
        if (!this.noRotate.getValue() || !this.teleportItem.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7)) || !isRoomAllowed()) return;
        ItemStack stack = mc.player.getInventory().getSelectedItem();
        if (!isTpItem(stack)) return;

        if (event.getResult() instanceof BlockHitResult blockHitResult) {
            if (isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) return;
        }


        if (!noRotateFromPackets.getValue()) noRotateSent.add(System.currentTimeMillis());
        if (zpew.getValue() || zptp.getValue())
            checkZpew(stack, event.getYRot(), event.getXRot());
    }

    @SubscribeEvent
    private void onUseItem(PacketEvent.Send event) {
        if (!this.noRotate.getValue() || !this.teleportItem.getValue() || !noRotateFromPackets.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7)) || !isRoomAllowed()) return;
        if (event.getPacket() instanceof ServerboundUseItemPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            if (!isTpItem(stack)) return;
            noRotateSent.add(System.currentTimeMillis());
            return;
        }

        if (event.getPacket() instanceof ServerboundUseItemOnPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            Block block =  mc.level.getBlockState(packet.getHitResult().getBlockPos()).getBlock();
            if (!isIgnored(block) && isTpItem(stack)) {
                noRotateSent.add(System.currentTimeMillis());
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

        boolean sneaking = mc.player.isShiftKeyDown();
        Pos currentPos = new Pos(renderPos == null ? mc.player.position() : renderPos.asVec3());
        Vec3 eyePos = currentPos.asVec3().add(0.0d, EtherUtils.getEyeHeight(), 0.0d);
        if (sneaking && ItemUtils.isEtherwarp(stack) && zpew.getValue()) {

            Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(eyePos, yaw, pitch, 57 + ItemUtils.getTunerDistance(stack));
            if (ether.getFirst() == null || !ether.getSecond()) return;

            renderPos = new Pos(ether.getFirst()).selfAdd(0.5d, 1.05d, 0.5d);
            playEtherwarpSound(mc.player, renderPos.asVec3());
            CameraHandler.registerProvider(this);
            zpewSent.add(renderPos.copy());
        } else if (!sneaking && zptp.getValue()) {
            long now = System.currentTimeMillis();
            boolean wimp = isWitherImpactItem(stack);
            if (wimp && now - lastWIMP < WITHER_IMPACT_COOLDOWN_MS) {
                return;
            }

            float distance = getTpDistance(stack);
            if (distance == 0) return;
            Pos prediction = EtherUtils.predictTeleport((int) distance, currentPos, yaw,  pitch);
//            Pos prediction = EtherUtils.predictTeleport(eyePos, yaw,  pitch, distance);
            if (prediction == null) return;

            Pos target = prediction.subtract(0.0d, 1.0d, 0.0d);
            target = resolveZptpTarget(target);
            if (target == null) return;
            if (isSameTeleportDestination(target, currentPos)) {
                return;
            }
            renderPos = target;
            CameraHandler.registerProvider(this);
            zpewSent.add(renderPos.copy());

            if (wimp) {
                lastWIMP = now;
            }
        }
    }

    private Pos resolveZptpTarget(Pos target) {
        if (isSafeZptpTarget(target)) return target;

        Pos above = target.above();
        return isSafeZptpTarget(above) ? above : null;
    }

    private boolean isSafeZptpTarget(Pos target) {
        if (mc.level == null) return false;

        BlockPos feet = target.asBlockPos();
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

    private boolean isSameTeleportDestination(Pos target, Pos currentPos) {
        return target.asBlockPos().equals(currentPos.asBlockPos());
    }

    private void playEtherwarpSound(Player player, Vec3 position) {
        if (player == null || !etherwarpSound.getValue() || mc.level == null) return;
        Optional<Holder.Reference<SoundEvent>> event = BuiltInRegistries.SOUND_EVENT.get(Identifier.withDefaultNamespace(this.etherwarpSoundId.getValue()));
        if (event.isEmpty()) return;
        soundQueue++;
        mc.level.playSound(player, position.x, position.y, position.z, event.get().value(), SoundSource.MASTER, etherwarpSoundVolume.getValue().floatValue(), etherwarpSoundPitch.getValue().floatValue());
    }

    // timeout stuff
    @SubscribeEvent
    private void onTick(ClientTickEvent.Start event) {
        long now = System.currentTimeMillis();
        noRotateSent.removeIf(t -> now - t >= timeout.getValue().longValue());
        if (noRotateSent.isEmpty() && renderPos != null) {
            renderPos = null;
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
        long now = System.currentTimeMillis();
        noRotateSent.removeIf(t -> now - t >= timeout.getValue().longValue());
        return this.alwaysNoRotate.getValue()
                || (!noRotateSent.isEmpty() && this.teleportItem.getValue()
                || this.outbounds.getValue() && !Dungeon.isStarted() && Location.getArea().is(Island.Dungeon)
        );
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onTP(PacketEvent.Receive event) {
        if (!this.noRotate.getValue() || !this.isEnabled() || !(event.getPacket() instanceof ClientboundPlayerPositionPacket packet)) return;
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
            this.renderPos = null;
        } else {
            Pos old = zpewSent.removeFirst();
            boolean correct = old.x() == newPos.position().x()
                    && old.y() == newPos.position().y()
                    && old.z() == newPos.position().z();
            if (!correct || zpewSent.isEmpty()) {
                this.zpewSent.clear();
                this.renderPos = null;
            }
        }
    }

    @Override
    public void reset() {
        this.noRotateSent.clear();
        this.zpewSent.clear();
        this.renderPos = null;
        this.lastWIMP = 0;
        this.soundQueue = 0;
    }

    private boolean isTpItem(ItemStack item) {
        String sbId = ItemUtils.getID(item);
        if (Utils.equalsOneOf(sbId, "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID", "ETHERWARP_CONDUIT", "ASPECT_OF_THE_LEECH_1", "ASPECT_OF_THE_LEECH_2", "ASPECT_OF_THE_LEECH_3")) return true;
        return Utils.equalsOneOf(sbId, "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA") && ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
    }

    private int getTpDistance(ItemStack item) {
        return switch (ItemUtils.getID(item)) {
            case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> 8 + ItemUtils.getTunerDistance(item);
            case "ASPECT_OF_THE_LEECH_1" -> 3;
            case "ASPECT_OF_THE_LEECH_2" -> 4;
            case "ASPECT_OF_THE_LEECH_3" -> 5;
            case "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA" -> ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3 ? 10 : 0;
            case null, default -> 0;
        };
    }

    private boolean isIgnored(Block block) {
        return (this.assumeCancelInteract.getValue() ? ignoredForCI : ignored).stream().anyMatch(c -> c.isInstance(block));
    }

    @Override
    public boolean shouldOverridePosition() {
        return this.isEnabled() && this.renderPos != null && (zpew.getValue() || zptp.getValue());
    }

    @Override
    public boolean shouldOverrideHitPos() {
        return this.isEnabled()
                && this.renderPos != null && (zpew.getValue() || zptp.getValue())
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
    public Vec3 getCameraPosition() {
        if (mc.player == null) return null;
        return this.renderPos.asVec3();
    }

    @Override
    public Vec3 getPosForHit() {
        return this.getCameraPosition();
    }

    @Override
    public Vec3 getRotForHit() {
        return Vec3.ZERO;
    }
}
