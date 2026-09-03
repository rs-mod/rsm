package com.ricedotwho.rsm.type;

import com.google.gson.JsonPrimitive;
import lombok.val;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public class Pos extends Vec3 implements Accessor {
    public Pos() {
        super(0, 0, 0);
    }

    public Pos(@Nullable BlockPos pos) {
        val x = pos == null ? 0 : pos.getX();
        val y = pos == null ? 0 : pos.getY();
        val z = pos == null ? 0 : pos.getZ();
        super(x, y, z);
    }

    public Pos(Vec3 vec) {
        val x = vec == null ? 0 : vec.x;
        val y = vec == null ? 0 : vec.y;
        val z = vec == null ? 0 : vec.z;
        super(x, y, z);
    }

    public Pos(final double x, final double y, final double z) {
        super(x, y, z);
    }

    public Pos copy() {
        return new Pos(this);
    }

    @Override
    public @NonNull String toString() {
        return "Pos"
                + "{"
                + "x=" + this.x
                + ",y=" + this.y
                + ",z=" + this.z
                + "}";
    }
    public String toNiceString() {
        return  "x: " + this.x
                + ", y:" + this.y
                + ", z: " + this.z;
    }
    public String toChatString() {
        return this.x
                + "," + this.y
                + "," + this.z;
    }


    public BlockPos asBlockPos() {
        return new BlockPos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
    }

    public double get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> -1;
        };
    }

    public double distanceTo(Pos pos) {
        return Math.sqrt(squaredDistanceTo(pos));
    }

    public double squaredDistanceTo(Pos pos) {
        double d = pos.x - this.x;
        double e = pos.y - this.y;
        double f = pos.z - this.z;
        return d * d + e * e + f * f;
    }

    public double squaredDistanceTo(Vec3 pos) {
        double d = pos.x - this.x;
        double e = pos.y - this.y;
        double f = pos.z - this.z;
        return d * d + e * e + f * f;
    }

    public static BlockPos blockPos(Vec3 vec3) {
        return new BlockPos(Mth.floor(vec3.x), Mth.floor(vec3.y), Mth.floor(vec3.z));
    }

    @Override
    public @NonNull Pos add(final double x, final double y, final double z) {
        return new Pos(this.x + x, this.y + y, this.z + z);
    }

    public @NotNull Pos add(final double factor) {
        return new Pos(this.x + factor, this.y + factor, this.z + factor);
    }

    public Pos add(final Pos pos) {
        return new Pos(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    public Pos subtract(final Pos pos) {
        return new Pos(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    public @NonNull Pos subtract(final double x, final double y, final double z) {
        return new Pos(this.x - x, this.y - y, this.z - z);
    }

    public @NonNull Pos multiply(final double x, final double y, final double z) {
        return new Pos(this.x * x, this.y * y, this.z * z);
    }

    public Pos multiply(final double f) {
        return this.multiply(f, f, f);
    }

    public Pos divide(final double x, final double y, final double z) {
        return new Pos(this.x / x, this.y / y, this.z / z);
    }

    public Pos divide(final double f) {
        return this.divide(f, f, f);
    }

    public Pos sign() {
        return new Pos(Math.signum(this.x), Math.signum(this.y), Math.signum(this.z));
    }

    public Pos floor() {
        return new Pos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
    }

    public Pos round() {
        return new Pos(Math.round(this.x), Math.round(this.y), Math.round(this.z));
    }

    public Pos round(final int places) {
        double factor = Math.pow(10, places);
        return new Pos(Math.round(this.x * factor) / factor, Math.round(this.y * factor) / factor, Math.round(this.z * factor) / factor);
    }

    public static Pos fromRotation(final Rotation rot) {
        return fromRotation(rot.getPitch(), rot.getYaw());
    }

    public static Pos fromRotation(final float pitch, final float yaw) {
        double f = Math.cos(-yaw * 0.017453292 - Math.PI);
        double f1 = Math.sin(-yaw * 0.017453292 - Math.PI);
        double f2 = -Math.cos(-pitch * 0.017453292);
        double f3 = Math.sin(-pitch * 0.017453292);
        return new Pos(f1*f2, f3, f*f2).normalize();
    }

    public @NonNull Pos normalize() {
        double len = this.getLength();
        return new Pos(this.x / len, this.y / len, this.z / len);
    }

    public double getLength() {
        return Math.sqrt(getLengthSquared());
    }

    public double getLengthSquared() {
        return this.x * this.x + this.y*this.y + this.z*this.z;
    }

    public Pos above() {
        return this.add(0, 1, 0);
    }

    @Override
    public boolean equals(@NonNull Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos pos)) return false;
        return x == pos.x && y == pos.y && z == pos.z;
    }

    public Pos shift(Direction dir, double amount) {
        return switch (dir) {
            case UP -> this.add(0, amount, 0);
            case DOWN -> this.add(0, -amount, 0);
            case WEST -> this.add(-amount, 0, 0);
            case SOUTH -> this.add(0, 0, amount);
            case NORTH -> this.add(0, 0, -amount);
            case EAST -> this.add(amount, 0, 0);
            case null -> this;
        };
    }

    public JsonPrimitive getAsJsonPrimitive() {
        return new JsonPrimitive(this.x + " " + this.y + " " + this.z);
    }

    public static Pos fromJsonPrimitive(JsonPrimitive primitive) {
        String[] parts = primitive.getAsString().trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Pos format: \"" + primitive.getAsString() + "\"");
        }

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        return new Pos(x, y, z);
    }

    public AABB getAABB() {
        if (mc.level == null) return Shapes.block().bounds();
        BlockPos bp = this.asBlockPos();
        BlockState state = mc.level.getBlockState(bp);
        VoxelShape shape = state.getShape(mc.level, bp);
        if (shape.isEmpty()) return Shapes.block().bounds().move(bp);
        return shape.bounds().move(bp);
    }
}
