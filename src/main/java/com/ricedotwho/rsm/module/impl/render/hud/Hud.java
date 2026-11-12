package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

@Getter
@ModuleInfo(aliases = "Hud", id = "hud", category = Category.RENDER)
@SuppressWarnings("unused")
public class Hud extends Module {
    @SuppressWarnings("unused")
    private static final Hud instance = new Hud();
    private final GroupSetting<TimeHud> timeHud = new GroupSetting<>("Time", new TimeHud(this));

    private static TitleInfo titleInfo = null;
    private static TitleInfo subTitleInfo = null;

    private final HudSetting title = new HudSetting("Title", new Vector2f(50, 50)) {
        @Override
        protected @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            int dims;
            if (example) {
                dims = this.text(gfx, "Example Title!", 0, 0, Color.RED);
            } else {
                dims = this.text(gfx, titleInfo.content, 0, 0, titleInfo.color);
                if (titleInfo.isExpired()) {
                    titleInfo = null;
                }
            }
            return dims(dims);
        }
    }.isVisible(() -> titleInfo != null);

    private final HudSetting subTitle = new HudSetting("Subtitle", new Vector2f(50, 50)) {
        @Override
        protected @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            int dims;
            if (example) {
                dims = this.text(gfx, "Example Subtitle!", 0, 0, Color.RED);
            } else {
                dims = this.text(gfx, subTitleInfo.content, 0, 0, subTitleInfo.color);
                if (subTitleInfo.isExpired()) {
                    subTitleInfo = null;
                }
            }
            return dims(dims);
        }
    }.isVisible(() -> subTitleInfo != null);

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
