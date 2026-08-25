package com.ricedotwho.rsm.type;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.managers.KeybindManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

public class Keybind {
    @Getter
    @Setter
    private boolean allowGui;
    @Setter
    @Getter
    protected InputConstants.Key key;
    @Setter
    protected transient BooleanSupplier runnable;
    @Getter
    private final boolean cancel;

    public Keybind(Keybind keybind) {
        this.key = keybind.key;
        this.allowGui = keybind.allowGui;
        this.runnable = keybind.runnable;
        this.cancel = keybind.cancel;
    }

    public Keybind(InputConstants.Key key, boolean allowGui, boolean cancel, BooleanSupplier runnable) {
        this.key = key;
        this.allowGui = allowGui;
        this.runnable = runnable;
        this.cancel = cancel;
    }

    public Keybind(InputConstants.Key key, boolean allowGui, BooleanSupplier runnable) {
        this.key = key;
        this.allowGui = allowGui;
        this.runnable = runnable;
        this.cancel = false;
    }

    public Keybind(InputConstants.Key key, BooleanSupplier runnable) {
        this.key = key;
        this.allowGui = false;
        this.runnable = runnable;
        this.cancel = false;
    }

    public Keybind(int key, boolean allowGui, boolean mouse, boolean cancel, BooleanSupplier runnable) {
        if (mouse) {
            this.key = InputConstants.Type.MOUSE.getOrCreate(key);
        } else {
            this.key = InputConstants.Type.KEYSYM.getOrCreate(key);
        }
        this.allowGui = allowGui;
        this.runnable = runnable;
        this.cancel = cancel;
    }

    public Keybind(int key, boolean mouse, BooleanSupplier runnable) {
        if (mouse) {
            this.key = InputConstants.Type.MOUSE.getOrCreate(key);
        } else {
            this.key = InputConstants.Type.KEYSYM.getOrCreate(key);

        }
        this.allowGui = false;
        this.runnable = runnable;
        this.cancel = false;
    }

    /// This probably won't return true on InputEvent!
    public boolean isActive() {
        if (this.key == null || this.key == InputConstants.UNKNOWN) return false;

        long windowHandle = Minecraft.getInstance().getWindow().handle();

        if (this.key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(windowHandle, this.key.getValue()) == GLFW.GLFW_PRESS;
        } else {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), this.key.getValue());
        }
    }

    public boolean run() {
        if (runnable == null) return false;

        return runnable.getAsBoolean();
    }

    public String getDisplay() {
        if (key == null) return "NONE"; // this shouldn't be null but wtv
        return this.key.getDisplayName().getString();
    }

    public void register() {
        KeybindManager.register(this);
    }

    public void unregister() {
        KeybindManager.deregister(this);
    }

    @Override
    public String toString() {
        return "Keybind{"
                + "keyBind=" +  this.key
                + ",allowGui=" +  this.allowGui
                + ",runnable=" +  this.runnable + "}";
    }
}