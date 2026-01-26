package com.ricedotwho.rsm.component.impl.notification;

import com.ricedotwho.rsm.component.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationComponent extends Component {
    private static final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private static final ResourceLocation WARNING = new ResourceLocation("clickgui/warning.png");
    private static final ResourceLocation INFO = new ResourceLocation("clickgui/info.png");

    public NotificationComponent() {
        super("NotificationComponent");

    }

    public static void showNotification(String title, String description, boolean warning, int duration) {
        notifications.add(new Notification(title, description, warning, duration));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        notifications.removeIf(Notification::isReadyToRemove);
    }

    @SubscribeEvent
    public void render(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        int y = sr.getScaledHeight() - 40;

        for (Notification n : notifications) {
            n.isExpired();
            if (!n.expired || n.getSlideOutProgress() < 1.0f) {
                drawNotification(n, y);
                y -= 45;
            }
        }
    }



    private void drawNotification(Notification n, int y) {
        //double remainingMillis = (n.duration - n.timer.getElapsedTime());
        //double remainingTime = remainingMillis / 1000.0;
        //String timeFormatted = String.format("%.1f", Math.max(0, remainingTime));
        ScaledResolution sr = new ScaledResolution(mc);

        //int timeWidth = (int) Fonts.getProductSans(17).getWidth("0.0");
        int titleWidth = (int) Fonts.getJoseFinBold(20).getWidth(n.title) + 67;
        int descWidth = (int) Fonts.getProductSans(17).getWidth(n.description + " (" + 0.0 + "s left)") + 67;
        int fullWidth = Math.max(titleWidth, descWidth);
        int x = sr.getScaledWidth() - fullWidth;

        if (n.expired) {
            float slideProgress = n.getSlideOutProgress();
            x += (int) (slideProgress * (fullWidth + 20));
            if (x >= sr.getScaledWidth() + 20) return;
        }

        RenderUtils.drawRect(x, y, fullWidth, 33, new Color(0, 0, 0, 165));

        ResourceLocation icon = n.warning ? WARNING : INFO;
        RenderUtils.drawImage(icon, x + 1, y + 1, 32, 32);

        Fonts.getJoseFinBold(20).drawString(n.title, x + 33, y + 8, -1);

        Fonts.getProductSans(17).drawString(n.description, x + 33, y + 18, new Color(200, 200, 200).getRGB());

        Color theme = n.warning ? new Color(255, 216, 0) : new Color(255, 255, 255);
        int remainingWidth = (int) (fullWidth * (1.0f - n.getProgress()));
        RenderUtils.drawRect(x, y + 32, remainingWidth, 1, theme);
    }

}
