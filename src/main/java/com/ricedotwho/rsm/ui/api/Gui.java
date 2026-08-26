package com.ricedotwho.rsm.ui.api;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.event.impl.render.GuiRender;
import com.ricedotwho.rsm.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.impl.popups.Popup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static com.ricedotwho.rsm.type.Accessor.mc;

public abstract class Gui extends Screen implements AutoCloseable {
    protected enum GuiAlignment {
        TopLeft,
        TopMiddle,
        TopRight,
        CenterLeft,
        CenterMiddle,
        CenterRight,
        BottomLeft,
        BottomMiddle,
        BottomRight;

        private float calculateX(float screenWidth) {
            return switch (this) {
                case TopLeft, CenterLeft, BottomLeft -> 0f;
                case TopMiddle, CenterMiddle, BottomMiddle -> 0.5f * screenWidth;
                case TopRight, CenterRight, BottomRight -> screenWidth;
            };
        }

        private float calculateY(float screenHeight) {
            return switch (this) {
                case TopLeft, TopMiddle, TopRight -> 0f;
                case CenterLeft, CenterMiddle, CenterRight -> screenHeight * 0.5f;
                case BottomLeft, BottomMiddle, BottomRight -> screenHeight;
            };
        }

        private float getXMultiplier() {
            return switch (this) {
                case TopLeft , CenterLeft, BottomLeft -> 0f;
                case TopMiddle, CenterMiddle, BottomMiddle -> -0.5f;
                case TopRight, CenterRight, BottomRight -> -1f;
            };
        }
        private float getYMultiplier() {
            return switch (this) {
                case TopLeft, TopMiddle, TopRight -> 0f;
                case CenterLeft, CenterMiddle, CenterRight -> -0.5f;
                case BottomLeft, BottomMiddle, BottomRight -> -1f;
            };
        }
    }

    @Setter
    @Getter
    float xOffset = 0;

    @Setter
    @Getter
    float yOffset = 0;
    
    @Getter
    private static final ArrayList<Popup> popups = new ArrayList<>();
    public static void registerPopup(Popup popup) {
        popups.add(popup);
    }

    @Override
    public final void extractRenderState(@NonNull GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        Palette.updateColors();
        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            new GuiRender.CursorReset().post();
            float width = mc.getWindow().getWidth();
            float height = mc.getWindow().getHeight();

            frame.calculateLayout(width, height);
            NVGUtils.push();
            NVGUtils.translate(
                    originX(),
                    originY()
            );
            NVGUtils.push();
            for (UiElement popup : popups) {
                NVGUtils.globalAlpha(0f);
                popup.dispatchFrame(0f, 0f, this.mouseX(), this.mouseY(), 0f);
            }
            NVGUtils.pop();

            frame.dispatchFrame(0, 0, this.mouseX(), this.mouseY(), 0f);

            for (UiElement popup : popups) {
                popup.calculateLayout(Float.NaN, Float.NaN);
                popup.dispatchFrame(0, 0, this.mouseX(), this.mouseY(), 0f);
            }
            NVGUtils.pop();
            new GuiRender.CursorSet().post();
        });
    }

    public float originX() {
        float width = mc.getWindow().getWidth();
        return Math.round(alignment.calculateX(width) + frame.layoutWidth() * alignment.getXMultiplier());
    }

    public float originY() {
        float height = mc.getWindow().getHeight();
        return Math.round(alignment.calculateY(height) + frame.layoutHeight() * alignment.getYMultiplier());
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        boolean cancelClick = false;
        for (UiElement popup : popups) {

            if (!cancelClick) {
                if (popup.dispatchMouseClicked(event.button(), 0, 0, mouseX(), mouseY(), 0f)) cancelClick = true;
            }

            popup.dispatchMouseClickedUncancelable(event.button(), 0, 0, mouseX(), mouseY(), 0f);
        }
        if (!cancelClick) frame.dispatchMouseClicked(event.button(), 0, 0, mouseX(), mouseY(), 0f);
        frame.dispatchMouseClickedUncancelable(event.button(), 0, 0, mouseX(), mouseY(), 0f);
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        frame.dispatchMouseReleased(event.button(), 0, 0, mouseX(), mouseY(), 0f);
        for (UiElement popup : popups) {
            popup.dispatchMouseReleased(event.button(), 0, 0, mouseX(), mouseY(), 0f);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        for (UiElement popup : popups) {
            if (!popup.dispatchMouseScrolled((float) scrollY, 0, 0, mouseX(), mouseY(), 0f)) continue;
            return super.mouseScrolled(x, y, scrollX, scrollY);
        }

        frame.dispatchMouseScrolled((float) scrollY, 0, 0, mouseX(), mouseY(), 0f);
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        for (UiElement popup : popups) {
            if (popup.dispatchKeyPressed(event.key(), mouseX(), mouseY(), 0f)) return true;
        }
        if (frame.dispatchKeyPressed(event.key(), mouseX(), mouseY(), 0f)) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NotNull CharacterEvent event) {
        for (UiElement popup : popups) {
            if (popup.dispatchCharTyped(event.codepointAsString(), mouseX(), mouseY(), 0f)) return true;
        }
        if (frame.dispatchCharTyped(event.codepointAsString(), mouseX(), mouseY(), 0f)) return true;
        return super.charTyped(event);
    }

   public float mouseX() {
      float w = mc.getWindow().getWidth();
      return (float) mc.mouseHandler.xpos() - alignment.calculateX(w) - frame.layoutWidth() * alignment.getXMultiplier();
   }

   public float mouseY() {
      float h = mc.getWindow().getHeight();
      return (float) mc.mouseHandler.ypos() - alignment.calculateY(h) - frame.layoutHeight() * alignment.getYMultiplier();
   }

    private final GuiAlignment alignment;

    public Node frame;

    protected Gui(Component title, @NonNull GuiAlignment alignment, @NonNull Node frame) {
        super(title);
        this.alignment = alignment;
        this.frame = frame;
    }

    public static boolean hasShiftDown() {
        return mc.hasShiftDown();
    }

    public static boolean hasControlDown() {
        Window window = mc.getWindow();
        return Util.getPlatform() == Util.OS.OSX ? 
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER)
            : 
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);

    }

    @SuppressWarnings("unused")
    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
    }

    @Override
    public void extractBackground(@org.jspecify.annotations.NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void close() { frame.close(); }

    @Override
    public void onClose() {
        super.onClose();
        new GuiRender.CursorReset().post(); //to prevent the cursor from staying in a weird state when you close the ui
        new GuiRender.CursorSet().post();
        UniversalSettings.saveFavoriteColors();
        for (Popup popup : popups) {
            popup.onGuiClose();
        }
    }
}
