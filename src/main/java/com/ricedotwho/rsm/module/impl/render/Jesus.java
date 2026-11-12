package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.CustomSounds;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;

import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Jesus", id = "Jesus", category = Category.RENDER)
public class Jesus extends Module {
    @Getter
    private static final Jesus instance = new Jesus();

    private final NumberSetting<Integer> timeToFade = new NumberSetting<>("Time to fade", 1, 5000, 1000, 50);
    private final NumberSetting<Integer> opacity = new NumberSetting<>("Opacity", 1, 100, 100, 2);
    private final NumberSetting<Double> health = new NumberSetting<>("Health %", 0d, 0.5, 0.15, 0.01);
    private final BooleanSetting maskProc = new BooleanSetting("Mask Proc", true);

    private long started = 0;
    private boolean showJesus = false;
    private static final Pattern pattern = Pattern.compile("^Your (.+?) (Pet|Mask) saved (you from certain death|your life)!");

    private static Image jesusImage = null;
    private static Image adminImage = null;

    // this isnt ai btw i locked in w/ help from atowo
    private Image getJesusImage() {
        if (jesusImage == null) {
            jesusImage = new Image("/assets/rsmpack/image/jesus.png");
            adminImage = new Image("/assets/rsmpack/image/admin.png");
        }
        return Math.random() > 0.0001 ? jesusImage : adminImage;
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
        if (!maskProc.getValue()) return;
        String text = ChatFormatting.stripFormatting(event.getMessage().getString());
        if (pattern.matcher(text).find() || text.equals("Second Wind Activated! Your Spirit Mask saved your life!")) {
            jesus();
        }
    }

    @SubscribeEvent
    private void onHealthChanged(HealthChangedEvent.Hurt event) {
        if (mc.player == null || event.getHealthBefore() == event.getHealthAfter()) return;
        if (event.getHealthAfter() <= mc.player.getMaxHealth() * health.getValue() && event.getHealthAfter() > 0) {
            jesus();
        }
    }

    public void jesus() {
        if (mc.player == null || !this.isEnabled()) return;
        started = System.currentTimeMillis();
        jesusImage = null;
        showJesus = true;
        PlayerUtils.playSound(CustomSounds.BELL);
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

        return Math.clamp(opacity, 0f, 1f);
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        if (!showJesus) return;
        Render2D.image(event.getGfx(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), getJesusImage(), Color.WHITE.withAlpha(getOpacity()));
    }
}
