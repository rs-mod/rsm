package com.ricedotwho.rsm.module.impl.player.chat;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.type.Accessor;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class HiddenMessage implements Accessor {
    private boolean enabled;
    private String message;
    private Pattern pattern;

    public HiddenMessage(){
        this(false, "");
    }

    public HiddenMessage(boolean enabled, String regex) {
        this.enabled = enabled;
        this.message = regex;
        this.pattern = Pattern.compile(this.message);
    }

    public HiddenMessage(JsonObject obj) {
        this(obj.get("enabled").getAsBoolean(), obj.get("message").getAsString());
    }

    public void setMessage(String message) {
        this.message = message;
        this.pattern = Pattern.compile(this.message);
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", this.enabled);
        obj.addProperty("message", this.message);
        return obj;
    }

    public boolean check(String test) {
        return this.enabled && this.pattern != null && this.pattern.matcher(test).find();
    }
}
