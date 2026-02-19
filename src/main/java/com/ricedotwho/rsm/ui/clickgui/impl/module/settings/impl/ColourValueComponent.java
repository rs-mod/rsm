package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render2d.Gradient;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.StringValueComponent.*;

@Getter
public class ColourValueComponent extends ValueComponent<ColourSetting> {
    private static Image HUE_IMAGE;
    private static final String allowed = "abcdefABCDEF0123456789";
    private static ColourValueComponent focusedComponent = null;
    private long lastKeyTime = 0;
    private long lastCharTime = 0;
    private long lastMouseTime = 0;

    private static Image getHueImage() {
        if (HUE_IMAGE == null) {
            HUE_IMAGE = NVGUtils.createImage("/assets/rsm/clickgui/HueGradient.png");
        }
        return HUE_IMAGE;
    }

    private boolean expanded = false;

    private final int boxSize = 100;
    private final int hueStripWidth = 10;

    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private final float baseHeight = 21f;
    private String hex = "";
    private boolean writing = false;

    public ColourValueComponent(ColourSetting setting, ModuleBase parent) {
        super(setting, parent);
    }

    //todo: add a way to make it chroma

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        float sbX = posX + 240 + 24;
        float sbY = posY - baseHeight / 2f;

        float width = 50;

        // todo: fade
        Colour colour = NVGUtils.isHovering(mouseX, mouseY, (int) sbX, (int) sbY, (int) width, (int) baseHeight) ? setting.getValue().brighter() : setting.getValue();
        NVGUtils.drawRect(sbX, sbY, width, baseHeight, 1, colour);

        if (!expanded) return;
        float bgwidth = boxSize + (hueStripWidth * 2) + 24;
        float boxX = (sbX + width + 2) - bgwidth;

        float hueX = boxX + boxSize + 10;
        float alphaX = hueX + hueStripWidth + 10;
        float boxY = sbY + baseHeight + 4;

        NVGUtils.drawRect(boxX - 4, boxY - 4, bgwidth + 4, 128, 2, FatalityColours.PANEL);

        renderOverlay(mouseX, mouseY);

        NVGUtils.drawGradientRect(boxX, boxY, boxSize, boxSize - 1, 5f, Colour.WHITE, setting.getValue().hsbMax(), Gradient.LeftToRight);
        NVGUtils.drawGradientRect(boxX, boxY, boxSize, boxSize, 5f, Colour.TRANSPARENT, Colour.BLACK, Gradient.TopToBottom);

        short[] hsba = setting.getValue().getHSBA();
        float sat = hsba[1] / 100f;
        float bright = hsba[2] / 100f;

        Colour fullAlpha = this.setting.getValue().alpha(255);

        // box position dot
        float radius = 5f;
        float dotX = boxX + sat * boxSize;
        float dotY = boxY + (1 - bright) * boxSize;
        NVGUtils.drawCircle(dotX, dotY, radius, Colour.WHITE);
        NVGUtils.drawCircle(dotX, dotY, radius - 1, fullAlpha);

        NVGUtils.renderImage(getHueImage(), hueX, boxY, hueStripWidth, boxSize, 1.5F);

        // hue slider line
        float hueIndicator = hsba[0] / 360f;
        float hueMarkerY = (boxY + hueIndicator * boxSize) - 2.5f;
        NVGUtils.drawOutlineRect(hueX, hueMarkerY - 1, hueStripWidth, hueStripWidth / 2f + 1, 1, Colour.WHITE);
        NVGUtils.drawRect(hueX, hueMarkerY - 1, hueStripWidth - 1, hueStripWidth / 2f, fullAlpha);

        NVGUtils.drawGradientRect(alphaX, boxY, hueStripWidth, boxSize, 1.5F, setting.getValue().hsbMax(), Colour.TRANSPARENT, Gradient.TopToBottom);

        float alphaIndicator = 1f - (hsba[3] / 255f);
        float alphaMarkerY = (boxY + alphaIndicator * boxSize) - 2.5f;
        NVGUtils.drawOutlineRect(alphaX, alphaMarkerY - 1, hueStripWidth, hueStripWidth / 2f + 1, 1, Colour.WHITE);
        NVGUtils.drawRect(alphaX, alphaMarkerY - 1,  hueStripWidth - 1, hueStripWidth / 2f, setting.getValue());

        float stringX = boxX + (bgwidth - 50) / 2f;
        float stringY = boxY + 106;

        boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) stringX - 10f, (int) stringY - 2, 65, 18);
        Colour boxColor;
        if (writing) {
            boxColor = new Colour(60, 60, 60);
        } else if (hovered) {
            boxColor = new Colour(50, 50, 50);
        } else {
            boxColor = new Colour(40, 40, 40);
        }

        NVGUtils.drawRect(stringX - 10f, stringY - 2, 65f, 18f, 2, boxColor);

        long time = System.currentTimeMillis();
        boolean cursorVisible = writing && (time / 500 % 2 == 0);
        String text = writing ? hex + (cursorVisible ? "|" : "") : hex;
        NVGUtils.drawTextShadow(text, stringX, boxY + 108, 12, Colour.WHITE, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        float y = getPosition().y + baseHeight / 2f + 2f;

        float sbX = getPosition().x + 330 + 24 + 2;
        float sbY = getPosition().y - baseHeight / 2f + 0;
        float width = 50;
        float bgwidth = boxSize + (hueStripWidth * 2) + 24; // 122
        float expandX = (sbX + width + 2) - bgwidth;


        if (mouseButton == 1 || mouseButton == 0) {
            if (NVGUtils.isHovering(mouseX, mouseY, (int) expandX, (int) sbY, (int) width, (int) baseHeight)) {
                expanded = !expanded;
                consumeClick();
                return;
            }
        }
        if (mouseButton != 0 || !expanded) return;

        float boxX = (expandX + width + 1) - bgwidth;
        float hueX = boxX + boxSize + 10;
        float alphaX = hueX + hueStripWidth + 10;
        float boxY = sbY + baseHeight + 4;

        float relX = (float) (mouseX - (getPosition().x + 330 + 24 + width));
        float relY = (float) (mouseY - y);

        float stringX = boxX + (bgwidth - 50) / 2f;
        float stringY = boxY + 106;

        if (NVGUtils.isHovering(mouseX, mouseY, boxX - 4, boxY - 4, bgwidth + 4, 128)) {
            consumeClick();
        }

        if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, boxSize, boxSize)) {
            updateSB(relX, relY);
            draggingSB = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) hueX, (int) y, hueStripWidth, boxSize)) {
            updateHue(relY);
            draggingHue = true;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) alphaX, (int) y, hueStripWidth, boxSize)) {
            updateAlpha(relY);
            draggingAlpha = true;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCharTime < KEY_DEBOUNCE_TIME) {
            return;
        }

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

        if (NVGUtils.isHovering(mouseX, mouseY, stringX, stringY, 65, 18)) {
            if (focusedComponent != null) focusedComponent.writing = false;
            focusedComponent = this;
            writing = true;
            lastMouseTime = currentTime;
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

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCharTime < CHAR_DEBOUNCE_TIME) {
            return false;
        }
        lastCharTime = currentTime;
        String current = hex;

        if (allowed.indexOf(typedChar) != -1 && current.length() < 6) {
            hex = current + typedChar;
            int alpha = setting.getValue().getAlpha();
            setting.setValue(new Colour(hex));
            setting.getValue().setAlpha(alpha);
        }
        return false;
    }

    @Override
    public boolean keyTyped(KeyEvent input) {
        if (!writing || focusedComponent != this) return false;
        String current = hex;
        int key = input.key();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return false;
        }
        lastKeyTime = currentTime;

        if (key == GLFW.GLFW_KEY_BACKSPACE && !current.isEmpty()) {
            hex = current.substring(0, current.length() - 1);
            int alpha = setting.getValue().getAlpha();
            setting.setValue(new Colour(hex));
            setting.getValue().setAlpha(alpha);
        }

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            writing = false;
            focusedComponent = null;
            return true;
        }
        return false;
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    public void renderOverlay(double mouseX, double mouseY) {
        float y = getPosition().y + baseHeight / 2f + 2f;
        float x = getPosition().x + 170;

        if (draggingSB) {
            updateSB((float) (mouseX - x), (float) (mouseY - y));
        }

        if (draggingHue) {
            updateHue((float) (mouseY - y));
        }

        if (draggingAlpha) {
            updateAlpha((float) (mouseY - y));
        }

        if (!writing) hex = setting.getValue().getHex();
    }

    private void updateSB(float relX, float relY) {
        float sat = Math.max(0, Math.min(1, relX / boxSize));
        float bright = Math.max(0, Math.min(1, 1 - relY / boxSize));

        Colour color = setting.getValue();
        color.setHSBA(1, (int) (sat * 100));
        color.setHSBA(2, (int) (bright * 100));
    }

    private void updateHue(float relY) {
        float hue = Math.max(0, Math.min(1, relY / boxSize));
        setting.getValue().setHSBA(0, (int) (hue * 360));
    }
    private void updateAlpha(float relY) {
        float alpha = Math.max(0, Math.min(1, relY / boxSize));
        setting.getValue().setAlpha((int) ((1F - alpha) * 255f));
    }

    @Override
    public int getHeight() {
        return (int) (baseHeight + (expanded ? boxSize : 0));
    }
}