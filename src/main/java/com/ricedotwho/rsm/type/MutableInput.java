package com.ricedotwho.rsm.type;

import lombok.Getter;
import net.minecraft.world.entity.player.Input;

public class MutableInput {
    private static final byte FLAG_FORWARD = 0x01;
    private static final byte FLAG_BACKWARD = 0x02;
    private static final byte FLAG_LEFT = 0x04;
    private static final byte FLAG_RIGHT = 0x08;
    private static final byte FLAG_JUMP = 0x10;
    private static final byte FLAG_SHIFT = 0x20;
    private static final byte FLAG_SPRINT = 0x40;
    @Getter
    private boolean modified = false;
    private byte inputs;

    public MutableInput() {
        this((byte) 0);
    }

    public MutableInput(Input input) {
        this(fromInput(input));
    }

    public MutableInput(byte b) {
        this.inputs = b;
    }

    private static byte fromInput(Input input) {
        byte ret = 0;
        if (input.forward()) ret |= FLAG_FORWARD;
        if (input.backward()) ret |= FLAG_BACKWARD;
        if (input.left()) ret |= FLAG_LEFT;
        if (input.right()) ret |= FLAG_RIGHT;
        if (input.jump()) ret |= FLAG_JUMP;
        if (input.shift()) ret |= FLAG_SHIFT;
        if (input.sprint()) ret |= FLAG_SPRINT;
        return ret;
    }

    public void apply(Input input) {
        inputs = 0;
        if (input.forward()) inputs |= FLAG_FORWARD;
        if (input.backward()) inputs |= FLAG_BACKWARD;
        if (input.left()) inputs |= FLAG_LEFT;
        if (input.right()) inputs |= FLAG_RIGHT;
        if (input.jump()) inputs |= FLAG_JUMP;
        if (input.shift()) inputs |= FLAG_SHIFT;
        if (input.sprint()) inputs |= FLAG_SPRINT;
    }

    public Input toInput() {
        return new Input(forward(), backward(), left(), right(), jump(), shift(), sprint());
    }

    public boolean forward()  { return (inputs & FLAG_FORWARD)  != 0; }
    public boolean backward() { return (inputs & FLAG_BACKWARD) != 0; }
    public boolean left()     { return (inputs & FLAG_LEFT)     != 0; }
    public boolean right()    { return (inputs & FLAG_RIGHT)    != 0; }
    public boolean jump()     { return (inputs & FLAG_JUMP)     != 0; }
    public boolean shift()    { return (inputs & FLAG_SHIFT)    != 0; }
    public boolean sprint()   { return (inputs & FLAG_SPRINT)   != 0; }

    public void forward(boolean v) {
        if (v) inputs |= FLAG_FORWARD;
        else inputs &= ~FLAG_FORWARD;
        modified = true;
    }

    public void backward(boolean v) {
        if (v) inputs |= FLAG_BACKWARD;
        else inputs &= ~FLAG_BACKWARD;
        modified = true;
    }

    public void left(boolean v) {
        if (v) inputs |= FLAG_LEFT;
        else inputs &= ~FLAG_LEFT;
        modified = true;
    }

    public void right(boolean v) {
        if (v) inputs |= FLAG_RIGHT;
        else inputs &= ~FLAG_RIGHT;
        modified = true;
    }

    public void jump(boolean v) {
        if (v) inputs |= FLAG_JUMP;
        else inputs &= ~FLAG_JUMP;
        modified = true;
    }

    public void shift(boolean v)    {
        if (v) inputs |= FLAG_SHIFT;
        else inputs &= ~FLAG_SHIFT;
        modified = true;
    }

    public void sprint(boolean v)   {
        if (v) inputs |= FLAG_SPRINT;
        else inputs &= ~FLAG_SPRINT;
        modified = true;
    }
}

