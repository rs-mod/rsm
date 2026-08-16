package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.Renderer3D;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.type.Pos;
import com.ricedotwho.rsm.ui.old.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Secret Clicked", id = "secret-clicked", category = Category.DUNGEONS)
public class SecretClicked extends Module {
    @SuppressWarnings("unused")
    private static final SecretClicked instance = new SecretClicked();

    private final BooleanSetting drawBox = new BooleanSetting("Draw Box", true);
    private final NumberSetting timeToStay = new NumberSetting("Time to stay (t)", 0, 100, 20, 1, drawBox::getValue);
    private final ColorSetting fill = new ColorSetting("Fill", Color.GREEN.alpha(255 * 0.4f), drawBox::getValue);
    private final ColorSetting outline = new ColorSetting("Outline", Color.GREEN.copy(), drawBox::getValue);
    private final ColorSetting lockedFill = new ColorSetting("Locked Fill", Color.RED.alpha(255 * 0.4f), drawBox::getValue);
    private final ColorSetting lockedOutline = new ColorSetting("Locked Outline", Color.RED.copy(), drawBox::getValue);
    private final BooleanSetting depth = new BooleanSetting("Depth", false);

    private final BooleanSetting playSound = new BooleanSetting("Play Sound", true);
    private final StringSetting sound = new StringSetting("Sound", "block.note_block.pling");
    private final NumberSetting pitch = new NumberSetting("Pitch", 0.1, 2, 1, 0.1);
    private final NumberSetting volume = new NumberSetting("Volume", 0.1, 5, 1, 0.1);
    private final ButtonSetting testSound = new ButtonSetting("Test Sound", "", this::playSound);

    private final Map<Pos, Secret> clicked = new HashMap<>();
    private Secret last = null;
    private long lastPlayed = 0;

    @SubscribeEvent
    public void onSecret(SecretPickupEvent event) {
        if (!Location.getArea().is(Island.Dungeon) || event.getType() == SecretType.REDSTONE_BLOCK) return;
        if (drawBox.getValue() && !clicked.containsKey(event.getPos())) {
            last = new Secret(event.getPos().getAABB());
            clicked.put(event.getPos(), last);
        }
        if (playSound.getValue() && lastPlayed != EventDispatcher.getClientLifeTime()) {
            lastPlayed = EventDispatcher.getClientLifeTime();
            playSound();
        }
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon) || !"That chest is locked!".equals(event.getString()) || last == null) return;
        last.locked = true;
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || clicked.isEmpty()) return;
        clicked.values().forEach(secret ->
                Renderer3D.addTask(new FilledOutlineBox(secret.box, secret.locked ? lockedFill.getValue() : fill.getValue(), secret.locked ? lockedOutline.getValue() : outline.getValue(), this.depth.getValue()))
        );
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        long now = EventDispatcher.getClientLifeTime();
        long t = timeToStay.getValue().longValue();
        clicked.values().removeIf(s -> now - s.time > t);
    }

    private void playSound() {
        if (mc.player != null) {
            Identifier sound = Identifier.tryParse(this.sound.getValue());
            if (sound == null) return;
            PlayerUtils.playSound(SoundEvent.createVariableRangeEvent(sound), this.pitch.getValue().floatValue(), this.volume.getValue().floatValue());
        }
    }

    private static class Secret {
        private final AABB box;
        private boolean locked;
        private final long time;

        public Secret(AABB aabb) {
            this.box = aabb;
            this.locked = false;
            this.time = EventDispatcher.getClientLifeTime();
        }
    }
}
