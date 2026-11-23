package com.ricedotwho.rsm.module.impl.movement;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Floor;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.SbStatTracker;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.managers.camera.CameraPositionProvider;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.managers.dungeon.map.DungeonInfo;
import com.ricedotwho.rsm.managers.dungeon.map.RoomType;
import com.ricedotwho.rsm.managers.dungeon.map.UniqueRoom;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.ToggleableGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.module.impl.player.CancelInteract;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.type.RenderMode;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
@ModuleInfo(aliases = "Ether", id = "ether", category = Category.MOVEMENT)
public class Ether extends Module implements CameraPositionProvider {

    @Getter
    private final static Ether instance = new Ether();
    private final BooleanSetting singleplayerEw = new BooleanSetting("Singleplayer", false);
    private final BooleanSetting dontDisable = new BooleanSetting("Don't disable", false);

    private final ToggleableGroupSetting helper = new ToggleableGroupSetting("Helper", this);
    private final DualColorSetting correctColor = new DualColorSetting("Correct", Color.GREEN, Color.GREEN.withAlpha(0.35F), false);
    private final DualColorSetting failColor = new DualColorSetting("Correct", Color.RED, Color.RED.withAlpha(0.35F), false);
    private final EnumSetting<RenderMode> renderMode = new EnumSetting<>("Render Mode", RenderMode.FILLED_OUTLINE);
    private final BooleanSetting depth = new BooleanSetting("Depth", true);
    private final BooleanSetting serverPos = new BooleanSetting("Server Position", true);
    private final BooleanSetting fullBlock = new BooleanSetting("Full Block", false);
    private final BooleanSetting alwaysShow = new BooleanSetting("Show While Unsneaked", false);

    private final ToggleableGroupSetting zpewGroup = new ToggleableGroupSetting("Camera Zpew", this);
    private final BooleanSetting zpew = new BooleanSetting("Etherwarp", false);
    private final BooleanSetting zptp = new BooleanSetting("Teleport (Bad)", false);
    private final BooleanSetting zpInteract = new BooleanSetting("Zero Ping Interact (UAYOR)", false);
    private final BooleanSetting assumeCancelInteract = new BooleanSetting("Assume Cancel Interact", false);
    @Getter
    private final SaveSetting<Set<String>> ignoredRooms = new SaveSetting<>("Ignored rooms", "dungeon/zpew", "default.json", HashSet::new, new TypeToken<@NotNull Set<String>>() {}.getType(), true);

    private final BooleanSetting etherwarpSound = new BooleanSetting("Etherwarp Sound", false);
    private final BooleanSetting zeroPingSound = new BooleanSetting("Zero Ping", false);
    private final SoundSetting sound = new SoundSetting("Sound", "block.note_block.pling").isVisible(etherwarpSound::getValue);

    private static final Set<String> TELEPORT_ITEMS = Set.of("ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID", "ETHERWARP_CONDUIT", "ASPECT_OF_THE_LEECH_1", "ASPECT_OF_THE_LEECH_2", "ASPECT_OF_THE_LEECH_3");
    private static final Set<String> WITHER_BLADES = Set.of("NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA");
    private static final Set<String> NOT_ALLOWED_ROOMS = Set.of("Boulder", "Teleport Maze");
    private int soundQueue = 0;

    private Vec3 renderVec3;
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

    public Ether() {
        this.getGeneralGroup().add(singleplayerEw);

        helper.add(
                correctColor,
                failColor,
                renderMode,
                depth,
                serverPos,
                fullBlock,
                alwaysShow
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
            BlockPos temp = EtherUtils.getEtherPosFromOrigin(player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d), useItemPacket.yRot(), useItemPacket.xRot(), 61).getFirst();
            vec3 = temp == null ? null : new Vec3(temp.getX() + 0.5d, temp.getY() + 1d, temp.getZ() + 0.5d);
        } else {
            vec3 = EtherUtils.predictTeleport(61, player.position(), useItemPacket.yRot(),  useItemPacket.xRot());
        }

        if (vec3 == null) {
            return false;
        }

        packetListener.teleport(vec3.x(), vec3.y(), vec3.z(), useItemPacket.yRot(), useItemPacket.xRot());
        playEtherwarpSound();
        return true;
    }

    @SubscribeEvent(receiveCancelled = true)
    public void onReceiveSound(PacketEvent.MainReceivePre event, ClientboundSoundPacket packet) {
        if (!this.isEnabled() || !this.etherwarpSound.getValue() || packet.getSound().value() != SoundEvents.ENDER_DRAGON_HURT) return;
        if (!zpewGroup.getValue().isEnabled() && !zpew.getValue() || zeroPingSound.getValue()) {
            playEtherwarpSound();
        }
        if (soundQueue <= 0) return;
        soundQueue--;
        event.setCancelled(true);
    }

    private static Vec3 lastPosition() {
        assert mc.player != null;
        return new Vec3(mc.player.xLast, mc.player.yLast, mc.player.zLast);
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (mc.gui.screen() != null || !helper.getValue().isEnabled() || mc.player == null || (!mc.player.getLastSentInput().shift() && !alwaysShow.getValue())) return;
        ItemStack held = mc.player.getMainHandItem();
        if (!ItemUtils.isEtherwarp(held)) return;

        Vec3 vec3 = (renderVec3 == null ? (serverPos.getValue() ? lastPosition() : mc.player.position()) : renderVec3).add(0, EtherUtils.getSneakHeight(), 0);
        Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(vec3, 57 + ItemUtils.getTunerDistance(held));
        if (ether.getFirst() == null) return;

        boolean canInteract = true;
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            assert mc.level != null;
            canInteract = !isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock());
        }

        boolean canTp = ether.getSecond() && canInteract && isRoomAllowed() && isRoomAllowing(DungeonInfo.getRoomFromPos0(ether.getFirst().getX(), ether.getFirst().getZ()));

        Color fill = canTp ? this.correctColor.getOtherValue() : this.failColor.getOtherValue();
        Color outline = canTp ? this.correctColor.getValue() : this.failColor.getValue();

         VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : Utils.getBlockShape(ether.getFirst()));
         AABB aabb = shape.bounds().move(ether.getFirst());
         this.renderMode.getValue().draw(event, aabb, fill, outline, this.depth.getValue());
    }

    private boolean isRoomAllowed() {
        if (dontDisable.getValue()) return true;
        boolean allowed = Dungeon.current() == null || !NOT_ALLOWED_ROOMS.contains(Dungeon.current().getName()) && Dungeon.current().getType() != RoomType.TRAP && !ignoredRooms.getValue().contains(Dungeon.current().getName());

        if (!allowed) {
            var modoloX = (mc.player.getBlockX() + 201) % 32;
            var modoloZ = (mc.player.getBlockZ() + 201) % 32;
            if (modoloX == 0 || modoloZ == 0) return true;
        }

        return allowed;
    }

    private boolean isRoomAllowing(UniqueRoom room) {
        if (dontDisable.getValue()) return true;
        return room == null || !NOT_ALLOWED_ROOMS.contains(room.getName());
    }

    @SubscribeEvent
    private void onPlayerUse(PlayerInputEvent.Use event) {
        if (!zpewGroup.getValue().isEnabled()
                || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7))
                || event.getHand() != InteractionHand.MAIN_HAND
                || !isRoomAllowed()
        ) return;
        assert mc.player != null;
        ItemStack stack = mc.player.getInventory().getSelectedItem();
        if (!isTpItem(stack)) return;

        if (event.getResult() instanceof BlockHitResult blockHitResult) {
            assert mc.level != null;
            if (isIgnored(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) return;
        }

        var sneaking = mc.player.getLastSentInput().shift();
        if (sneaking && SbStatTracker.getStats().getMana().getCurrent() < 180) return;

        Vec3 currentVec3 = renderVec3 == null ? lastPosition() : renderVec3;
        Vec3 eyeVec3 = currentVec3.add(0.0d, EtherUtils.getEyeHeight(), 0.0d);
        if (sneaking && ItemUtils.isEtherwarp(stack) && zpew.getValue()) {
            var ether = EtherUtils.getEtherPosFromOrigin(eyeVec3, event.getYRot(), event.getXRot(), 57 + ItemUtils.getTunerDistance(stack));
            if (ether.getFirst() == null || !ether.getSecond()) return;

            if (!isRoomAllowing(DungeonInfo.getRoomFromPos0(ether.getFirst().getX(), ether.getFirst().getZ()))) return;

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
            Vec3 prediction = EtherUtils.predictTeleport((int) distance, currentVec3, event.getYRot(),  event.getXRot());
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
        if (!WITHER_BLADES.contains(itemId)) {
            return false;
        }

        return ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
    }

    private boolean isSameTeleportDestination(Vec3 target, Vec3 currentVec3) {
        return target.toBlockPos().equals(currentVec3.toBlockPos());
    }

    private void playEtherwarpSound() {
        if (!etherwarpSound.getValue() || mc.level == null) return;
        soundQueue++;
        sound.play();
    }

    @SubscribeEvent
    private void onEnterBoss(DungeonEvent.EnterBoss event) {
        reset();
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onTP(PacketEvent.MainReceivePost event, ClientboundPlayerPositionPacket packet) {
        if (!zpewGroup.getValue().isEnabled()) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        PositionMoveRotation startPos = PositionMoveRotation.of(player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());

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
        this.zpewSent.clear();
        this.renderVec3 = null;
        this.lastWIMP = 0;
        this.soundQueue = 0;
    }

    public static boolean isTpItem(ItemStack item) {
        String sbId = ItemUtils.getID(item);
        if (TELEPORT_ITEMS.contains(sbId)) return true;
        return WITHER_BLADES.contains(sbId) && ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
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
        return (instance.assumeCancelInteract.getValue() || CancelInteract.getInstance().isEnabled() ? CancelInteract.WHITELIST : ignored).stream().anyMatch(c -> c.isInstance(block));
    }

    @Override
    public boolean shouldOverridePosition() {
        return this.isEnabled() && this.renderVec3 != null && zpewGroup.getValue().isEnabled();
    }

    @Override
    public boolean shouldOverrideHitPos() {
        return this.isEnabled()
                && this.renderVec3 != null
                && zpewGroup.getValue().isEnabled()
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
        return this.renderVec3;
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
