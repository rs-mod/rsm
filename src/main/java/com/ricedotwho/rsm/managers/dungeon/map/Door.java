package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.type.Vec2i;
import lombok.*;
import org.joml.Vector2i;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class Door {
    private final Vec2i position;
    @Setter(value = AccessLevel.PACKAGE)
    private DoorType type;
    @Setter
    private boolean opened;
    @Setter
    private RoomState state;
    private final RoomRotation rotation;
    final ArrayList<UniqueRoom> connectedTo;

    @Override
    public String toString() {
        return "Door{" +
                "position=" + this.position +
                ",type=" + type +
                ",opened=" + opened +
                ",state=" + state + "}";
    }
}
