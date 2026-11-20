package com.ricedotwho.rsm.module.impl.player.chat;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;
import com.ricedotwho.rsm.module.impl.player.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.regex.Pattern;

@Getter
@SubModuleInfo(name = "Hide Drops", alwaysDisabled = false)
public class NoMFDrops extends SubModule<Chat> {
    private static final List<Sound> DROP_SOUND = List.of(
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.8f, 0.5873016f),
            new Sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 0.6984127f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.4f, 0.5873016f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.8f, 0.7936508f),
            new Sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 0.93650794f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.4f, 0.7936508f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.8f, 1.0476191f),
            new Sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 1.2539682f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.4f, 1.0476191f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.8f, 1.1746032f),
            new Sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 1.4126984f),
            new Sound(SoundEvents.NOTE_BLOCK_PLING, 0.4f, 1.1746032f)
    );
    private final EnumSetSetting<Drop> drops = new EnumSetSetting<>("Drops", Drop.class, List.of());
    private final BooleanSetting dungeon = new BooleanSetting("Dungeon Only", false);

    public NoMFDrops(Chat chat) {
        super(chat);
	}

    private int index = -1;

    @SubscribeEvent
    public void onWorld(WorldEvent.Load event) {
        index = -1;
    }

    @SubscribeEvent
    private void onSoundPlay(PacketEvent.MainReceivePre event, ClientboundSoundPacket packet) {
        if (index == -1) return;
        var next = DROP_SOUND[index];
        if (!next.event.equals(packet.getSound().value()) || packet.getVolume() != next.volume || packet.getPitch() != next.pitch) return;
        index++;
        if (index == DROP_SOUND.size()) index = 0;
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Show event) {
        if (dungeon.getValue() && !Location.getArea().is(Island.Dungeon)) return;
        if (drops.none(it -> it.pattern.matcher(event.getString()).find())) return;
        event.setCancelled(true);
        index = 0;
    }

    private record Sound(SoundEvent event, float volume, float pitch) {
        private Sound(Holder<SoundEvent> event, float volume, float pitch) {
            this(event.value(), volume, pitch);
        }
    }

    @AllArgsConstructor
    private enum Drop {
        RARE(Pattern.compile("^RARE DROP! .* \\(\\+\\d+\uE01A Magic Find\\)$")),
        VERY_RARE(Pattern.compile("^VERY RARE DROP! .* \\(\\+\\d+\uE01A Magic Find\\)$")),
        CRAZY_RARE_DROP(Pattern.compile("^CRAZY RARE DROP! .* \\(\\+\\d+\uE01A Magic Find\\)$")),
        INSANE_DROP(Pattern.compile("^INSANE DROP! .* \\(\\+\\d+\uE01A Magic Find\\)$"));
        final Pattern pattern;
    }
}
