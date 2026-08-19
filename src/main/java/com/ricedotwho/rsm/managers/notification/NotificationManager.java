package com.ricedotwho.rsm.managers.notification;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.render.animation.Easing;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
@Register
public class NotificationManager {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private Image WARNING = null;
    private Image INFO = null;
    private Image CHECK = null;
    private Image X = null;
    private final Easing OPEN_EASING = Easing.OUT_CUBIC;
    private final Easing CLOSE_EASING = Easing.IN_CUBIC;
    private final float NOTIFICATION_RADIUS = 6.0f;
    private final float PROGRESS_INSET = 3.0f;
    private final float RIGHT_MARGIN = 8.0f;


    public void showNotification(String title, String description, boolean warning, int duration) {
        notifications.add(new Notification(title, description, warning, duration));
    }

    private Image getWarning() {
        if (WARNING == null) {
            WARNING = NVGUtils.createImage("/assets/rsm/clickgui/warning.png");
        }
        return WARNING;
    }

    private Image getInfo() {
        if (INFO == null) {
            INFO = NVGUtils.createImage("/assets/rsm/clickgui/info.png");
        }
        return INFO;
    }
// thank you cga
    private Image getCheck() {
        if (CHECK == null) {
            CHECK = NVGUtils.createImage("/assets/rsm/clickgui/check.png");
        }
        return CHECK;
    }

    private Image getX() {
        if (X == null) {
            X = NVGUtils.createImage("/assets/rsm/clickgui/x.png");
        }
        return X;
    }

    private Image getNotificationIcon(Notification n) {
        if (n.warning) return getWarning();

        String titleLower = n.title.toLowerCase();
        if (titleLower.startsWith("enabled ") || titleLower.endsWith(" enabled")) return getCheck();
        if (titleLower.startsWith("disabled ") || titleLower.endsWith(" disabled")) return getX();

        return getInfo();
    }

    @SubscribeEvent
    private void onTick(TickEvent.ClientStart event) {
        notifications.removeIf(Notification::isReadyToRemove);
    }

    @SubscribeEvent
    private void render(Render2DEvent event) {
        Window window = mc.getWindow();

        NVGSpecialRenderer.draw(event.getGfx(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), () -> {
            int y = window.getGuiScaledHeight() - 45;
            NVGUtils.scale(window.getGuiScale());
            for (Notification n : notifications) {
                n.update();
                if (!n.expired || n.getSlideProgress() < 1.0f) {
                    drawNotification(event.getGfx(), n, y);
                    y -= 45;
                }
            }
        });
    }

    private final Color background = Color.fromRGB(0, 0, 0, 0.65f);
    private final Color descriptionColor = Color.fromRGB(200, 200, 200);
    private final Color randomAssColorICouldntBeAskedToFigureOutWhatItIsFor = Color.fromRGB(255, 216, 0);
    private void drawNotification(GuiGraphicsExtractor gfx, Notification n, int y) {
        float titleWidth = NVGUtils.getTextWidth(n.title, 10, NVGUtils.getFont(NVGUtils.JOSEFIN_BOLD)) + 67;
        float descWidth = NVGUtils.getTextWidth(n.description/* + " (" + 0.0 + "s left)"*/, 8, NVGUtils.getFont(NVGUtils.PRODUCT_SANS)) + 67;
        float fullWidth = Math.max(titleWidth, descWidth);
        float x = gfx.guiWidth() - fullWidth - RIGHT_MARGIN;

        float alpha = 1.0f;
        float scale = 1.0f;
        if (n.slideIn) {
            float openProgress = 1.0f - n.getSlideProgress();
            float easedOpen = OPEN_EASING.getFunction().apply((double) openProgress).floatValue();
            alpha = easedOpen;
            scale = 1.2f - (0.2f * easedOpen);
        } else if (n.expired) {
            float closeProgress = n.getSlideProgress();
            float easedClose = CLOSE_EASING.getFunction().apply((double) closeProgress).floatValue();
            alpha = 1.0f - easedClose;
            scale = 1.0f + (0.2f * easedClose);
        }

        if (alpha <= 0.0f) return;

        float centerX = x + (fullWidth / 2.0f);
        float centerY = y + 16.5f;

        NVGUtils.push();
        NVGUtils.translate(centerX, centerY);
        NVGUtils.scale(scale, scale);
        NVGUtils.translate(-centerX, -centerY);
        NVGUtils.globalAlpha(alpha);

        NVGUtils.drawRect(x, y, fullWidth, 33, NOTIFICATION_RADIUS, background);

        Image icon = getNotificationIcon(n);
        NVGUtils.renderImage(icon, x + 1, y + 1, 32, 32);

        NVGUtils.drawText(n.title, x + 33, y + 8, 10, Color.WHITE, NVGUtils.getFont(NVGUtils.JOSEFIN_BOLD));
        NVGUtils.drawText(n.description, x + 33, y + 18, 8, descriptionColor, NVGUtils.getFont(NVGUtils.JOSEFIN_BOLD));

        Color theme = n.warning ? randomAssColorICouldntBeAskedToFigureOutWhatItIsFor : Color.WHITE;
        float progressTrackX = x + PROGRESS_INSET;
        float progressTrackWidth = Math.max(0.0f, fullWidth - (PROGRESS_INSET * 2.0f));
        int remainingWidth = (int) (progressTrackWidth * (1.0f - n.getProgress()));
        NVGUtils.drawRect(progressTrackX, y + 32, remainingWidth, 1, theme);

        NVGUtils.pop();
    }
}
