package com.ricedotwho.rsm.data;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Pos {
    public double x;
    public double y;
    public double z;

    public Pos() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Pos(BlockPos pos) {
        if(pos == null) return;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Pos(Vec3 vec) {
        if(vec == null) return;
        this.x = vec.x();
        this.y = vec.y();
        this.z = vec.z();
    }

    public Pos(Pos other) {
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

    public Pos copy() {
        return new Pos(this);
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

    public Vec3 asVec3() {
        return new Vec3(this.x, this.y, this.z);
    }

    public Pos add(double x, double y, double z) {
        return new Pos(this.x + x, this.y + y, this.z + z);
    }

    public Pos add(Pos pos) {
        return new Pos(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    public Pos subtract(Pos pos) {
        return new Pos(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    public Pos subtract(double x, double y, double z) {
        return new Pos(this.x - x, this.y - y, this.z - z);
    }

    public Pos multiply(double x, double y, double z) {
        return new Pos(this.x * x, this.y * y, this.z * z);
    }

    public Pos multiply(double f) {
        return this.multiply(f, f, f);
    }

    public Pos divide(double x, double y, double z) {
        return new Pos(this.x / x, this.y / y, this.z / z);
    }

    public Pos divide(double f) {
        return this.divide(f, f, f);
    }

    public Pos sign() {
        return new Pos(Math.signum(this.x), Math.signum(this.y), Math.signum(this.z));
    }

    public Pos selfAdd(Pos pos) {
        return this.selfAdd(pos.x(), pos.y(), pos.z());
    }

    public Pos selfAdd(double x, double y, double z) {
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

    @Deprecated
    public Pos selfFloor() { //todo: remove
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        return this;
    }

    public Pos floor() {
        return new Pos(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    public Pos round() {
        return new Pos(Math.round(this.x), Math.round(this.y), Math.round(this.z));
    }

    public static Pos fromRotation(Rotation rot) {
        return fromRotation(rot.getPitch(), rot.getYaw());
    }

    public static Pos fromRotation(float pitch, float yaw) {
        double f = Math.cos(-yaw * 0.017453292 - Math.PI);
        double f1 = Math.sin(-yaw * 0.017453292 - Math.PI);
        double f2 = -Math.cos(-pitch * 0.017453292);
        double f3 = Math.sin(-pitch * 0.017453292);
        return new Pos(f1*f2, f3, f*f2).normalize();
    }

    public Pos normalize() {
        double len = this.getLength();
        this.x = this.x / len;
        this.y = this.y / len;
        this.z = this.z / len;
        return this;
    }

    public double getLength() {
        return Math.sqrt(this.x * this.x + this.y*this.y + this.z*this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos pos)) return false;
        return x == pos.x && y == pos.y && z == pos.z;
    }

    @Override
    public int hashCode() {
        double result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return (int) result;
    }
}
