package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
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
    private boolean writing = false;

    public ColourValueComponent(ColourSetting setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getValue().getHex(), 12, allowed, 6, false));
    }

    //todo: add a way to make it chroma

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        NVGUtils.drawText(this.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        float sbX = posX + 240 + 24;
        float sbY = posY - BASE_HEIGHT / 2f;

        // todo: fade
        Colour colour = NVGUtils.isHovering(mouseX, mouseY, (int) sbX, (int) sbY, (int) WIDTH, (int) BASE_HEIGHT) ? setting.getValue().brighter() : setting.getValue();
        NVGUtils.drawRect(sbX, sbY, WIDTH, BASE_HEIGHT, 1, colour);

        if (!expanded) return;
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

        float stringX = boxX + (bgwidth - 50) / 2f;
        float stringY = boxY + 106;

        boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) stringX - 10f, (int) stringY - 2, 65, 18);
        Colour boxColor;
        if (writing) {
            boxColor = FatalityColours.WRITING_TEXT;
        } else if (hovered) {
            boxColor = FatalityColours.HOVERING_TEXT;
        } else {
            boxColor = FatalityColours.INPUT_TEXT;
        }

        NVGUtils.drawRect(stringX - 10f, stringY - 2, 65f, 18f, 2, boxColor);

        input.render(stringX, boxY + 108, writing);
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
                writing = false;
                return;
            }
        }
        if (mouseButton != 0 || !expanded) {
            writing = false;
            return;
        }

        float boxX = (expandX + width + 1) - bgwidth;
        float hueX = boxX + BOX_SIZE + 10;
        float alphaX = hueX + HUE_STRIP_WIDTH + 10;
        float boxY = sbY + BASE_HEIGHT + 4;

        float relX = (float) (mouseX - (getPosition().x + 330 + 24 + width));
        float relY = (float) (mouseY - y);

        float stringX = boxX + (bgwidth - 50) / 2f;
        float stringY = boxY + 106;

        if (NVGUtils.isHovering(mouseX, mouseY, boxX - 4, boxY - 4, bgwidth + 4, 128)) {
            consumeClick();
        }

        boolean hoveringInput = NVGUtils.isHovering(mouseX, mouseY, stringX, stringY, 65, 18);

        if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, BOX_SIZE, BOX_SIZE)) {
            updateSB(relX, relY, setting.getValue());
            draggingSB = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
            updateHue(relY, setting.getValue());
            draggingHue = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, HUE_STRIP_WIDTH, BOX_SIZE)) {
            updateAlpha(relY, setting.getValue());
            draggingAlpha = true;
        } else if (!hoveringInput) {
            // clicking outside closes the picker
            expanded = false;
            writing = false;
            if (focusedComponent == this) {
                focusedComponent = null;
            }
            if (expandedInstance == this) {
                expandedInstance = null;
            }
        }

        if (hoveringInput) {
            if (focusedComponent != null) focusedComponent.writing = false;
            focusedComponent = this;
            writing = true;
            input.click((float) (mouseX - stringX), mouseButton);
        } else {
            if (writing && focusedComponent == this) {
                writing = false;
                focusedComponent = null;
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;

        boolean ret = input.charTyped(typedChar);

        int alpha = setting.getValue().getAlpha();
        setting.setValue(new Colour(input.getValue()));
        setting.getValue().setAlpha(alpha);
        getSetting().onEdit();
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!writing || focusedComponent != this) return false;
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            writing = false;
            focusedComponent = null;
            return true;
        }
        boolean ret = input.keyTyped(event);
        int alpha = setting.getValue().getAlpha();
        setting.setValue(new Colour(input.getValue()));
        setting.getValue().setAlpha(alpha);
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

        if (!writing) input.setValue(setting.getValue().getHex());
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
