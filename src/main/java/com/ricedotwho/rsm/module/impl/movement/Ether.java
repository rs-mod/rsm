package com.ricedotwho.rsm.module.impl.movement;


import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.camera.CameraPositionProvider;
import com.ricedotwho.rsm.event.impl.game.HitProcessEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.SbStatTracker;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Ether", id = "Ether", category = Category.MOVEMENT)
public class Ether extends Module implements CameraPositionProvider {

    private final DefaultGroupSetting helperGroup = new DefaultGroupSetting("Helper", this);
    private final BooleanSetting helper = new BooleanSetting("Enabled", false);
    private final ColourSetting correctColour = new ColourSetting("Correct", new Colour(0, 255, 0, 90));
    private final ColourSetting failColour = new ColourSetting("Fail", new Colour(255, 0, 0, 90));
    private final ModeSetting renderMode = new ModeSetting("Render Mode", "Filled Outline", List.of("Outline", "Filled Outline", "Filled"));
    private final BooleanSetting depth = new BooleanSetting("Depth", true);
    private final BooleanSetting serverPos = new BooleanSetting("Server Position", true);
    private final BooleanSetting fullBlock = new BooleanSetting("Full Block", false);

    private final DefaultGroupSetting noRotateGroup = new DefaultGroupSetting("No Rotate", this);
    private final BooleanSetting noRotate = new BooleanSetting("Enabled", false);
    private final BooleanSetting teleportItem = new BooleanSetting("Teleport Items", true);
    private final BooleanSetting outbounds = new BooleanSetting("Outbounds", false);
    private final BooleanSetting alwaysNoRotate = new BooleanSetting("Always No Rotate", false);
    private final NumberSetting timeout = new NumberSetting("Timeout", 250, 2000, 1000, 25);

    private final DefaultGroupSetting zpewGroup = new DefaultGroupSetting("Zpew", this);
    private final BooleanSetting zpew = new BooleanSetting("Etherwarp", false);
    private final BooleanSetting zptp = new BooleanSetting("(WIP) Teleport", false);
    private final BooleanSetting zpInteract = new BooleanSetting("Zero Ping Interact", false);

    private Pos renderPos;

    private final List<Long> noRotateSent = new ArrayList<>();
    private final List<Pos> zpewSent = new ArrayList<>();

    private static final List<Block> ignored = Arrays.asList(
            Blocks.HOPPER,
            Blocks.ANVIL,
            Blocks.DAMAGED_ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.CHEST, // copper chest ?
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.LEVER
    );

    public Ether() {
        this.registerProperty(
                helperGroup,
                noRotateGroup,
                zpewGroup
        );

        helperGroup.add(
                helper,
                correctColour,
                failColour,
                renderMode,
                depth,
                serverPos,
                fullBlock
        );

        noRotateGroup.add(
                noRotate,
                teleportItem,
                outbounds,
                alwaysNoRotate,
                timeout
        );

        zpewGroup.add(
                zpew,
                zptp,
                zpInteract
        );
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (mc.screen != null || !helper.getValue() || mc.player == null || !mc.player.isShiftKeyDown()) return;
        ItemStack held = mc.player.getMainHandItem();
        if (!ItemUtils.isEtherwarp(held)) return;

        Vec3 pos = (renderPos == null ? (serverPos.getValue() ? mc.player.oldPosition() : mc.player.position()) : renderPos.asVec3()).add(0, EtherUtils.SNEAK_EYE_HEIGHT, 0);
        Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(pos, 57 + ItemUtils.getTunerDistance(held));
        if (ether.getFirst() == null) return;

        boolean canInteract = true;
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            canInteract = !ignored.contains(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock());
        }

        Colour colour = ether.getSecond() && SbStatTracker.getStats().getMana().getCurrent() > 90 && canInteract ? this.correctColour.getValue() : this.failColour.getValue();
        VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : Utils.getBlockShape(ether.getFirst()));
        AABB aabb = shape.bounds().move(ether.getFirst());

        Renderer3D.addTask(switch (this.renderMode.getValue()) {
            case "Outline" -> new OutlineBox(aabb, colour.alpha(255), this.depth.getValue());
            case "Filled Outline" -> new FilledOutlineBox(aabb, colour, colour.alpha(255), this.depth.getValue());
            default -> new FilledBox(aabb, colour, this.depth.getValue());
        });
    }

    @SubscribeEvent
    public void onUseItem(PacketEvent.Send event) {
        if (!this.noRotate.getValue() || !this.teleportItem.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7))) return;
        if (event.getPacket() instanceof ServerboundUseItemPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            if (!isTpItem(stack)) return;
            noRotateSent.add(System.currentTimeMillis());
            if (zpew.getValue() || zptp.getValue())
                checkZpew(stack, packet.getYRot(), packet.getXRot());
            return;
        }

        if (event.getPacket() instanceof ServerboundUseItemOnPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            Block block =  mc.level.getBlockState(packet.getHitResult().getBlockPos()).getBlock();
            if (!ignored.contains(block) && isTpItem(stack)) {
                noRotateSent.add(System.currentTimeMillis());
                //checkZpew(stack, mc.player.getYRot(), mc.player.getXRot());
            }
        }
    }

    private void checkZpew(ItemStack stack, float yaw, float pitch) {
        if (mc.level == null || mc.player == null || !isTpItem(stack) || SbStatTracker.getStats().getMana().getCurrent() < 180) return;

        // tspmo
        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            if (ignored.contains(mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) return;
        }

        boolean sneaking = mc.player.isShiftKeyDown();
        Vec3 eyePos = (renderPos == null ? mc.player.position() : renderPos.asVec3()).add(0.0d, sneaking ? EtherUtils.SNEAK_EYE_HEIGHT : EtherUtils.STAND_EYE_HEIGHT, 0.0d);
        if (sneaking && ItemUtils.isEtherwarp(stack) && zpew.getValue()) {

            Pair<BlockPos, Boolean> ether = EtherUtils.getEtherPosFromOrigin(eyePos, yaw, pitch, 57 + ItemUtils.getTunerDistance(stack));
            if (ether.getFirst() == null || !ether.getSecond()) return;

            renderPos = new Pos(ether.getFirst()).selfAdd(0.5d, 1.05d, 0.5d);
            CameraHandler.registerProvider(this);
            zpewSent.add(renderPos.copy());
        } else if (!sneaking && zptp.getValue()) {
            float distance = getTpDistance(stack);
            if (distance == 0) return;
            Pos prediction = EtherUtils.predictTeleport((int) distance, new Pos(renderPos == null ? mc.player.position() : renderPos.asVec3()), yaw,  pitch);
//            Pos prediction = EtherUtils.predictTeleport(eyePos, yaw,  pitch, distance);
            if (prediction == null) return;
            renderPos = prediction;
            CameraHandler.registerProvider(this);
            zpewSent.add(renderPos.copy());
        }
    }


    @SubscribeEvent
    public void onEnterBoss(DungeonEvent.EnterBoss event) {
        reset();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    private boolean shouldNoRotate() {
        long now = System.currentTimeMillis();
        noRotateSent.removeIf(t -> now - t >= timeout.getValue());
        return this.alwaysNoRotate.getValue()
                || (!noRotateSent.isEmpty() && this.teleportItem.getValue()
                || this.outbounds.getValue() && !Dungeon.isStarted() && Location.getArea().is(Island.Dungeon)
        );
    }

    public void onHandleMovePlayer(ClientboundPlayerPositionPacket packet, Connection connection, CallbackInfo ci) {
        if (!this.noRotate.getValue() || !this.isEnabled()) return;
        LocalPlayer player = mc.player;
        if (player == null) return;

        PositionMoveRotation startPos = PositionMoveRotation.of(player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());

        if (this.zpew.getValue() || this.zptp.getValue()) handleZpew(newPos);

        if (!shouldNoRotate()) return;
        if (!noRotateSent.isEmpty()) noRotateSent.removeFirst();

        player.setPos(newPos.position());
        player.setDeltaMovement(newPos.deltaMovement());

        PositionMoveRotation oldPlayerPos = new PositionMoveRotation(player.oldPosition(), Vec3.ZERO, player.yRotO, player.xRotO);
        PositionMoveRotation newOldPlayerPos = PositionMoveRotation.calculateAbsolute(oldPlayerPos, packet.change(), packet.relatives());

        player.setOldPosAndRot(newOldPlayerPos.position(), player.yRotO, player.xRotO); // i would prefer to just set position here, but fun is private

        connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), newPos.yRot(), newPos.xRot(), false, false));

        ((LocalPlayerAccessor) player).setYRotLast(newPos.yRot());
        ((LocalPlayerAccessor) player).setXRotLast(newPos.xRot());

        ci.cancel();
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

    @Override
    public boolean shouldOverridePosition() {
        return this.isEnabled() && this.renderPos != null && (zpew.getValue() || zptp.getValue());
    }

    @Override
    public boolean shouldBlockKeyboardMovement() {
        return false;
    }

    @Override
    public Vec3 getCameraPosition() {
        if (Minecraft.getInstance().player == null) return null;
        return this.renderPos.add(0.0d, Minecraft.getInstance().player.getEyeHeight(), 0.0d).asVec3();
    }

    @SubscribeEvent
    public void onHitProcessPosition(HitProcessEvent.Position event) {
        if (shouldOverridePosition())
            event.getPositionVectorConsumer().accept(getCameraPosition());
    }
}
