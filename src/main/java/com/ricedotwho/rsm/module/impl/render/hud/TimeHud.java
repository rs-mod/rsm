package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.text.SimpleDateFormat;

@Getter
@SubModuleInfo(name = "Time", alwaysDisabled = false)
public class TimeHud extends SubModule<Hud> {
    private final BooleanSetting mcFont = new BooleanSetting("Minecraft Font", true);
    private final BooleanSetting shadow = new BooleanSetting("Shadow", false);
    private final BooleanSetting timeHud24h = new BooleanSetting("24 Hour", false);
    private final ColorSetting timeColor = new ColorSetting("Time Color", Color.WHITE);
    private final HudSetting timeHudPos = new HudSetting("Time", new Vector2f(50, 50)) {
        @Override
        protected @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            return dims(this.text(gfx, content,0, 0, timeColor.getValue(), shadow:shadow.getValue()));
        }
    };

    private static final SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm:ss a");

    private String content = "[00:00:00]";

    public TimeHud(Hud hud) {
        super(hud);
    }

    @SubscribeEvent
    private void onTick(TickEvent.End event) {
        content = "[" + (timeHud24h.getValue() ? sdf24.format(System.currentTimeMillis()) : sdf12.format(System.currentTimeMillis())) + "]";
    }
}
