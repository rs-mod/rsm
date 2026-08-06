package com.ricedotwho.rsm.data;

import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import lombok.Setter;
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
    @Getter
    private boolean dead = false;

    public DungeonPlayer(DungeonClass dClass, Player player, Integer level, Integer secrets) {
        this.dClass = dClass;
        this.player = player;
        this.name = player.getName().getString();
        this.uuid = player.getGameProfile().id();
        this.level = level;
        this.secrets = secrets;
    }

    public DungeonPlayer(DungeonClass dClass, String name, Integer level, Integer secrets) {
        this.dClass = dClass;
        this.player = null;
        this.name = name;
        this.uuid = null;
        this.level = level;
        this.secrets = secrets;
    }

    public Player findPlayer() {
        assert mc.level != null;
        Player p = mc.level.getPlayerByUUID(this.uuid);
        if (p != null) this.player = p;
        return this.player;
    }

    public void update(DungeonClass clazz, int level, boolean dead) {
        if (!clazz.equals(DungeonClass.NONE)) this.dClass = clazz;
        if (level != 0) this.level = level;
        this.dead = dead;
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
        if (!(o instanceof DungeonPlayer that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
