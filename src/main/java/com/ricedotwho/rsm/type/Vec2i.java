package com.ricedotwho.rsm.type;

public record Vec2i(int x, int y) {
    public Vec2i() {
        this(0, 0);
    }

    public Vec2i add(Vec2i other) {
        return add(other.x(), other.y());
    }

    public Vec2i add(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    public Vec2i add(int i) {
        return add(i, i);
    }

    public Vec2i multiply(Vec2i other) {
        return multiply(other.x(), other.y());
    }

    public Vec2i multiply(int x, int y) {
        return new Vec2i(this.x * x, this.y * y);
    }

    public Vec2i multiply(int i) {
        return multiply(i, i);
    }

    public Vec2i div(Vec2i other) {
        return multiply(other.x(), other.y());
    }

    public Vec2i div(int x, int y) {
        return new Vec2i(this.x / x, this.y / y);
    }

    public Vec2i div(int i) {
        return multiply(i, i);
    }

    @Override
    public int hashCode() {
        return this.y() * 31 + this.x();
    }
}
