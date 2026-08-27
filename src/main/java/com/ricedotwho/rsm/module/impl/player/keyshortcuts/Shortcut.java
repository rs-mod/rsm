package com.ricedotwho.rsm.module.impl.player.keyshortcuts;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.ui.old.TextInput;
import com.ricedotwho.rsm.ui.old.api.FatalityColors;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

@Getter
@Setter
public class Shortcut implements Accessor {
    private static final float WIDTH = 818;
    private static final float HEIGHT = 30;
    private static final float GAP = 5;
    private static final float SUB = 72.5f;
    private static final float H = 20;
    private static final float INPUT_WIDTH = 500;
    private static Shortcut selected = null;

    private boolean enabled;
    private final Keybind keybind = new Keybind(InputConstants.UNKNOWN, false, this::run);
    private String command;
    private boolean waitingKey = false;
    private final TextInput input = new TextInput("", 12, false, 256);

    public Shortcut(){
        this(true, false, "", InputConstants.UNKNOWN);
    }

    public Shortcut(boolean enabled, boolean allowGui, String command, InputConstants.Key key) {
        this.enabled = enabled;
        this.command = command;
        this.keybind.setKey(key);
        this.keybind.setAllowGui(allowGui);
        this.input.setValue(command);
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

    public boolean click(double mouseX, double mouseY, int button) {
        float keyX = INPUT_WIDTH + GAP * 2;
        float guiX = keyX + SUB + GAP;
        float enabledX = guiX + SUB + GAP;
        float deleteX = enabledX + SUB + GAP;

        if (NVGUtils.isHovering(mouseX, mouseY, 5, 5, INPUT_WIDTH, H)) {
            selected = this;
            input.setWriting(true);
            input.click((float) (mouseX - 10f), button);
        } else {
            input.setWriting(false);
        }

        if (NVGUtils.isHovering(mouseX, mouseY, keyX, 5, SUB, H)) {
            if (selected == this && waitingKey) {
                InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
                this.waitingKey = false;
                selected = null;
                keybind.setKey(key);
            } else {
                selected = this;
                waitingKey = true;
            }
        } else {
            waitingKey = false;
        }

        if (NVGUtils.isHovering(mouseX, mouseY, guiX, 5, SUB, H)) {
            keybind.setAllowGui(!keybind.isAllowGui());
        }

        if (NVGUtils.isHovering(mouseX, mouseY, enabledX, 5, SUB, H)) {
            setEnabled(!this.enabled);
        }

        if (NVGUtils.isHovering(mouseX, mouseY, deleteX, 5, SUB, H)) {
            this.keybind.unregister();
            KeyShortcuts.getData().getValue().remove(this);
            return true;
        }
        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (input.isWriting()) {
            input.charTyped(typedChar);
            this.command = input.getValue();
        }
        return false;
    }

    public boolean keyTyped(KeyEvent event) {
        if (input.isWriting()) {
            int key = event.key();
            if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                input.setWriting(false);
                selected = null;
                return true;
            }
            input.keyTyped(event);
            this.command = input.getValue();
        }

        if (!this.waitingKey || selected != this) return false;
        InputConstants.Key key = InputConstants.getKey(event);

        this.waitingKey = false;
        selected = null;
        if (key.getValue() == 0 || key.getValue() == InputConstants.KEY_ESCAPE) {
            keybind.setKey(InputConstants.UNKNOWN);
            selected = null;
            return true;
        }
        keybind.setKey(key);
        return false;
    }

    public void render(GuiGraphicsExtractor gfx, float x, float y, double mouseX, double mouseY) {
        NVGUtils.drawOutlineRect(x, y, WIDTH, HEIGHT, 1f, FatalityColors.GROUP_OUTLINE);
        NVGUtils.drawRect(x, y, WIDTH, HEIGHT, FatalityColors.GROUP_FILL);

        // me when im in top 10 worst code competition and my opponent is ricedotwho

        boolean hoveringInput = NVGUtils.isHovering(mouseX, mouseY, x + 5, y + 5, INPUT_WIDTH, H);

        // todo: fade
        Color textBoxColor;
        if (input.isWriting()) {
            textBoxColor = FatalityColors.WRITING_TEXT;
        } else if (hoveringInput) {
            textBoxColor = FatalityColors.HOVERING_TEXT;
        } else {
            textBoxColor = FatalityColors.INPUT_TEXT;
        }

        NVGUtils.drawRect(x + GAP, y + 5, INPUT_WIDTH, H, textBoxColor);
        input.render(x + GAP + 5, y + HEIGHT / 2 - 4);

        // keybind
        float keyX = x + INPUT_WIDTH + GAP * 2;
        Color keyColor;
        if (waitingKey) {
            keyColor = FatalityColors.WRITING_TEXT; // ts ts ts...
        } else if (NVGUtils.isHovering(mouseX, mouseY, keyX, y + 5, SUB, H)) {
            keyColor = FatalityColors.HOVERING_TEXT;
        } else {
            keyColor = FatalityColors.INPUT_TEXT;
        }

        Font font = NVGUtils.getFont(NVGUtils.JOSEFIN);
        NVGUtils.drawRect(keyX, y + 5, SUB, H, 2f, keyColor);
        String keyText = this.waitingKey ? "..." : this.getKeybind().getDisplay();
        NVGUtils.drawText(keyText, keyX + (SUB - NVGUtils.getTextWidth(keyText, 12, font)) / 2, y + 5 + NVGUtils.getTextHeight(12, font) / 2, 12, FatalityColors.TEXT, font);

        // alow gui button
        float guiX = keyX + SUB + GAP;
        boolean allowGuiHovered = NVGUtils.isHovering(mouseX, mouseY, guiX, y + 5, SUB, H);
        int guiColor;
        if (this.keybind.isAllowGui()) {
            guiColor = allowGuiHovered ? FatalityColors.SELECTED.darker() : FatalityColors.SELECTED.getARGB();
        } else {
            guiColor = allowGuiHovered ? FatalityColors.GROUP_OUTLINE.brighter() : FatalityColors.GROUP_OUTLINE.getARGB();
        }
        NVGUtils.drawRect(guiX, y + 5, SUB, H, 5f, guiColor);
        NVGUtils.drawText("Allow Gui", guiX + (SUB - NVGUtils.getTextWidth("Allow Gui", 12, font)) / 2, y + 5 + NVGUtils.getTextHeight(12, font) / 2, 12, FatalityColors.TEXT, font);

        // toggle
        float enabledX = guiX + SUB + GAP;
        boolean enabledHovered = NVGUtils.isHovering(mouseX, mouseY, enabledX, y + 5, SUB, H);
        int enabledColor;
        String text;
        if (this.enabled) {
            text = "On";
            enabledColor = enabledHovered ? FatalityColors.SELECTED.darker() : FatalityColors.SELECTED.getARGB();
        } else {
            text = "Off";
            enabledColor = enabledHovered ? FatalityColors.GROUP_OUTLINE.brighter() : FatalityColors.GROUP_OUTLINE.getARGB();
        }
        NVGUtils.drawRect(enabledX, y + 5, SUB, H, 5f, enabledColor);
        NVGUtils.drawText(text, enabledX + (SUB - NVGUtils.getTextWidth(text, 12, font)) / 2, y + 5 + NVGUtils.getTextHeight(12, font) / 2, 12, FatalityColors.TEXT, font);

        // delete
        float deleteX = enabledX + SUB + GAP;
        boolean deleteHovered = NVGUtils.isHovering(mouseX, mouseY, deleteX, y + 5, SUB, H);
        NVGUtils.drawRect(deleteX, y + 5, SUB, H, 5f, deleteHovered ? FatalityColors.SELECTED.brighter() : FatalityColors.SELECTED.getARGB());
        NVGUtils.drawText("Delete", deleteX + (SUB - NVGUtils.getTextWidth("Delete", 12, font)) / 2, y + 5 + NVGUtils.getTextHeight(12, font) / 2, 12, FatalityColors.TEXT, font);
    }

    private boolean run() {
        if (mc.getConnection() == null) return false;
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