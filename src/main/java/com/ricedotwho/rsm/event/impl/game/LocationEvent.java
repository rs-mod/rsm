package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public class LocationEvent extends Event {
    @Getter
    public static class Changed extends LocationEvent {
        private final Island newIsland;
        private final Island oldIsland;
        public Changed(Island newIsland, Island oldIsland) {
            this.newIsland = newIsland;
            this.oldIsland = oldIsland;
        }
    }
}
