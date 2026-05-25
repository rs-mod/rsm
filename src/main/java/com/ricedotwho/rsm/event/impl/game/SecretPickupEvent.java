package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.Component;

@Getter
@AllArgsConstructor
public class SecretPickupEvent extends Event {
    private final Pos pos;
    private final SecretType type;
}
