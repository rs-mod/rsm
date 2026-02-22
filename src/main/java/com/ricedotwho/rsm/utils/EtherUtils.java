package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@UtilityClass
public class EtherUtils implements Accessor {
    public final double STAND_EYE_HEIGHT = 1.6200000047683716;
    public final double SNEAK_EYE_HEIGHT = 1.5399999618530273f; // Change to 1.27d when update to 1.21.10
    public final double SNEAK_HEIGHT_INVERTED = 0.0800000429153443;
    public final double DEGREES_TO_RADIAN = Math.PI / 180.0;
    public final double EPSILON = 0.001f;

    private final Set<Class<? extends Block>> validTypes = new HashSet<>(Arrays.asList(
            ButtonBlock.class, CarpetBlock.class, SkullBlock.class,
            WallSkullBlock.class, LadderBlock.class, SaplingBlock.class,
            FlowerBlock.class, StemBlock.class, CropBlock.class,
            RailBlock.class, BubbleColumnBlock.class, SnowLayerBlock.class,
            TripWireBlock.class, TripWireHookBlock.class, FireBlock.class,
            AirBlock.class, TorchBlock.class, FlowerPotBlock.class,
            TallFlowerBlock.class, TallDryGrassBlock.class, BushBlock.class,
            SeagrassBlock.class, TallSeagrassBlock.class, SugarCaneBlock.class,
            LiquidBlock.class, VineBlock.class, MushroomBlock.class, TallGrassBlock.class,
            PistonHeadBlock.class, CarpetBlock.class, WebBlock.class, ShortDryGrassBlock.class,
            DryVegetationBlock.class, SmallDripleafBlock.class, LeverBlock.class,
            NetherWartBlock.class, NetherPortalBlock.class, RedStoneWireBlock.class,
            ComparatorBlock.class, RedstoneTorchBlock.class, RepeaterBlock.class,
            VineBlock.class
    ));

    // teleport
    private static final double STEPS = 100;

    private final Set<Class<? extends Block>> IGNORED = new HashSet<>(Arrays.asList(
            AirBlock.class, FireBlock.class, LiquidBlock.class, CarpetBlock.class,
            MushroomBlock.class, NetherWartBlock.class, NetherPortalBlock.class,
            RedStoneWireBlock.class, ComparatorBlock.class, RedstoneTorchBlock.class,
            RepeaterBlock.class, TripWireBlock.class, ButtonBlock.class, RailBlock.class,
            BubbleColumnBlock.class, SaplingBlock.class
    ));

    private final Set<Class<? extends Block>> IGNORED2 = new HashSet<>(Arrays.asList(
            SlabBlock.class
    ));

    private final Set<Class<? extends Block>> SPECIAL = new HashSet<>(Arrays.asList(
            LadderBlock.class,
            VineBlock.class,
            WaterlilyBlock.class
    ));

    private final Set<Class<? extends Block>> IGNORED_BLOCKS_CLASSES = new HashSet<>(Arrays.asList(
            ButtonBlock.class, AirBlock.class, CarpetBlock.class, RedStoneWireBlock.class, MushroomBlock.class,
            FlowerBlock.class, StemBlock.class, CropBlock.class, TripWireBlock.class, RailBlock.class
    ));

    private static final List<Block> IGNORED_BLOCKS = Arrays.asList(
            Blocks.LAVA,
            Blocks.WATER
    );

    private final Set<Class<? extends Block>> SPECIAL_BLOCKS = new HashSet<>(Arrays.asList(
        LadderBlock.class, VineBlock.class, WaterlilyBlock.class
    ));


    private final BitSet validEtherwarpFeetIds = new BitSet(0);
    private final BitSet ignored = new BitSet(0);
    private final BitSet ignored2 = new BitSet(0);
    private final BitSet special = new BitSet(0);
    public void initIDs() {
        BuiltInRegistries.BLOCK.forEach(block -> {
            for (Class<?> type : validTypes) {
                if (type.isInstance(block)) {
                    validEtherwarpFeetIds.set(Block.getId(block.defaultBlockState()));
                    break;
                }
            }

//            for (Class<?> type : IGNORED) {
//                if (type.isInstance(block)) {
//                    validEtherwarpFeetIds.set(Block.getId(block.defaultBlockState()));
//                    break;
//                }
//            }
//
//            for (Class<?> type : validTypes) {
//                if (type.isInstance(block)) {
//                    validEtherwarpFeetIds.set(Block.getId(block.defaultBlockState()));
//                    break;
//                }
//            }
//
//            for (Class<?> type : validTypes) {
//                if (type.isInstance(block)) {
//                    validEtherwarpFeetIds.set(Block.getId(block.defaultBlockState()));
//                    break;
//                }
//            }
        });
    }

    public float[] getYawAndPitch(double dx, double dy, double dz) {
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        double normalizedYaw = yaw < -180 ? yaw + 360 : yaw;

        return new float[] { (float) normalizedYaw, (float) pitch };
    }

    public float[] getYawAndPitch(Vec3 pos, boolean sneaking, LocalPlayer playerSP, boolean doY) {
        double dx = pos.x - playerSP.getX();
        double dy = !doY ? 0 : pos.y - (playerSP.getY() + 1.62f - (sneaking ? SNEAK_HEIGHT_INVERTED : 0.0));
        double dz = pos.z - playerSP.getZ();
        return getYawAndPitch(dx, dy, dz);
    }

    public BlockPos fastGetEtherFromOrigin(Vec3 start, float yaw, float pitch, int dist) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return null;
        Vec3 end = Minecraft.getInstance().player.calculateViewVector(pitch, yaw).scale(dist).add(start);
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


        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 1000; i++) {
            pos.set(currentPos[0], currentPos[1], currentPos[2]);

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

            if (currentPos[0] == endPos[0] && currentPos[1] == endPos[1] && currentPos[2] == endPos[2]) {
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

    public Pair<BlockPos, Boolean> getEtherPosFromOrigin(Vec3 origin, float yaw, float pitch, int dist) {
        if (mc.player == null) return new Pair<>(null, false);

        Vec3 endPos = mc.player.calculateViewVector(pitch, yaw).scale(dist).add(origin);
        return traverseVoxels(origin, endPos);
    }

    public Pair<BlockPos, Boolean> getEtherPosFromOrigin(Vec3 origin, int distance) {
        if (mc.player == null) return new Pair<>(null, false);

        Vec3 endPos = mc.player.getLookAngle().scale(distance).add(origin);
        return traverseVoxels(origin, endPos);
    }

    private double getCoord(Vec3 vec, int i) {
        return switch (i) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> 0d;
        };
    }

    private Pair<BlockPos, Boolean> traverseVoxels(Vec3 start, Vec3 end) {
        if (mc.level == null) return new Pair<>(null, false);
        ClientLevel world = mc.level;

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

            if (!Minecraft.getInstance().level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return new Pair<>(null, false);
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
                if (!validEtherwarpFeetIds.get(footBlockId)) return new Pair<>(pos, false);

                int headBlockId = Block.getId(
                        chunk.getBlockState(
                                new BlockPos(
                                        pos.getX(),
                                        pos.getY() + 2,
                                        pos.getZ()
                                )
                        ).getBlock().defaultBlockState()
                );
                if (!validEtherwarpFeetIds.get(headBlockId)) return new Pair<>(pos, false);

                return new Pair<>(pos, true);
            }

            if (Arrays.equals(currentPos, endPos)) {
                return new Pair<>(null, false);
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

        return new Pair<>(null, false);
    }

    private static int getBlockId(BlockPos pos, ChunkAccess chunk) {
        return Block.getId(chunk.getBlockState(pos).getBlock().defaultBlockState());
    }

    public static boolean isValidEtherwarpPosition(BlockPos pos) {
        if (Minecraft.getInstance().level == null) return false;
        ChunkAccess chunk = Minecraft.getInstance().level.getChunk(pos);

        if (validEtherwarpFeetIds.get(getBlockId(pos, chunk))) return false;
        if (!validEtherwarpFeetIds.get(getBlockId(pos.above(1), chunk))) return false;
        if (!validEtherwarpFeetIds.get(getBlockId(pos.above(2), chunk))) return false;
        return true;
    }

    public Vec3 rayTraceBlock(int maxDistance, float yaw, float pitch, Vec3 playerEyePos) {
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

    private boolean isAir(BlockPos pos) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;
        Block block = Minecraft.getInstance().level.getBlockState(pos).getBlock();
        int currentBlockId = Block.getId(block.defaultBlockState());
        //ChatUtils.chat(block.getName() + " : " + bl);
        return validEtherwarpFeetIds.get(currentBlockId);
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public Pos predictTeleport(int distance, Pos start, float yaw, float pitch) {
        Pos forward = Pos.fromRotation(pitch, yaw).multiply(1.0 / STEPS);
        Pos player = start.add(0.0, STAND_EYE_HEIGHT, 0.0);
        Pos cur = new Pos(player);
        int i = 0;

        while(true) {
            if ((double)i < (double)distance * STEPS) {
                if ((double)i % STEPS == 0.0 && !isSpecial(cur) && !isSpecial(cur) && !isIgnored(cur)) {
                    cur.selfAdd(forward.multiply(-STEPS));
                    return i != 0 && isIgnored(cur) ? new Pos(Math.floor(cur.x()) + 0.5, Math.floor(cur.y()), Math.floor(cur.z()) + 0.5) : null;
                }

                if ((isIgnored2(cur) || !inBB(cur)) && (isIgnored2(cur.add(0.0, 1.0, 0.0)) || !inBB(cur.add(0.0, 1.0, 0.0)))) {
                    cur.selfAdd(forward);
                    ++i;
                    continue;
                }

                cur.selfAdd(forward.multiply(-STEPS));
                if (i == 0 || !isIgnored(cur) && inBB(cur) || !isIgnored(cur.add(0.0, 1.0, 0.0)) && inBB(cur.add(0.0, 1.0, 0.0))) {
                    return null;
                }
            }

            Pos pos = player.add(Pos.fromRotation(pitch, yaw).multiply(Math.floor((double)i / STEPS)));
            if ((isIgnored(cur) || !inBB(cur)) && (isIgnored(cur.add(0.0, 1.0, 0.0)) || !inBB(cur.add(0.0, 1.0, 0.0)))) {
                return new Pos(Math.floor(pos.x()) + 0.5, Math.floor(pos.y()), Math.floor(pos.z()) + 0.5);
            }

            return null;
        }
    }

    public Pos predictTeleportNoCheck(int distance, Pos start, float yaw, float pitch) {
        Pos player = start.add(0.0, STAND_EYE_HEIGHT, 0.0);
        Pos dir = Pos.fromRotation(pitch, yaw);
        Pos end = player.add(dir.multiply(distance));
        return new Pos(
                Math.floor(end.x()) + 0.5,
                Math.floor(end.y()),
                Math.floor(end.z()) + 0.5
        );
    }

    private boolean isIgnored(Pos pos) {
        BlockState state = mc.level.getBlockState(pos.asBlockPos());
        return isIgnored(state);
    }

    private boolean isIgnored(BlockState state) {
        return IGNORED_BLOCKS.contains(state.getBlock())
                || IGNORED_BLOCKS_CLASSES.stream().anyMatch(c -> c.isInstance(state.getBlock()));
    }

    private boolean isIgnored2(Pos pos) {
        BlockState state = mc.level.getBlockState(pos.asBlockPos());
        return isIgnored(state) || state.getBlock() instanceof SlabBlock;
    }

    public boolean isSpecial(Pos pos) {
        BlockState state = mc.level.getBlockState(pos.asBlockPos());
        return SPECIAL_BLOCKS.stream().anyMatch(c -> c.isInstance(state.getBlock()));
    }

    // todo: verify if this is even correct
    public boolean inBB(Pos pos) {
        // if (!isSpecial(x, y, z)) return true;
        BlockState block = mc.level.getBlockState(pos.asBlockPos());
        AABB bb = block.getShape(mc.level, pos.asBlockPos()).bounds();
        return bb.contains(pos.asVec3());
    }

//    private int getIdFromPos(BlockPos pos) {
//        return Block.getId(
//                mc.level.getChunk(pos).getBlockState(
//                        new BlockPos(
//                                pos.getX(),
//                                pos.getY() + 2,
//                                pos.getZ()
//                        )
//                ).getBlock().defaultBlockState());
//    }
//
//
//    public Pos predictTeleport(Vec3 start, float yaw, float pitch, float distance) {
//        Pos cur = new Pos(start);
//        Pos forward = Pos.fromRotation(pitch, yaw);
//        int stepsTaken = 0;
//        for (int i = 0; i < (int)(distance * STEPS) + 1; i++) {
//            if (i % STEPS == 0 && !isSpecial(cur) && !isSpecial(cur.add(0, 1, 0))) {
//                if (!isIgnored(cur) || !isIgnored(cur.add(0, 1, 0))) {
//                    cur = cur.add(forward.multiply(-STEPS));
//                    if (i == 0 || !isIgnored(cur) || !isIgnored(cur.add(0, 1, 0))) {
//                        return null;
//                    }
//                    return new Pos(Mth.floor(cur.x) + 0.5, Mth.floor(cur.y), Mth.floor(cur.z) + 0.5);
//                }
//            }
//            if ((!isIgnored2(cur) && inBB(cur)) || (!isIgnored2(cur.add(0, 1, 0)) && inBB(cur.add(0, 1, 0)))) {
//                cur = cur.add(forward.multiply(-STEPS));
//                if (i == 0 || (!isIgnored(cur) && inBB(cur)) || (!isIgnored(cur.add(0, 1, 0)) && inBB(cur.add(0, 1, 0)))) {
//                    return null;
//                }
//                stepsTaken = i;
//                break;
//            }
//            cur = cur.add(forward);
//            stepsTaken = i;
//        }
//        float multiplicationFactor = Mth.floor(stepsTaken / STEPS);
//        Vec3 pos = start.add(Pos.fromRotation(pitch, yaw).multiply(multiplicationFactor).asVec3());
//        if ((!isIgnored(cur) && inBB(cur)) || (!isIgnored(cur.add(0, 1, 0)) && inBB(cur.add(0, 1, 0)))) return null;
//        return new Pos(Mth.floor(pos.x) + 0.5, Mth.floor(pos.y), Mth.floor(pos.z) + 0.5);
//    }
//
//    @Nullable
//    private Pos predictTeleport2(Vec3 start, float yaw, float pitch, float distance) {
//        Pos cur = new Pos(start);
//
//        Pos forward = Pos.fromRotation(pitch, yaw)
//                .multiply(1f / STEPS);
//
//        int stepsTaken = 0;
//        int maxSteps = (int) (distance * STEPS);
//
//        for (int i = 0; i <= maxSteps; i++) {
//
//            if (i % STEPS == 0 && !isSpecial(cur) && !isSpecial(cur.above())) {
//                if (!isIgnored(cur) || !isIgnored(cur.above())) {
//                    cur = cur.add(forward.multiply(-STEPS));
//
//                    if (i == 0 || !isIgnored(cur) || !isIgnored(cur.above())) {
//                        return null;
//                    }
//
//                    return new Pos(
//                            Math.floor(cur.x) + 0.5,
//                            Math.floor(cur.y),
//                            Math.floor(cur.z) + 0.5
//                    );
//                }
//            }
//
//            if ((!isIgnored2(cur) && inBB(cur))
//                    || (!isIgnored2(cur.above()) && inBB(cur.above()))) {
//
//                cur = cur.add(forward.multiply(-STEPS));
//
//                if (i == 0
//                        || (!isIgnored(cur) && inBB(cur))
//                        || (!isIgnored(cur.above()) && inBB(cur.above()))) {
//                    return null;
//                }
//
//                stepsTaken = i;
//                break;
//            }
//
//            cur = cur.add(forward);
//            stepsTaken = i;
//        }
//
//        float multiplicationFactor = (float) Math.floor((float) stepsTaken / STEPS);
//
//        Pos pos = new Pos(start).add(Pos.fromRotation(pitch, yaw).multiply(multiplicationFactor)
//        );
//
//        if ((!isIgnored(cur) && inBB(cur))
//                || (!isIgnored(cur.above()) && inBB(cur.above()))) {
//            return null;
//        }
//
//        return new Pos(
//                Math.floor(pos.x) + 0.5,
//                Math.floor(pos.y),
//                Math.floor(pos.z) + 0.5
//        );
//    }
}
