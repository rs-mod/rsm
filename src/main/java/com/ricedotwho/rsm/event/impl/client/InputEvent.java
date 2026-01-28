package com.ricedotwho.rsm.event.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputEvent extends Event {
    private final InputConstants.Key key;
}
