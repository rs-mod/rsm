package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2d;

@Getter
@ModuleInfo(aliases = "Hud", id = "Hud", category = Category.RENDER)
@SuppressWarnings("unused")
public class Hud extends Module {
    @SuppressWarnings("unused")
    private static final Hud instance = new Hud();
    private final DragSetting title = new DragSetting("Title", new Vector2d(50, 50), new Vector2d(150, 30));
    private final DragSetting subTitle = new DragSetting("Subtitle", new Vector2d(50, 50), new Vector2d(100, 20));
    private final GroupSetting<TimeHud> timeHud = new GroupSetting<>("Time", new TimeHud(this));

    private static TitleInfo titleInfo = null;
    private static TitleInfo subTitleInfo = null;

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        if (titleInfo != null) {
            title.renderScaled(event.getGfx(), () -> {
                NVGUtils.drawCenteredText(titleInfo.content, 75, 0, 24, titleInfo.color, NVGUtils.getFont(NVGUtils.JOSEFIN));
                if (titleInfo.isExpired()) {
                    titleInfo = null;
                }
            }, 150, 30);
        }
        if (subTitleInfo != null) {
            subTitle.renderScaled(event.getGfx(), () -> {
                NVGUtils.drawCenteredText(subTitleInfo.content, 75, 0, 16, subTitleInfo.color, NVGUtils.getFont(NVGUtils.JOSEFIN));
                if (subTitleInfo.isExpired()) {
                    subTitleInfo = null;
                }
            }, 150, 30);
        }
    }

    public static void showTitle(String content, Color color, long duration) {
        showTitle(content, color, duration, false);
    }

    public static void showTitle(String content, Color color, long duration, boolean override) {
        if (titleInfo == null || override) {
            titleInfo = new TitleInfo(content, color, duration);
        }
    }

    public static void showSubTitle(String content, Color color, long duration) {
        showSubTitle(content, color, duration, false);
    }

    public static void showSubTitle(String content, Color color, long duration, boolean override) {
        if (subTitleInfo == null || override) {
            subTitleInfo = new TitleInfo(content, color, duration);
        }
    }

    @AllArgsConstructor
    private static class TitleInfo {
        public final String content;
        public final Color color;
        public final long duration;
        private final long startedAt = System.currentTimeMillis();

        public boolean isExpired() {
            return System.currentTimeMillis() - startedAt > duration;
        }
    }
}
