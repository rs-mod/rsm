package com.ricedotwho.rsm.component.impl.notification;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationComponent extends ModComponent {
    private static final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private static Image WARNING = null;
    private static Image INFO = null;

    public NotificationComponent() {
        super("NotificationComponent");

    }

    public static void showNotification(String title, String description, boolean warning, int duration) {
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

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        notifications.removeIf(Notification::isReadyToRemove);
    }

    @SubscribeEvent
    public void render(Render2DEvent event) {
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

    private void drawNotification(GuiGraphics gfx, Notification n, int y) {
        //double remainingMillis = (n.duration - n.timer.getElapsedTime());
        //double remainingTime = remainingMillis / 1000.0;
        //String timeFormatted = String.format("%.1f", Math.max(0, remainingTime));

        //int timeWidth = (int) Fonts.getProductSans(17).getWidth("0.0");

        float titleWidth = NVGUtils.getTextWidth(n.title, 10, NVGUtils.JOSEFIN_BOLD) + 67;
        float descWidth = NVGUtils.getTextWidth(n.description/* + " (" + 0.0 + "s left)"*/, 8, NVGUtils.PRODUCT_SANS) + 67;
        float fullWidth = Math.max(titleWidth, descWidth);
        float x = gfx.guiWidth() - fullWidth;

        if (n.expired || n.slideIn) {
            float slideProgress = n.getSlideProgress();
            x += (int) (slideProgress * (fullWidth + 20));
            if (x >= gfx.guiWidth() + 20) return;
        }

        NVGUtils.drawRect(x, y, fullWidth, 33, new Colour(0, 0, 0, 165));

        Image icon = n.warning ? getWarning() : getInfo();
        NVGUtils.renderImage(icon, x + 1, y + 1, 32, 32);

        NVGUtils.drawText(n.title, x + 33, y + 8, 10, Colour.WHITE, NVGUtils.JOSEFIN_BOLD);
        NVGUtils.drawText(n.description, x + 33, y + 18, 8, new Colour(200, 200, 200), NVGUtils.JOSEFIN_BOLD);

        Colour theme = n.warning ? new Colour(255, 216, 0) : new Colour(255, 255, 255);
        int remainingWidth = (int) (fullWidth * (1.0f - n.getProgress()));
        NVGUtils.drawRect(x, y + 32, remainingWidth, 1, theme);
    }
}
