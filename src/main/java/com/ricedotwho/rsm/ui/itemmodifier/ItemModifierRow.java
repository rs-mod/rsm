package com.ricedotwho.rsm.ui.itemmodifier;

import com.ricedotwho.rsm.module.impl.render.itemmodifier.ItemModifier;
import com.ricedotwho.rsm.module.impl.render.itemmodifier.ItemOverride;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.ColourValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.render.render2d.Gradient;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.ColourValueComponent.*;
import static com.ricedotwho.rsm.ui.itemmodifier.ItemModifierGui.consumeClick;

public class ItemModifierRow {
    private static final float WIDTH = 818f;
    private static final float HEIGHT = 30f;
    private static final float GAP = 5f;
    private static final float BOX_HEIGHT = 20f;
    private static final float UUID_WIDTH = 300f;
    private static final float NAME_WIDTH = 300f;
    private static final float NAME_WIDTH_NO_COLOUR = 365f;
    private static final float COLOUR_WIDTH = 65f;
    private static final float BUTTON_WIDTH = 66f;
    private static final float DELETE_WIDTH = 67f;

    private static ItemModifierRow expandedInstance = null;
    private static ItemModifierRow selected = null;
    private static ItemModifierRow focusedComponent = null;

    @Getter
    private final String uuid;
    private final ItemOverride value;
    private final TextInput nameInput;

    @Getter
    private boolean expanded = false;
    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private final TextInput colourInput;

    public ItemModifierRow(String uuid, ItemOverride value) {
        this.uuid = uuid;
        this.value = value;
        this.nameInput = new TextInput(value.name, 12, false, 128);
        this.colourInput = new TextInput(value.colour == null ? "" : value.colour.getHex(), 12, ColourValueComponent.getAllowed(), 6, false);
    }

    public boolean click(double mouseX, double mouseY, int button) {
        float nameX = GAP + UUID_WIDTH + GAP;
        float colourX;
        float enabledX;
        float nameWidth;
        if (value.colour == null) {
            colourX = 0;
            nameWidth = NAME_WIDTH_NO_COLOUR;
            enabledX = nameX + nameWidth + GAP;
        } else {
            nameWidth = NAME_WIDTH;
            colourX = nameX + nameWidth + GAP;
            enabledX = colourX + COLOUR_WIDTH + GAP;
        }
        float deleteX = enabledX + BUTTON_WIDTH + GAP;

        if (NVGUtils.isHovering(mouseX, mouseY, nameX, GAP, nameWidth, BOX_HEIGHT)) {
            selected = this;
            nameInput.setWriting(true);
            nameInput.click((float) (mouseX - (nameX + 5f)), button);
        } else if (nameInput.isWriting() && selected == this) {
            commitName();
            nameInput.setWriting(false);
            selected = null;
        }

        if (value.colour != null) {

            if (button == 0 && NVGUtils.isHovering(mouseX, mouseY, colourX, GAP, COLOUR_WIDTH, BOX_HEIGHT)) {
                // item modifier row larping as ColourValueComponent
                if (expandedInstance != null && expandedInstance != this) {
                    expandedInstance.expanded = false;
                }
                expanded = !expanded;
                if (expanded) {
                    expandedInstance = this;
                } else {
                    expandedInstance = null;
                }
                consumeClick();
                colourInput.setWriting(false);
                return false;
            }


            if (expanded && button == 0) {
                float boxX = colourX + COLOUR_WIDTH / 2;
                float hueX = boxX + BOX_SIZE + 10;
                float alphaX = hueX + HUE_STRIP_WIDTH + 10;
                float boxY = BOX_HEIGHT;
                float bgwidth = BOX_SIZE + (HUE_STRIP_WIDTH * 2) + 24;
                float y = BASE_HEIGHT + 4;

                float relX = (float) (mouseX - (boxX + 2));
                float relY = (float) (mouseY - y);

                float stringX = boxX + (bgwidth - 50) / 2f;
                float stringY = boxY + 106;

                if (NVGUtils.isHovering(mouseX, mouseY, boxX - 4, boxY - 4, bgwidth + 4, 128)) {
                    consumeClick();
                }

                boolean hoveringInput = NVGUtils.isHovering(mouseX, mouseY, stringX, stringY, 65, 18);

                if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, BOX_SIZE, BOX_SIZE)) {
                    updateSB(relX, relY, value.colour);
                    draggingSB = true;
                } else if (NVGUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
                    updateHue(relY, value.colour);
                    draggingHue = true;
                } else if (NVGUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
                    updateAlpha(relY, value.colour);
                    draggingAlpha = true;
                } else if (!hoveringInput) {
                    // clicking outside closes the picker
                    expanded = false;
                    colourInput.setWriting(false);
                    if (focusedComponent == this) {
                        focusedComponent = null;
                    }
                    if (expandedInstance == this) {
                        expandedInstance = null;
                    }
                }

                if (hoveringInput) {
                    if (focusedComponent != null) focusedComponent.colourInput.setWriting(false);
                    focusedComponent = this;
                    colourInput.setWriting(true);
                    colourInput.click((float) (mouseX - stringX), button);
                } else {
                    if (colourInput.isWriting() && focusedComponent == this) {
                        colourInput.setWriting(false);
                        focusedComponent = null;
                    }
                }
            }
        }

        if (button == 0 && NVGUtils.isHovering(mouseX, mouseY, enabledX, GAP, BUTTON_WIDTH, BOX_HEIGHT)) {
            value.toggle();
            ItemModifier.save();
        }

        if (button == 0 && NVGUtils.isHovering(mouseX, mouseY, deleteX, GAP, DELETE_WIDTH, BOX_HEIGHT)) {
            ItemModifier.remove(uuid);
            return true;
        }

        return false;
    }

    // release like steve from minecart movie with ther walter bucket
    public void release() {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    public boolean charTyped(char typedChar) {
        boolean ret = false;
        if (nameInput.isWriting() && nameInput.charTyped(typedChar)) ret = true;
        else if (colourInput.isWriting() && colourInput.charTyped(typedChar)) ret = true;
        return ret;
    }

    public boolean keyTyped(KeyEvent event) {
        int key = event.key();
        if ((nameInput.isWriting() || colourInput.isWriting()) && key == GLFW.GLFW_KEY_ESCAPE) {
            if (colourInput.isWriting()) {
                colourInput.setValue(value.colour == null ? "" : value.colour.getHex());
            } else {
                nameInput.setValue(value.name);
            }
            nameInput.setWriting(false);
            selected = null;
            colourInput.setWriting(false);
            return true;
        }

        if ((nameInput.isWriting() || colourInput.isWriting()) && key == GLFW.GLFW_KEY_ENTER) {
            if (colourInput.isWriting() && value.colour != null) {
                value.colour.setColorFromHex(colourInput.getValue());
            } else if (nameInput.isWriting()) {
                commitName();
            }
            colourInput.setWriting(false);
            nameInput.setWriting(false);
            selected = null;
            return true;
        }

        boolean ret = false;
        if (nameInput.isWriting() && nameInput.keyTyped(event)) ret = true;
        else if (colourInput.isWriting() && colourInput.keyTyped(event)) ret = true;
        return ret;
    }

    public void render(GuiGraphics gfx, float x, float y, double mouseX, double mouseY) {
        NVGUtils.drawOutlineRect(x, y, WIDTH, HEIGHT, 1f, FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawRect(x, y, WIDTH, HEIGHT, FatalityColours.GROUP_FILL);

        float uuidX = x + GAP;
        float nameX = uuidX + UUID_WIDTH + GAP;

        float colourX;
        float enabledX;
        float nameWidth;
        if (value.colour == null) {
            colourX = 0;
            nameWidth = NAME_WIDTH_NO_COLOUR;
            enabledX = nameX + nameWidth + GAP;
        } else {
            nameWidth = NAME_WIDTH;
            colourX = nameX + nameWidth + GAP;
            enabledX = colourX + COLOUR_WIDTH + GAP;
        }
        float deleteX = enabledX + BUTTON_WIDTH + GAP;

        NVGUtils.drawRect(uuidX, y + GAP, UUID_WIDTH, BOX_HEIGHT, FatalityColours.INPUT_TEXT);
        NVGUtils.drawText(uuid, uuidX + 5f, y + HEIGHT / 2f - 2f, 11, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        Colour nameColour = inputColour(nameInput.isWriting(), NVGUtils.isHovering(mouseX, mouseY, nameX, y + GAP, nameWidth, BOX_HEIGHT));
        NVGUtils.drawRect(nameX, y + GAP, nameWidth, BOX_HEIGHT, nameColour);
        nameInput.render(nameX + 5f, y + HEIGHT / 2f - 4f);

        boolean enabledHovered = NVGUtils.isHovering(mouseX, mouseY, enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT);
        Colour enabledColour = value.enabled
                ? (enabledHovered ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED)
                : (enabledHovered ? FatalityColours.GROUP_OUTLINE.brighter() : FatalityColours.GROUP_OUTLINE);

        NVGUtils.drawRect(enabledX, y + GAP, BUTTON_WIDTH, BOX_HEIGHT, 5f, enabledColour);
        String enabledText = value.enabled ? "On" : "Off";
        NVGUtils.drawText(enabledText, enabledX + (BUTTON_WIDTH - NVGUtils.getTextWidth(enabledText, 12, NVGUtils.JOSEFIN)) / 2f,
                y + GAP + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        boolean deleteHovered = NVGUtils.isHovering(mouseX, mouseY, deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT);
        NVGUtils.drawRect(deleteX, y + GAP, DELETE_WIDTH, BOX_HEIGHT, 5f,
                deleteHovered ? FatalityColours.SELECTED.brighter() : FatalityColours.SELECTED);
        String deleteText = "Delete";
        NVGUtils.drawText(deleteText, deleteX + (DELETE_WIDTH - NVGUtils.getTextWidth(deleteText, 12, NVGUtils.JOSEFIN)) / 2f,
                y + GAP + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        // pmopmopmopmop[mmop
        if (value.colour != null) {
            Colour colour = NVGUtils.isHovering(mouseX, mouseY, colourX, y + GAP, COLOUR_WIDTH, BOX_HEIGHT) ? value.colour.brighter() : value.colour;
            NVGUtils.drawRect(colourX, y + GAP, COLOUR_WIDTH, BOX_HEIGHT, 1, colour);

            if (expanded) {
                // ar ar ar fredy faze bear
                renderStupidFuckingColourThing(mouseX, mouseY, colourX + COLOUR_WIDTH / 2, y + GAP);
            }
        }
    }

    // what's up team
    private void renderStupidFuckingColourThing(double mouseX, double mouseY, float sbX, float sbY) {
        float bgWidth = BOX_SIZE + (HUE_STRIP_WIDTH * 2) + 24;
        float boxX = sbX + 2; // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

        float hueX = boxX + BOX_SIZE + 10;
        float alphaX = hueX + HUE_STRIP_WIDTH + 10;
        float boxY = sbY + BASE_HEIGHT / 2 + 4;

        NVGUtils.drawRect(boxX - 4, boxY - 4, bgWidth + 4, 128, 2, FatalityColours.PANEL);

        renderOverlay(mouseX, mouseY, sbX, boxY);

        NVGUtils.drawGradientRect(boxX, boxY, BOX_SIZE, BOX_SIZE - 1, 5f, Colour.WHITE, value.colour.hsbMax(), Gradient.LeftToRight);
        NVGUtils.drawGradientRect(boxX, boxY, BOX_SIZE, BOX_SIZE, 5f, Colour.TRANSPARENT, Colour.BLACK, Gradient.TopToBottom);

        short[] hsba = value.colour.getHSBA();
        float sat = hsba[1] / 100f;
        float bright = hsba[2] / 100f;

        Colour fullAlpha = value.colour.alpha(255);

        // box position dot
        float radius = 5f;
        float dotX = boxX + sat * BOX_SIZE;
        float dotY = boxY + (1 - bright) * BOX_SIZE;
        NVGUtils.drawCircle(dotX, dotY, radius, Colour.WHITE);
        NVGUtils.drawCircle(dotX, dotY, radius - 1, fullAlpha);

        NVGUtils.renderImage(getHueImage(), hueX, boxY, HUE_STRIP_WIDTH, BOX_SIZE, 1.5F);

        // hue slider line
        float hueIndicator = hsba[0] / 360f;
        float hueMarkerY = (boxY + hueIndicator * BOX_SIZE) - 2.5f;
        NVGUtils.drawOutlineRect(hueX, hueMarkerY - 1, HUE_STRIP_WIDTH, HUE_STRIP_WIDTH / 2f + 1, 1, Colour.WHITE);
        NVGUtils.drawRect(hueX, hueMarkerY - 1, HUE_STRIP_WIDTH - 1, HUE_STRIP_WIDTH / 2f, fullAlpha);

        NVGUtils.drawGradientRect(alphaX, boxY, HUE_STRIP_WIDTH, BOX_SIZE, 1.5F, value.colour.hsbMax(), Colour.TRANSPARENT, Gradient.TopToBottom);

        float alphaIndicator = 1f - (hsba[3] / 255f);
        float alphaMarkerY = (boxY + alphaIndicator * BOX_SIZE) - 2.5f;
        NVGUtils.drawOutlineRect(alphaX, alphaMarkerY - 1, HUE_STRIP_WIDTH, HUE_STRIP_WIDTH / 2f + 1, 1, Colour.WHITE);
        NVGUtils.drawRect(alphaX, alphaMarkerY - 1,  HUE_STRIP_WIDTH - 1, HUE_STRIP_WIDTH / 2f, value.colour);

        float stringX = boxX + (bgWidth - 50) / 2f;
        float stringY = boxY + 106;

        boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) stringX - 10f, (int) stringY - 2, 65, 18);
        NVGUtils.drawRect(stringX - 10f, stringY - 2, 65f, 18f, 2, inputColour(colourInput.isWriting(), hovered));

        colourInput.render(stringX, boxY + 108);
    }

    private void renderOverlay(double mouseX, double mouseY, float x, float y) {
        Colour hi = value.colour;

        if (draggingSB) {
            updateSB((float) (mouseX - x), (float) (mouseY - y), hi);
        }

        if (draggingHue) {
            updateHue((float) (mouseY - y), hi);
        }

        if (draggingAlpha) {
            updateAlpha((float) (mouseY - y), hi);
        }

        if (!colourInput.isWriting()) colourInput.setValue(hi.getHex());
    }

    public void commitPendingEdits() {
        if (!nameInput.isWriting()) {
            return;
        }

        commitName();
        nameInput.setWriting(false);
        if (selected == this) {
            selected = null;
        }
    }

    private void commitName() {
        String nextName = StringUtils.format(nameInput.getValue().trim());
        if (nextName.isBlank()) {
            nameInput.setValue(StringUtils.format(value.name));
            return;
        }

        if (!nextName.equals(value.name)) {
            value.name = nextName;
            nameInput.setValue(nextName);
            ItemModifier.save();
        }
    }

    private Colour inputColour(boolean writing, boolean hovering) {
        if (writing) {
            return FatalityColours.WRITING_TEXT;
        }
        if (hovering) {
            return FatalityColours.HOVERING_TEXT;
        }
        return FatalityColours.INPUT_TEXT;
    }
}
