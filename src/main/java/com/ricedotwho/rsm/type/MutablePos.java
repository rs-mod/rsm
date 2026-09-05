package com.ricedotwho.rsm.type;

import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("unused")
public class MutablePos implements Accessor {
    @Expose
    public double x;
    @Expose
    public double y;
    @Expose
    public double z;

    public MutablePos() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public MutablePos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutablePos(BlockPos pos) {
        if(pos == null) return;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public MutablePos(Vec3 vec) {
        if(vec == null) return;
        this.x = vec.x();
        this.y = vec.y();
        this.z = vec.z();
    }

    public MutablePos(MutablePos other) {
        if(other == null) return;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public double x() {
        return this.x;
    }
    public double y() {
        return this.y;
    }
    public double z() {
        return this.z;
    }

    public void x(double x) {
        this.x = x;
    }
    public void y(double y) {
        this.y = y;
    }
    public void z(double z) {
        this.z = z;
    }

    public MutablePos copy() {
        return new MutablePos(this);
    }

    @Override
    public String toString() {
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

    public double distanceTo(Vec3 pos) {
        return Math.sqrt(squaredDistanceTo(pos));
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

    public Pos asPos() { return new Pos(this.x, this.y, this.z); }

    public Vec3 asVec3() {
        return new Vec3(this.x, this.y, this.z);
    }

    public MutablePos add(double x, double y, double z) {
        return new MutablePos(this.x + x, this.y + y, this.z + z);
    }

    public MutablePos add(Vec3 pos) {
        return new MutablePos(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    public MutablePos subtract(Vec3 pos) {
        return new MutablePos(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    public MutablePos subtract(double x, double y, double z) {
        return new MutablePos(this.x - x, this.y - y, this.z - z);
    }

    public MutablePos multiply(double x, double y, double z) {
        return new MutablePos(this.x * x, this.y * y, this.z * z);
    }

    public MutablePos multiply(double f) {
        return this.multiply(f, f, f);
    }

    public MutablePos divide(double x, double y, double z) {
        return new MutablePos(this.x / x, this.y / y, this.z / z);
    }

    public MutablePos divide(double f) {
        return this.divide(f, f, f);
    }

    public MutablePos sign() {
        return new MutablePos(Math.signum(this.x), Math.signum(this.y), Math.signum(this.z));
    }

    public MutablePos selfAdd(Vec3 pos) {
        return this.selfAdd(pos.x(), pos.y(), pos.z());
    }

    public MutablePos selfAdd(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3 other) {
        this.x = other.x();
        this.y = other.y();
        this.z = other.z();
    }

    @Deprecated
    public MutablePos selfFloor() { //todo: remove
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        return this;
    }

    public MutablePos floor() {
        return new MutablePos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
    }

    public MutablePos round() {
        return new MutablePos(Math.round(this.x), Math.round(this.y), Math.round(this.z));
    }

    public MutablePos round(int places) {
        double factor = Math.pow(10, places);
        return new MutablePos(Math.round(this.x * factor) / factor, Math.round(this.y * factor) / factor, Math.round(this.z * factor) / factor);
    }

    public static MutablePos fromRotation(Rotation rot) {
        return fromRotation(rot.getPitch(), rot.getYaw());
    }

    public static MutablePos fromRotation(float pitch, float yaw) {
        double f = Math.cos(-yaw * 0.017453292 - Math.PI);
        double f1 = Math.sin(-yaw * 0.017453292 - Math.PI);
        double f2 = -Math.cos(-pitch * 0.017453292);
        double f3 = Math.sin(-pitch * 0.017453292);
        return new MutablePos(f1*f2, f3, f*f2).normalize();
    }

    public MutablePos normalize() {
        double len = this.getLength();
        this.x = this.x / len;
        this.y = this.y / len;
        this.z = this.z / len;
        return this;
    }

    public double getLength() {
        return Math.sqrt(getLengthSquared());
    }

    public double getLengthSquared() {
        return this.x * this.x + this.y*this.y + this.z*this.z;
    }

    public MutablePos above() {
        return this.add(0, 1, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutablePos pos)) return false;
        return x == pos.x && y == pos.y && z == pos.z;
    }

    @Override
    public int hashCode() {
        double result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return (int) result;
    }

    public MutablePos shiftSelf(Direction dir, double amount) {
        return switch (dir) {
            case UP -> this.selfAdd(0, amount, 0);
            case DOWN -> this.selfAdd(0, -amount, 0);
            case WEST -> this.selfAdd(-amount, 0, 0);
            case SOUTH -> this.selfAdd(0, 0, amount);
            case NORTH -> this.selfAdd(0, 0, -amount);
            case EAST -> this.selfAdd(amount, 0, 0);
            case null -> this;
        };
    }

    public double dot(final MutablePos other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public MutablePos shift(Direction dir, double amount) {
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

    public static MutablePos fromJsonPrimitive(JsonPrimitive primitive) {
        String[] parts = primitive.getAsString().trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid MutablePos format: \"" + primitive.getAsString() + "\"");
        }

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        return new MutablePos(x, y, z);
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