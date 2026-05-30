package com.ricedotwho.rsm.module.impl.player.chat;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.impl.player.Chat;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

@Getter
@Setter
public class HiddenMessage implements Accessor {
    private static final float WIDTH = 818;
    private static final float HEIGHT = 30;
    private static final float GAP = 5;
    private static final float SUB = 72.5f;
    private static final float H = 20;
    private static final float INPUT_WIDTH = 645;

    private boolean enabled;
    private String message;
    private Pattern pattern;
    private boolean writing = false;
    private final TextInput input = new TextInput("", 12, false, 256);

    public HiddenMessage(){
        this(false, "");
    }

    public HiddenMessage(boolean enabled, String regex) {
        this.enabled = enabled;
        this.message = regex;
        this.pattern = Pattern.compile(this.message);
        this.input.setValue(regex);
    }

    public HiddenMessage(JsonObject obj) {
        this(obj.get("enabled").getAsBoolean(), obj.get("message").getAsString());
    }

    public boolean click(double mouseX, double mouseY, int button) {
        float enabledX = INPUT_WIDTH + GAP * 2;
        float deleteX = enabledX + SUB + GAP;

        if (NVGUtils.isHovering(mouseX, mouseY, 5, 5, INPUT_WIDTH, H)) {
            this.writing = true;
            input.click((float) (mouseX - 10f), button);
        } else {
            this.writing = false;
        }

        if (NVGUtils.isHovering(mouseX, mouseY, enabledX, 5, SUB, H)) {
            setEnabled(!this.enabled);
        }

        if (NVGUtils.isHovering(mouseX, mouseY, deleteX, 5, SUB, H)) {
            Chat.getHiddenMessages().getValue().remove(this);
            return true;
        }
        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (writing) {
            input.charTyped(typedChar);
            this.message = input.getValue();
            try {
                this.pattern = Pattern.compile(this.message);
            } catch (Exception ignored) {
                this.pattern = null;
            }
        }
        return false;
    }

    public boolean keyTyped(KeyEvent event) {
        if (writing) {
            int key = event.key();
            if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                writing = false;

                try {
                    this.pattern = Pattern.compile(this.message);
                } catch (Exception e) {
                    this.pattern = null;
                    ChatUtils.chat("Invalid regex \"%s\", %s", this.message, e.getMessage());
                    RSM.getLogger().error("Invalid regex", e);
                }

                return true;
            }
            input.keyTyped(event);
            this.message = input.getValue();
            try {
                this.pattern = Pattern.compile(this.message);
            } catch (Exception ignored) {
                this.pattern = null;
            }
        }
        return false;
    }

    public void render(GuiGraphicsExtractor gfx, float x, float y, double mouseX, double mouseY) {
        NVGUtils.drawOutlineRect(x, y, WIDTH, HEIGHT, 1f, FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawRect(x, y, WIDTH, HEIGHT, FatalityColours.GROUP_FILL);

        boolean hoveringInput = NVGUtils.isHovering(mouseX, mouseY, x + 5, y + 5, INPUT_WIDTH, H);

        // todo: fade
        Colour textBoxColor;
        if (writing) {
            textBoxColor = FatalityColours.WRITING_TEXT;
        } else if (hoveringInput) {
            textBoxColor = FatalityColours.HOVERING_TEXT;
        } else {
            textBoxColor = FatalityColours.INPUT_TEXT;
        }

        NVGUtils.drawRect(x + GAP, y + 5, INPUT_WIDTH, H, textBoxColor);
        input.render(x + GAP + 5, y + HEIGHT / 2 - 4, writing);

        // toggle
        float enabledX = x + INPUT_WIDTH + GAP * 2;
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
