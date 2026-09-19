package com.ricedotwho.rsm.managers.notification;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;

@Getter
public enum NotificationType {
    INFO(NotificationManager.getInfo()),
    WARNING(NotificationManager.getWarning(), Color.fromRGB(255, 216, 0)),
    CHECK(NotificationManager.getCheck()),
    CROSS(NotificationManager.getX());

    private final Image image;
    private final Color color;
    NotificationType(Image image, Color color) {
        this.image = image;
        this.color = color;
    }

    NotificationType(Image image) {
        this.image = image;
        this.color = Color.WHITE;
    }
}
