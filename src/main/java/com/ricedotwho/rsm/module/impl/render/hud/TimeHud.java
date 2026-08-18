package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import org.joml.Vector2d;

import java.text.SimpleDateFormat;

@Getter
@SubModuleInfo(name = "Time", alwaysDisabled = false)
public class TimeHud extends SubModule<Hud> {
    private final BooleanSetting mcFont = new BooleanSetting("Minecraft Font", true);
    private final BooleanSetting shadow = new BooleanSetting("Shadow", false);
    private final BooleanSetting timeHud24h = new BooleanSetting("24 Hour", false);
    private final ColorSetting timeColor = new ColorSetting("Time Color", Color.WHITE);
    private final DragSetting timeHudPos = new DragSetting("Time", new Vector2d(50, 50), new Vector2d(60, 6));

    private static final SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm:ss a");

    private String content = "[00:00:00]";

    public TimeHud(Hud hud) {
        super(hud);
    }

    @Override
    public void onEnable() {
        if (mc.level == null || mc.player == null) return;
    }

    @SubscribeEvent
    private void onTick(ClientTickEvent.Start event) {
        content = "[" + (timeHud24h.getValue() ? sdf24.format(System.currentTimeMillis()) : sdf12.format(System.currentTimeMillis())) + "]";
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mcFont.getValue()) {
            timeHudPos.renderScaledGFX(event.getGfx(), () -> event.getGfx().text(mc.font, content,0, 0, timeColor.getValue().getARGB(), shadow.getValue()), 65, 6.5f);
        } else {
            timeHudPos.renderScaled(event.getGfx(), () -> {
                if (shadow.getValue()) {
                    NVGUtils.drawTextShadow(content, 0, 0, 12, timeColor.getValue(), NVGUtils.getFont(NVGUtils.JOSEFIN));
                } else {
                    NVGUtils.drawText(content, 0, 0, 12, timeColor.getValue(), NVGUtils.getFont(NVGUtils.JOSEFIN));
                }
            }, 65, 7.5f);
        }
    }
}
