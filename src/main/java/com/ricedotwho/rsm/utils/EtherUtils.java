package com.ricedotwho.rsm.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class EtherUtils {
    public static final double STAND_EYE_HEIGHT = 1.6200000047683716;
    public static final double SNEAK_EYE_HEIGHT = 1.5399999618530273;
    public static final double SNEAK_HEIGHT_INVERTED = 0.0800000429153443;
    public static final double DEGREES_TO_RADIAN = Math.PI / 180.0;

    private static final Set<Class<? extends Block>> validTypes = new HashSet<>(Arrays.asList(
            ButtonBlock.class, CarpetBlock.class, SkullBlock.class,
            WallSkullBlock.class, LadderBlock.class, SaplingBlock.class,
            FlowerBlock.class, StemBlock.class, CropBlock.class,
            RailBlock.class, BubbleColumnBlock.class, SnowLayerBlock.class,
            TripWireBlock.class, TripWireHookBlock.class, FireBlock.class,
            AirBlock.class, TorchBlock.class, FlowerPotBlock.class,
            TallFlowerBlock.class, TallDryGrassBlock.class, BushBlock.class,
            SeagrassBlock.class, TallSeagrassBlock.class, SugarCaneBlock.class,
            LiquidBlock.class, VineBlock.class, MushroomBlock.class,
            PistonHeadBlock.class, CarpetBlock.class, WebBlock.class,
            DryVegetationBlock.class, SmallDripleafBlock.class, LeverBlock.class,
            NetherWartBlock.class, NetherPortalBlock.class, RedStoneWireBlock.class,
            ComparatorBlock.class, RedstoneTorchBlock.class, RepeaterBlock.class,
            VineBlock.class
    ));


    private static final BitSet validEtherwarpFeetIds = new BitSet(0);
    public static void initIDs() {
        BuiltInRegistries.BLOCK.forEach(block -> {
            for (Class<?> type : validTypes) {
                if (type.isInstance(block)) {
                    validEtherwarpFeetIds.set(Block.getId(block.defaultBlockState()));
                    break;
                }
            }
        });
    }

    public static float[] getYawAndPitch(double dx, double dy, double dz) {
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        double normalizedYaw = yaw < -180 ? yaw + 360 : yaw;

        return new float[] { (float) normalizedYaw, (float) pitch };
    }

    public static float[] getYawAndPitch(Vec3 pos, boolean sneaking, LocalPlayer playerSP, boolean doY) {
        double dx = pos.x - playerSP.getX();
        double dy = !doY ? 0 : pos.y - (playerSP.getY() + 1.62f - (sneaking ? SNEAK_HEIGHT_INVERTED : 0.0));
        double dz = pos.z - playerSP.getZ();
        return getYawAndPitch(dx, dy, dz);
    }

    public static BlockPos getEtherPosFromOrigin(Vec3 origin, float yaw, float pitch) {
        if (Minecraft.getInstance().player == null) return null;

        Vec3 endPos = Minecraft.getInstance().player.calculateViewVector(pitch, yaw).scale(60.0d).add(origin);
        return traverseVoxels(origin, endPos);
    }

    private static double getCoord(Vec3 vec, int i) {
        return switch (i) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> 0d;
        };
    }

    private static BlockPos traverseVoxels(Vec3 start, Vec3 end) {
        if (Minecraft.getInstance().level == null) return null;
        ClientLevel world = Minecraft.getInstance().level;

        Vec3 direction = end.subtract(start);

        int[] step = new int[3];
        for (int i = 0; i < 3; i++) {
            step[i] = (int) Math.signum(getCoord(direction, i));
        }

        double[] invDirection = new double[3];
        for (int i = 0; i < 3; i++) {
            double d = getCoord(direction, i);
            invDirection[i] = (d != 0.0) ? (1.0 / d) : Double.MAX_VALUE;
        }

        double[] tDelta = new double[3];
        for (int i = 0; i < 3; i++) {
            tDelta[i] = invDirection[i] * step[i];
        }

        int[] currentPos = new int[3];
        int[] endPos = new int[3];
        for (int i = 0; i < 3; i++) {
            currentPos[i] = (int) Math.floor(getCoord(start, i));
            endPos[i] = (int) Math.floor(getCoord(end, i));
        }

        double[] tMax = new double[3];
        for (int i = 0; i < 3; i++) {
            double startCoord = getCoord(start, i);
            tMax[i] = Math.abs((Math.floor(startCoord) + Math.max(step[i], 0) - startCoord) * invDirection[i]);
        }

        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(currentPos[0], currentPos[1], currentPos[2]);

            if (!Minecraft.getInstance().level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return null;
            ChunkAccess chunk = world.getChunk(pos);

            Block currentBlock = chunk.getBlockState(pos).getBlock();
            int currentBlockId = Block.getId(currentBlock.defaultBlockState());

            if (!validEtherwarpFeetIds.get(currentBlockId)) {
                int footBlockId = Block.getId(
                        chunk.getBlockState(
                                new BlockPos(
                                        pos.getX(),
                                        pos.getY() + 1,
                                        pos.getZ()
                                )
                        ).getBlock().defaultBlockState()
                );
                if (!validEtherwarpFeetIds.get(footBlockId)) return null;

                int headBlockId = Block.getId(
                        chunk.getBlockState(
                                new BlockPos(
                                        pos.getX(),
                                        pos.getY() + 2,
                                        pos.getZ()
                                )
                        ).getBlock().defaultBlockState()
                );
                if (!validEtherwarpFeetIds.get(headBlockId)) return null;

                return pos;
            }

            if (Arrays.equals(currentPos, endPos)) {
                return null;
            }

            int minIndex;
            if (tMax[0] <= tMax[1]) {
                minIndex = (tMax[0] <= tMax[2]) ? 0 : 2;
            } else {
                minIndex = (tMax[1] <= tMax[2]) ? 1 : 2;
            }

            tMax[minIndex] += tDelta[minIndex];
            currentPos[minIndex] += step[minIndex];
        }

        return null;
    }

    public static Vec3 rayTraceBlock(int maxDistance, float yaw, float pitch, Vec3 playerEyePos) {
        double roundedYaw = round(yaw, 14) * DEGREES_TO_RADIAN;
        double roundedPitch = round(pitch, 14) * DEGREES_TO_RADIAN;

        double cosPitch = Math.cos(roundedPitch);
        double dx = -cosPitch * Math.sin(roundedYaw);
        double dy = -Math.sin(roundedPitch);
        double dz = cosPitch * Math.cos(roundedYaw);

        int x = (int) Math.floor(playerEyePos.x());
        int y = (int) Math.floor(playerEyePos.y());
        int z = (int) Math.floor(playerEyePos.z());

        int stepX = dx < 0 ? -1 : 1;
        int stepY = dy < 0 ? -1 : 1;
        int stepZ = dz < 0 ? -1 : 1;

        double tDeltaX = Math.abs(1.0 / dx);
        double tDeltaY = Math.abs(1.0 / dy);
        double tDeltaZ = Math.abs(1.0 / dz);

        double tMaxX = (dx < 0 ? playerEyePos.x() - x : x + 1 - playerEyePos.x()) * tDeltaX;
        double tMaxY = (dy < 0 ? playerEyePos.y() - y : y + 1 - playerEyePos.y()) * tDeltaY;
        double tMaxZ = (dz < 0 ? playerEyePos.z() - z : z + 1 - playerEyePos.z()) * tDeltaZ;

        if (!isAir(new BlockPos(x, y, z))) {
            return new Vec3(playerEyePos.x(), playerEyePos.y(), playerEyePos.z());
        }

        int i = 0;
        while (i < maxDistance) {
            i++;

            double c = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));

            double hitX = Math.round((playerEyePos.x() + dx * c) * 1e10) * 1e-10;
            double hitY = Math.round((playerEyePos.y() + dy * c) * 1e10) * 1e-10;
            double hitZ = Math.round((playerEyePos.z() + dz * c) * 1e10) * 1e-10;

            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }

            if (!isAir(new BlockPos(x, y, z))) {
                return new Vec3(hitX, hitY, hitZ);
            }
        }

        return null;
    }

    private static boolean isAir(BlockPos pos) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;
        return Minecraft.getInstance().level.getBlockState(pos).getBlock() == Blocks.AIR;
    }

    private static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
