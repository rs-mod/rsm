package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.type.Vec2i;
import lombok.*;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Door {
    private final Vec2i position;
    private final Vec2i array;
    private DoorType type;
    private boolean opened;
    private final RoomRotation rotation;
    final ArrayList<UniqueRoom> connectedTo;

    public Door(Vec2i position, DoorType type, RoomRotation rotation, ArrayList<UniqueRoom> connections) {
        this(position, new Vec2i((position.x() - DungeonScanner.START) / 32, (position.y() - DungeonScanner.START) / 32), type, false, rotation, connections);
    }

    void setOpened(boolean value) {
        if (value == this.opened) return;
        this.opened = value;
        if (value && (this.type == DoorType.BLOOD || this.type == DoorType.WITHER)) {
            this.connectedTo.forEach(uni -> uni.setOnBloodRush(false));
        }
    }

    void setType(DoorType type) {
        this.type = type;
        if (type == DoorType.BLOOD || type == DoorType.WITHER) {
            this.connectedTo.forEach(uni -> uni.setOnBloodRush(true));
        }
    }

    @Override
    public String toString() {
        return "Door{" +
                "position=" + this.position +
                ",type=" + this.type +
                ",opened=" + this.opened + "}";
    }

    @Override
    public int hashCode() {
        return this.array.hashCode();
    }
}
