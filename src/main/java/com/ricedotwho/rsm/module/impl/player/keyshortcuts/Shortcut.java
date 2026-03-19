package com.ricedotwho.rsm.module.impl.player.keyshortcuts;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
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
    private boolean writing = false, waitingKey = false;
    private final TextInput input = new TextInput("", 12, false, 256);

    public Shortcut(){
        this(true, false, "", InputConstants.UNKNOWN);
    }

    public Shortcut(boolean enabled, boolean allowGui, String command, InputConstants.Key key) {
        this.enabled = enabled;
        this.command = command;
        this.keybind.setKeyBind(key);
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
            this.writing = true;
            input.click((float) (mouseX - 10f), button);
        } else {
            this.writing = false;
        }

        if (NVGUtils.isHovering(mouseX, mouseY, keyX, 5, SUB, H)) {
            selected = this;
            waitingKey = true;
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
        if (writing) {
            input.charTyped(typedChar);
            this.command = input.getValue();
        }
        return false;
    }

    public boolean keyTyped(KeyEvent event) {
        if (writing) {
            int key = event.key();
            if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                writing = false;
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
            keybind.setKeyBind(InputConstants.UNKNOWN);
            selected = null;
            return true;
        }
        keybind.setKeyBind(key);
        return false;
    }

    public void render(GuiGraphics gfx, float x, float y, double mouseX, double mouseY) {
        NVGUtils.drawOutlineRect(x, y, WIDTH, HEIGHT, 1f, FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawRect(x, y, WIDTH, HEIGHT, FatalityColours.GROUP_FILL);

        // me when im in top 10 worst code competition and my opponent is ricedotwho

        boolean hoveringInput = NVGUtils.isHovering(mouseX, mouseY, x + 5, y + 5, INPUT_WIDTH, H);

        // todo: fade
        Colour textBoxColor;
        if (writing) {
            textBoxColor = new Colour(60, 60, 60);
        } else if (hoveringInput) {
            textBoxColor = new Colour(50, 50, 50);
        } else {
            textBoxColor = new Colour(40, 40, 40);
        }

        NVGUtils.drawRect(x + GAP, y + 5, INPUT_WIDTH, H, textBoxColor);
        input.render(x + GAP + 5, y + HEIGHT / 2 - 4, writing);

        // keybind
        float keyX = x + INPUT_WIDTH + GAP * 2;
        Colour keyColor;
        if (waitingKey) {
            keyColor = new Colour(60, 60, 60); // ts ts ts...
        } else if (NVGUtils.isHovering(mouseX, mouseY, keyX, y + 5, SUB, H)) {
            keyColor = new Colour(50, 50, 50);
        } else {
            keyColor = new Colour(40, 40, 40);
        }

        NVGUtils.drawRect(keyX, y + 5, SUB, H, 2f, keyColor);
        String keyText = this.waitingKey ? "..." : this.getKeybind().getDisplay();
        NVGUtils.drawText(keyText, keyX + (SUB - NVGUtils.getTextWidth(keyText, 12, NVGUtils.JOSEFIN)) / 2, y + 5 + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        // alow gui button
        float guiX = keyX + SUB + GAP;
        boolean allowGuiHovered = NVGUtils.isHovering(mouseX, mouseY, guiX, y + 5, SUB, H);
        Colour guiColour;
        if (this.keybind.isAllowGui()) {
            guiColour = allowGuiHovered ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED;
        } else {
            guiColour = allowGuiHovered ? FatalityColours.GROUP_OUTLINE.brighter() : FatalityColours.GROUP_OUTLINE;
        }
        NVGUtils.drawRect(guiX, y + 5, SUB, H, 5f, guiColour);
        NVGUtils.drawText("Allow Gui", guiX + (SUB - NVGUtils.getTextWidth("Allow Gui", 12, NVGUtils.JOSEFIN)) / 2, y + 5 + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        // toggle
        float enabledX = guiX + SUB + GAP;
        boolean enabledHovered = NVGUtils.isHovering(mouseX, mouseY, enabledX, y + 5, SUB, H);
        Colour enabledColour;
        String text;
        if (this.enabled) {
            text = "On";
            enabledColour = enabledHovered ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED;
        } else {
            text = "Off";
            enabledColour = enabledHovered ? FatalityColours.GROUP_OUTLINE.brighter() : FatalityColours.GROUP_OUTLINE;
        }
        NVGUtils.drawRect(enabledX, y + 5, SUB, H, 5f, enabledColour);
        NVGUtils.drawText(text, enabledX + (SUB - NVGUtils.getTextWidth(text, 12, NVGUtils.JOSEFIN)) / 2, y + 5 + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        // delete
        float deleteX = enabledX + SUB + GAP;
        boolean deleteHovered = NVGUtils.isHovering(mouseX, mouseY, deleteX, y + 5, SUB, H);
        NVGUtils.drawRect(deleteX, y + 5, SUB, H, 5f, deleteHovered ? FatalityColours.SELECTED.brighter() : FatalityColours.SELECTED);
        NVGUtils.drawText("Delete", deleteX + (SUB - NVGUtils.getTextWidth("Delete", 12, NVGUtils.JOSEFIN)) / 2, y + 5 + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);
    }

    private void run() {
        if (mc.getConnection() == null) return;
        mc.getConnection().sendCommand(this.command);
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", this.enabled);
        obj.addProperty("gui", this.keybind.isAllowGui());
        obj.addProperty("key", this.keybind.getKeyBind().getName());
        obj.addProperty("command", this.command);
        return obj;
    }
}
