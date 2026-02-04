package com.ricedotwho.rsm.data;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.impl.KeybindComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

public class Keybind {
    @Getter
    @Setter
    private boolean pressed;
    @Getter
    private final boolean allowGui;
    @Setter
    @Getter
    protected InputConstants.Key keyBind;
    @Setter
    protected transient Runnable runnable;

    public Keybind(InputConstants.Key key, boolean allowGui, Runnable runnable) {
        this.keyBind = key;
        this.allowGui = allowGui;
        this.runnable = runnable;
    }

    public Keybind(InputConstants.Key key, Runnable runnable) {
        this.keyBind = key;
        this.allowGui = false;
        this.runnable = runnable;
    }

    public Keybind(int key, boolean allowGui, boolean mouse, Runnable runnable) {
        if (mouse) {
            this.keyBind = InputConstants.Type.MOUSE.getOrCreate(key);
        } else {
            this.keyBind = InputConstants.Type.KEYSYM.getOrCreate(key);
        }
        this.allowGui = allowGui;
        this.runnable = runnable;
    }

    public Keybind(int key, boolean mouse, Runnable runnable) {
        if (mouse) {
            this.keyBind = InputConstants.Type.MOUSE.getOrCreate(key);
        } else {
            this.keyBind = InputConstants.Type.KEYSYM.getOrCreate(key);

        }
        this.allowGui = false;
        this.runnable = runnable;
    }

    /// This probably won't return true on InputEvent!
    public boolean isActive() {
        return this.keyBind != null && this.keyBind != InputConstants.UNKNOWN && InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow(),
                this.keyBind.getValue()
        );
    }

    public void run() {
        if (runnable == null) return;
        runnable.run();
    }

    public String getDisplay() {
        if (keyBind == null) return "NONE"; // this shouldn't be null but wtv
        return this.keyBind.getDisplayName().getString();
    }

    public void register() {
        KeybindComponent.register(this);
    }

    public void unregister() {
        KeybindComponent.deregister(this);
    }

    @Override
    public String toString() {
        return "Keybind{"
                + "keyBind=" +  this.keyBind
                + ",allowGui=" +  this.allowGui
                + ",runnable=" +  this.runnable + "}";
    }
}