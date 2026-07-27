package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.EventComponent;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.Secret;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Secret Clicked", id = "secret-clicked", category = Category.DUNGEONS)
public class SecretClicked extends Module {

    private final BooleanSetting drawBox = new BooleanSetting("Draw Box", true);
    private final BooleanSetting boxInBoss = new BooleanSetting("Box In Boss", true, drawBox::getValue);
    private final NumberSetting timeToStay = new NumberSetting("Time to stay (t)", 0, 100, 20, 1, drawBox::getValue);
    private final ColourSetting fill = new ColourSetting("Fill", Colour.GREEN.alpha(255 * 0.4f), drawBox::getValue);
    private final ColourSetting outline = new ColourSetting("Outline", Colour.GREEN.copy(), drawBox::getValue);
    private final ColourSetting lockedFill = new ColourSetting("Locked Fill", Colour.RED.alpha(255 * 0.4f), drawBox::getValue);
    private final ColourSetting lockedOutline = new ColourSetting("Locked Outline", Colour.RED.copy(), drawBox::getValue);
    private final BooleanSetting depth = new BooleanSetting("Depth", false);

    private final BooleanSetting playSound = new BooleanSetting("Play Sound", true);
    private final BooleanSetting soundInBoss = new BooleanSetting("Sound In Boss", true, playSound::getValue);
    private final StringSetting sound = new StringSetting("Sound", "block.note_block.pling");
    private final NumberSetting pitch = new NumberSetting("Pitch", 0.1, 2, 1, 0.1);
    private final NumberSetting volume = new NumberSetting("Volume", 0.1, 5, 1, 0.1);
    private final ButtonSetting testSound = new ButtonSetting("Test Sound", "", this::playSound);

    private final Map<Pos, Secret> clicked = new HashMap<>();
    private Secret last = null;
    private long lastPlayed = 0;

    public SecretClicked() {
        this.registerProperty(
                drawBox,
                boxInBoss,
                timeToStay,
                fill,
                outline,
                lockedFill,
                lockedOutline,
                depth,
                playSound,
                soundInBoss,
                sound,
                pitch,
                volume,
                testSound
        );
    }

    @SubscribeEvent
    public void onSecret(SecretPickupEvent event) {
        if (drawBox.getValue() && (Dungeon.isInBoss() && boxInBoss.getValue()) && !clicked.containsKey(event.getPos())) {
            last = new Secret(event.getPos().getAABB());
            clicked.put(event.getPos(), last);
        }
        if (playSound.getValue() && (Dungeon.isInBoss() && soundInBoss.getValue()) && lastPlayed != EventComponent.getClientLifeTime()) {
            lastPlayed = EventComponent.getClientLifeTime();
            playSound();
        }
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon) || !"That chest is locked!".equals(event.getString()) || last == null) return;
        last.locked = true;
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (!Location.getArea().is(Island.Dungeon) || clicked.isEmpty()) return;
        clicked.values().forEach(secret ->
                Renderer3D.addTask(new FilledOutlineBox(secret.box, secret.locked ? lockedFill.getValue() : fill.getValue(), secret.locked ? lockedOutline.getValue() : outline.getValue(), this.depth.getValue()))
        );
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        long now = EventComponent.getClientLifeTime();
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
            this.time = EventComponent.getClientLifeTime();
        }
    }
}
