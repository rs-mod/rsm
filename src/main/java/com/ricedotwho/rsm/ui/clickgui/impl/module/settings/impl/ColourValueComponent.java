package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.render.render2d.Gradient;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;

@Getter
public class ColourValueComponent extends InputValueComponent<ColourSetting> {
    private static Image HUE_IMAGE;
    @Getter
    private static final String allowed = "abcdefABCDEF0123456789";
    private static ColourValueComponent expandedInstance = null;
    private final StopWatch stopWatch = new StopWatch();
    private boolean lastHovered = false;

    public static Image getHueImage() {
        if (HUE_IMAGE == null) {
            HUE_IMAGE = NVGUtils.createImage("/assets/rsm/clickgui/HueGradient.png");
        }
        return HUE_IMAGE;
    }

    private boolean expanded = false;

    public static final int BOX_SIZE = 100;
    public static final int HUE_STRIP_WIDTH = 10;
    public static final float BASE_HEIGHT = 21f;
    public static final float WIDTH = 50;

    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public ColourValueComponent(ColourSetting setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getValue().getHex(), 12, allowed, 6, false), new TextInput(String.valueOf(setting.getValue().getAlpha()), 12, "0123456789", 3, false));
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        float sbX = posX + 240 + 24;
        float sbY = posY - BASE_HEIGHT / 2f;

        // todo: fade
        Colour colour = NVGUtils.isHovering(mouseX, mouseY, (int) sbX, (int) sbY, (int) WIDTH, (int) BASE_HEIGHT) ? setting.getValue().brighter() : setting.getValue();
        NVGUtils.drawRect(sbX, sbY, WIDTH, BASE_HEIGHT, 1, colour);

        if (!expanded) return;
        boolean chroma = setting.getValue().getDataBit() != -1;
        float bgwidth = BOX_SIZE + (HUE_STRIP_WIDTH * 2) + 24;
        float boxX = (sbX + WIDTH + 2) - bgwidth;

        float hueX = boxX + BOX_SIZE + 10;
        float alphaX = hueX + HUE_STRIP_WIDTH + 10;
        float boxY = sbY + BASE_HEIGHT + 4;

        NVGUtils.drawRect(boxX - 4, boxY - 4, bgwidth + 4, 128, 2, FatalityColours.PANEL);

        renderOverlay(mouseX, mouseY);

        NVGUtils.drawGradientRect(boxX, boxY, BOX_SIZE, BOX_SIZE - 1, 5f, Colour.WHITE, setting.getValue().hsbMax(), Gradient.LeftToRight);
        NVGUtils.drawGradientRect(boxX, boxY, BOX_SIZE, BOX_SIZE, 5f, Colour.TRANSPARENT, Colour.BLACK, Gradient.TopToBottom);

        short[] hsba = setting.getValue().getHSBA();
        float sat = hsba[1] / 100f;
        float bright = hsba[2] / 100f;

        Colour fullAlpha = this.setting.getValue().alpha(255);

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

        NVGUtils.drawGradientRect(alphaX, boxY, HUE_STRIP_WIDTH, BOX_SIZE, 1.5F, setting.getValue().hsbMax(), Colour.TRANSPARENT, Gradient.TopToBottom);

        float alphaIndicator = 1f - (hsba[3] / 255f);
        float alphaMarkerY = (boxY + alphaIndicator * BOX_SIZE) - 2.5f;
        NVGUtils.drawOutlineRect(alphaX, alphaMarkerY - 1, HUE_STRIP_WIDTH, HUE_STRIP_WIDTH / 2f + 1, 1, Colour.WHITE);
        NVGUtils.drawRect(alphaX, alphaMarkerY - 1,  HUE_STRIP_WIDTH - 1, HUE_STRIP_WIDTH / 2f, setting.getValue());

        float stringX = boxX + 10f;
        float stringY = boxY + 106;

        boolean hexHovered = NVGUtils.isHovering(mouseX, mouseY, (int) stringX - 10f, (int) stringY - 2, 65, 18);
        Colour hexBoxColor;
        TextInput hex = getInput();
        if (hex.isWriting()) {
            hexBoxColor = FatalityColours.WRITING_TEXT;
        } else if (hexHovered) {
            hexBoxColor = FatalityColours.HOVERING_TEXT;
        } else {
            hexBoxColor = FatalityColours.INPUT_TEXT;
        }

        NVGUtils.drawRect(stringX - 10f, stringY - 2, 65f, 18f, 2, hexBoxColor);
        hex.render(stringX, boxY + 108);

        // chroma
        float chromaX = boxX + 65 + 15f;

        boolean isHovered = NVGUtils.isHovering(mouseX, mouseY, chromaX, stringY, 14, 14);

        if (isHovered != lastHovered) {
            stopWatch.reset();
        }

        int hoverAlpha = isHovered ? (int) Math.min(255, (float) stopWatch.getElapsedTime() / 200.0f * 255) : 0;
        int r = chroma ? 255 : 150;
        int alpha = chroma ? 255 : hoverAlpha;

        NVGUtils.drawRect(chromaX, stringY, 14, 14, 2, FatalityColours.INPUT_TEXT);
        NVGUtils.drawCheckmark(chromaX, stringY, 13f, 1f, new Colour(r, r, r, alpha));
        lastHovered = isHovered;

        // alpha
        float alphaBoxX = boxX + (bgwidth - 30);

        TextInput aIn = inputs.get(1);
        boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) alphaBoxX - 10f, (int) stringY - 2, 65, 18);
        Colour boxColor;
        if (aIn.isWriting()) {
            boxColor = FatalityColours.WRITING_TEXT;
        } else if (hovered) {
            boxColor = FatalityColours.HOVERING_TEXT;
        } else {
            boxColor = FatalityColours.INPUT_TEXT;
        }

        NVGUtils.drawRect(alphaBoxX - 5f, stringY - 2, 30f, 18f, 2, boxColor);
        aIn.render(alphaBoxX, boxY + 108);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        float y = getPosition().y + BASE_HEIGHT / 2f + 2f;

        float sbX = getPosition().x + 330 + 24 + 2;
        float sbY = getPosition().y - BASE_HEIGHT / 2f + 0;
        float width = 50;
        float bgwidth = BOX_SIZE + (HUE_STRIP_WIDTH * 2) + 24; // 122
        float expandX = (sbX + width + 2) - bgwidth;


        if (mouseButton == 1 || mouseButton == 0) {
            if (NVGUtils.isHovering(mouseX, mouseY, (int) expandX, (int) sbY, (int) width, (int) BASE_HEIGHT)) {
                // close other open pickers
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
                setAllNotWriting();
                return;
            }
        }
        if (mouseButton != 0 || !expanded) {
            setAllNotWriting();
            return;
        }

        float boxX = (expandX + width + 1) - bgwidth;
        float hueX = boxX + BOX_SIZE + 10;
        float alphaX = hueX + HUE_STRIP_WIDTH + 10;
        float boxY = sbY + BASE_HEIGHT + 4;

        float relX = (float) (mouseX - (getPosition().x + 330 + 24 + width));
        float relY = (float) (mouseY - y);

        boolean inside = NVGUtils.isHovering(mouseX, mouseY, boxX - 4, boxY - 4, bgwidth + 4, 128);
        if (inside) {
            consumeClick();
        }

        if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, BOX_SIZE, BOX_SIZE)) {
            updateSB(relX, relY, setting.getValue());
            draggingSB = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
            updateHue(relY, setting.getValue());
            draggingHue = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
            updateAlpha(relY, setting.getValue());
            draggingAlpha = true;
        } else if (!inside) {
            // clicking outside closes the picker
            expanded = false;
            setAllNotWriting();
            if (focusedComponent == this) {
                focusedComponent = null;
            }
            if (expandedInstance == this) {
                expandedInstance = null;
            }
        }

        float stringX = boxX + 10f;
        float stringY = boxY + 106;
        float chromaX = boxX + 65 + 15f;
        float alphaBoxX = boxX + (bgwidth - 30);

        boolean hoveringHexInput = NVGUtils.isHovering(mouseX, mouseY, stringX, stringY, 65, 18);
        boolean hoveringChroma = NVGUtils.isHovering(mouseX, mouseY, chromaX, stringY, 14, 14);
        boolean hoveringAlphaInput = NVGUtils.isHovering(mouseX, mouseY, alphaBoxX, stringY, 30, 18);

        if (hoveringHexInput) {
            TextInput hexInput = getInput();
            if (focusedComponent != null) focusedComponent.setAllNotWriting();
            focusedComponent = this;
            setAllNotWriting();
            hexInput.setWriting(true);
            hexInput.click((float) (mouseX - stringX), mouseButton);
        } else if (hoveringChroma) {
            if (focusedComponent != null) focusedComponent.setAllNotWriting();
            focusedComponent = null;
            if (setting.getValue().getDataBit() == -1) {
                setting.getValue().setChromaSpeed(10);
            } else {
                setting.getValue().setChromaSpeed(-1);
                setting.setValue(new Colour(setting.getValue().getRGB()));
            }
        } else if (hoveringAlphaInput) {
            TextInput input = inputs.get(1);
            if (focusedComponent != null) focusedComponent.setAllNotWriting();
            focusedComponent = this;
            setAllNotWriting();
            input.setWriting(true);
            input.click((float) (mouseX - alphaBoxX), mouseButton);
        } else {
            if (isWriting() && focusedComponent == this) {
                focusedComponent = null;
            }
            setAllNotWriting();
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!isWriting() || focusedComponent != this) return false;

        TextInput input = getWriting();
        if (input == null) return false;

        boolean ret = input.charTyped(typedChar);

        if (input == getInput()) {
            int alpha = setting.getValue().getAlpha();
            setting.setValue(new Colour(getInput().getValue()));
            setting.getValue().setAlpha(alpha);
        } else if (NumberUtils.isInteger(input.getValue())) {
            int alpha = Integer.parseInt(input.getValue());
            setting.getValue().setAlpha(alpha);
        }

        getSetting().onEdit();
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!isWriting() || focusedComponent != this) return false;
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            setAllNotWriting();
            focusedComponent = null;
            return true;
        }

        TextInput input = getWriting();
        if (input == null) return false;

        boolean ret = input.keyTyped(event);

        if (input == getInput()) {
            int alpha = setting.getValue().getAlpha();
            setting.setValue(new Colour(getInput().getValue()));
            setting.getValue().setAlpha(alpha);
        } else if (NumberUtils.isInteger(input.getValue())) {
            int alpha = Integer.parseInt(input.getValue());
            setting.getValue().setAlpha(alpha);
        }

        getSetting().onEdit();
        return ret;
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
        getSetting().onEdit();
    }

    private void renderOverlay(double mouseX, double mouseY) {
        float y = getPosition().y + BASE_HEIGHT / 2f + 2f;
        float x = getPosition().x + 170;

        Colour hi = setting.getValue();

        if (draggingSB) {
            updateSB((float) (mouseX - x), (float) (mouseY - y), hi);
        }

        if (draggingHue) {
            updateHue((float) (mouseY - y), hi);
        }

        if (draggingAlpha) {
            updateAlpha((float) (mouseY - y), hi);
        }

        if (!isWriting()) {
            getInput().setValue(setting.getValue().getHex());
            inputs.get(1).setValue(String.valueOf(setting.getValue().getAlpha()));
        }
    }

    public static void updateSB(float relX, float relY, Colour colour) {
        float sat = Math.max(0, Math.min(1, relX / BOX_SIZE));
        float bright = Math.max(0, Math.min(1, 1 - relY / BOX_SIZE));

        colour.setHSBA(1, (int) (sat * 100));
        colour.setHSBA(2, (int) (bright * 100));
    }

    public static void updateHue(float relY, Colour colour) {
        float hue = Math.max(0, Math.min(1, relY / BOX_SIZE));
        colour.setHSBA(0, (int) (hue * 360));
    }

    public static void updateAlpha(float relY, Colour colour) {
        float alpha = Math.max(0, Math.min(1, relY / BOX_SIZE));
        colour.setAlpha((int) ((1F - alpha) * 255f));
    }

    @Override
    public int getHeight() {
        return (int) (BASE_HEIGHT + (expanded ? BOX_SIZE : 0));
    }
}
