package com.ricedotwho.rsm.event.impl.world;

import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.event.Event;
import net.minecraft.client.multiplayer.ClientLevel;

public class WorldEvent extends Event {

    public static class Load extends WorldEvent {
        public final ClientLevel level;
        public Load(ClientLevel level) {
            this.level = level;
        }
    }

/*    public static class Unload extends WorldEvent {

    }*/
}
