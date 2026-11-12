package com.ricedotwho.rsm.managers.notification;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.render.animation.Easing;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
@Register
public class NotificationManager implements Accessor {
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


    public void showNotification(String title, String description, NotificationType type, int duration) {
        notifications.add(new Notification(title, description, type, duration));
    }

    Image getWarning() {
        if (WARNING == null) {
            WARNING = new Image("/assets/rsm/clickgui/warning.png");
        }
        return WARNING;
    }

    Image getInfo() {
        if (INFO == null) {
            INFO = new Image("/assets/rsm/clickgui/info.png");
        }
        return INFO;
    }

    // thank you cga
    Image getCheck() {
        if (CHECK == null) {
            CHECK = new Image("/assets/rsm/clickgui/check.png");
        }
        return CHECK;
    }

    Image getX() {
        if (X == null) {
            X = new Image("/assets/rsm/clickgui/x.png");
        }
        return X;
    }

    @SubscribeEvent
    private void onTick(TickEvent.ClientStart event) {
        notifications.removeIf(Notification::isReadyToRemove);
    }

    @SubscribeEvent
    private void render(Render2DEvent event) {
        var gfx = event.getGfx();
        var window = mc.getWindow();
        int y = gfx.guiHeight() - 45;
        for (Notification n : notifications) {
            n.update();
            if (!n.expired || n.getSlideProgress() < 1.0f) {
                drawNotification(gfx, n, y, window);
                y -= 45;
            }
        }
    }

    private final Color background = Color.fromRGB(0, 0, 0, 0.65f);
    private final Color descriptionColor = Color.fromRGB(200, 200, 200);


    private void drawNotification(GuiGraphicsExtractor gfx, Notification n, int y, Window window) {
        float titleWidth = Fonts.width(Fonts.JOSEFIN_BOLD, n.title, 10) + 67;
        float descWidth = Fonts.width(Fonts.PRODUCT_SANS, n.description, 8) + 67;
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

        gfx.push();
        gfx.pose().translate(centerX, centerY);
        gfx.pose().scale(scale);
        gfx.pose().translate(-centerX, -centerY);

        Render2D.rect(gfx, (int) x, y, (int) fullWidth, 33, background, NOTIFICATION_RADIUS);

        Image icon = n.type.getImage();
        Render2D.image(gfx, x + 1, y + 1, 32, 32, icon);

        Fonts.text(gfx, Fonts.JOSEFIN_BOLD, n.title, x + 33, y + 8, 10);
        Fonts.text(gfx, Fonts.PRODUCT_SANS, n.description, x + 33, y + 18, 8, descriptionColor);

        Color theme = n.type.getColor();
        float progressTrackX = x + PROGRESS_INSET;
        float progressTrackWidth = Math.max(0.0f, fullWidth - (PROGRESS_INSET * 2.0f));
        int remainingWidth = (int) (progressTrackWidth * (1.0f - n.getProgress()));
        Render2D.rect(gfx, progressTrackX, y + 32, remainingWidth, 1, theme);

        gfx.pop();
    }
}
