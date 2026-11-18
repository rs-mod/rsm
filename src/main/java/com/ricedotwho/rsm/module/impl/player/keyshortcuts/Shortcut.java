package com.ricedotwho.rsm.module.impl.player.keyshortcuts;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Keybind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Shortcut implements Accessor {
    private boolean enabled;
    private final Keybind keybind = new Keybind(InputConstants.UNKNOWN, false, this::run);
    private String command;
    private boolean waitingKey = false;

    public Shortcut(){
        this(false, false, "", InputConstants.UNKNOWN);
    }

    public Shortcut(boolean enabled, boolean allowGui, String command, InputConstants.Key key) {
        this.enabled = enabled;
        this.command = command;
        this.keybind.setKey(key);
        this.keybind.setAllowGui(allowGui);
    }

    public Shortcut(JsonObject obj) {
        this(
                obj.get("enabled").getAsBoolean(),
                obj.get("gui").getAsBoolean(),
                obj.get("command").getAsString(),
                InputConstants.getKey(obj.has("key") ? obj.get("key").getAsString() : "key.keyboard.unknown")
        );
    }

    public void setEnabled(boolean bl){
        if (!bl) {
            this.enabled = false;
            this.keybind.unregister();
        } else {
            if (!this.enabled) this.keybind.register();
            this.enabled = true;
        }
    }

    public void delete() {
        this.keybind.unregister();
        KeyShortcuts.getData().getValue().remove(this);
    }

    public boolean run() {
        if (mc.getConnection() == null || this.command.isBlank()) return false;
        if (this.command.startsWith(UniversalSettings.getCommandPrefix().getValue())) {
            mc.getConnection().sendChat(this.command);
        } else {
            mc.getConnection().sendCommand(this.command);
        }
        return false;
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", this.enabled);
        obj.addProperty("gui", this.keybind.isAllowGui());
        obj.addProperty("key", this.keybind.getKey().getName());
        obj.addProperty("command", this.command);
        return obj;
    }
}
