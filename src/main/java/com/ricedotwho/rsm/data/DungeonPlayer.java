package com.ricedotwho.rsm.data;

import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

public class DungeonPlayer implements Accessor {
    @Getter
    private DungeonClass dClass;
    @Getter
    private final String name;
    private final UUID uuid;
    @Getter
    private Integer level;
    @Getter
    @Setter
    private Integer secrets;
    @Getter
    private Player player;
    public DungeonPlayer(DungeonClass dClass, Player player, Integer level, Integer secrets) {
        this.dClass = dClass;
        this.player = player;
        this.name = player.getName().getString();
        this.uuid = player.getUUID();
        this.level = level;
        this.secrets = secrets;
    }

    public DungeonClass getMyClass() {
        return this.dClass;
    }

    public Player findPlayer() {
        assert mc.level != null;
        Player p = mc.level.getPlayerByUUID(this.uuid);
        if (p != null) this.player = p;
        return this.player;
    }

    public void update(DungeonClass clazz, int level) {
        this.dClass = clazz;
        this.level = level;
    }

    @Override
    public String toString() {
        return "DungeonPlayer"
                + "{"
                + "dClass=" + this.dClass
                + ",name=" + this.name
                + ",level=" + this.level
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DungeonPlayer that = (DungeonPlayer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
