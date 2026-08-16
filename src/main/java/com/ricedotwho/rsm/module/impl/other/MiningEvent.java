package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.module.api.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import com.ricedotwho.rsm.module.api.settings.impl.StringSetting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Mining Event", id = "mining-event", category = Category.OTHER)
public class MiningEvent extends Module {
    @SuppressWarnings("unused")
    private static final MiningEvent instance = new MiningEvent();

    private final MultiBoolSetting events = new MultiBoolSetting("Events", List.of("Mithril Gourmand", "Raffle", "2x Powder", "Goblin Raid"), List.of("Mithril Gourmand"));
    private final StringSetting sound = new StringSetting("Sound", "block.note_block.pling");
    private final NumberSetting<Float> volume = new NumberSetting<>("Volume", 0f, 1f, 1f, 0.01f);
    private final NumberSetting<Float> pitch = new NumberSetting<>("Pitch", 0f, 1f, 1f, 0.01f);
    private final NumberSetting<Integer> titleDuration = new NumberSetting<>("Title Duration", 0, 5000, 2500, 100);

    private final NumberSetting<Integer> amount = new NumberSetting<>("Amount", 1, 50, 20, 1);
    private final NumberSetting<Integer> increment = new NumberSetting<>("Increment", 1, 20, 2, 1);

    private final Pattern eventPattern = Pattern.compile(
            "⚑ The (.*) event starts in 20 seconds!\n" +
                    " (Click here to teleport to Garry and prepare!|This is a passive event! It's happening everywhere in the Dwarven Mines!)"
    );

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.DwarvenMines) && !Location.getArea().is(Island.CrystalHollows) || mc.player == null) return;
        Matcher matcher = eventPattern.matcher(event.getString());
        if (!matcher.find()) return;
        String theEvent = matcher.group(1);

        if (this.getEvents().get(theEvent)) {
            if (this.getTitleDuration().getValue().longValue() != 0) Hud.showTitle(theEvent + "!", Color.MINECRAFT_AQUA, this.getTitleDuration().getValue());
            playSounds();
        }
    }

    public void playSounds() {
        Identifier sound = Identifier.tryParse(this.sound.getValue());
        if (sound == null) return;
        SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
        for (int i = 0; i < this.getAmount().getValue(); i += this.getIncrement().getValue()) {
            Scheduler.tick(i, () -> PlayerUtils.playSound(event, this.pitch.getValue(), this.volume.getValue()));
        }
    }
}
