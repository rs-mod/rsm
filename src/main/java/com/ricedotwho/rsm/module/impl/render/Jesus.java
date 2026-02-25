package com.ricedotwho.rsm.module.impl.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.CustomSounds;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Jesus", id = "Jesus", category = Category.RENDER)
public class Jesus extends Module {
    private final NumberSetting timeToFade = new NumberSetting("Time to fade", 1, 5000, 1000, 50);
    private final NumberSetting opacity = new NumberSetting("Opacity", 1, 100, 100, 2);
    private final NumberSetting health = new NumberSetting("Health %", 0, 0.5, 0.15, 0.01);
    private final BooleanSetting maskProc = new BooleanSetting("Mask Proc", true);

    private long started = 0;
    private boolean showJesus = false;
    private static final Pattern pattern = Pattern.compile("^Your (.+?) (Pet|Mask) saved (you from certain death|your life)!");

    private static Image jesusImage = null;

    public Jesus() {
        this.registerProperty(
                timeToFade,
                opacity,
                health,
                maskProc
        );
    }

    private Image getJesusImage() {
        if (jesusImage == null){
            jesusImage = NVGUtils.createImage("/assets/rsmpack/image/jesus.png");
        }
        return jesusImage;
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!maskProc.getValue()) return;
        String text = ChatFormatting.stripFormatting(event.getMessage().getString());
        if (pattern.matcher(text).find() || text.equals("Second Wind Activated! Your Spirit Mask saved your life!")) {
            jesus();
        }
    }

    @SubscribeEvent
    public void onHealthChanged(HealthChangedEvent.Hurt event) {
        if (mc.player == null) return;
        if (event.getHealthAfter() <= mc.player.getMaxHealth() * health.getValue().doubleValue() && event.getHealthAfter() > 0) {
            jesus();
        }
    }

    public void jesus() {
        if (mc.player == null || !this.isEnabled()) return;
        started = System.currentTimeMillis();
        showJesus = true;
        mc.player.playSound(CustomSounds.BELL);
    }

    private float getOpacity() {
        long elapsed = System.currentTimeMillis() - started;
        float progress = Math.min(1f, elapsed / timeToFade.getValue().floatValue());
        float baseOpacity = opacity.getValue().floatValue() / 100f;

        float opacity = baseOpacity * (1f - progress);

        if (opacity <= 0f) {
            showJesus = false;
            return 0f;
        }

        return opacity;
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (!showJesus) return;
        Window window = mc.getWindow();
        float w = window.getScreenWidth();
        float h = window.getScreenHeight();

        NVGSpecialRenderer.draw(event.getGfx(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), () -> {
            NVGUtils.renderImage(getJesusImage(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), 0f, getOpacity());
        });
    }
}
