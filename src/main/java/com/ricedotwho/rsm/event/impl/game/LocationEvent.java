package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.location.Island;
import lombok.Getter;

public sealed abstract class LocationEvent extends Event {
    @Getter
    public final static class Changed extends LocationEvent {
        private final Island newIsland;
        private final Island oldIsland;
        public Changed(Island newIsland, Island oldIsland) {
            this.newIsland = newIsland;
            this.oldIsland = oldIsland;
        }
    }
}
