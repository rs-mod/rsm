package com.ricedotwho.rsm.location;

import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.LocationEvent;
import com.ricedotwho.rsm.event.impl.game.ScoreboardEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.managers.dungeon.map.handler.Dungeon;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import org.apache.commons.lang3.EnumUtils;

import java.util.HashMap;
import java.util.regex.Pattern;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Getter
@Register
@UtilityClass
public class Location {
    private boolean inSkyblock = false;
    private Floor floor = Floor.None;
    private Island area = Island.Unknown;
    @Getter
    private Floor kuudraTier = Floor.None;

    public static final Pattern TEAM_PATTERN = Pattern.compile("^team_(\\d+)$");

    @Init
    private void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> reset());

        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);

        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, packet -> {
            packet.getServerType().ifPresent(serverType -> {
                inSkyblock = "SKYBLOCK".equals(serverType.getName());
                ChatUtils.dev("Area: %s", packet.getMode().orElse(null));
                Island newArea = packet.getMode().isEmpty() ? Island.Unknown : Island.getByID(packet.getMode().get());
                Island oldArea = area;
                if (!newArea.is(oldArea)) {
                    area = newArea;
                    new LocationEvent.Changed(newArea, oldArea).post();
                }
            });
        });
    }

    private void reset() {
        inSkyblock = false;
        floor = Floor.None;
        area = Island.Unknown;
        kuudraTier = Floor.None;
    }

    public void setArea(Island island) {
        area = island;
    }

    public void setArea(String island) {
        setArea(Island.findByName(island));
    }

    public boolean isForceSkyblock() {
        return ClickGUI.getInstance().getForceSkyBlock().getValue();
    }

    public Island getArea() {
        if (isForceSkyblock()) return Island.Dungeon;
        return area;
    }

    public boolean isInSkyblock() {
        if (isForceSkyblock()) return true;
        return inSkyblock;
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    public Floor getFloor() {
        if (mc.isSingleplayer() && isForceSkyblock()) return Floor.F7;
        return floor;
    }

    // this only works on 1.8 servers with viaversion (dungeonsim)
    @SubscribeEvent
    private void onSetScore(PacketEvent.MainReceivePre event) {
        if (!(event.getPacket() instanceof ClientboundSetScorePacket packet) || !inSkyblock) return;
        String value = ChatFormatting.stripFormatting(packet.owner());
        if (value.contains("The Catacombs")) {
            floor = Floor.findByName(value.split("\\(")[1].split("\\)")[0]);
        } else if(value.contains("Kuudra's Hollow (")) {
            kuudraTier = Floor.findByName(value.split("\\(")[1].split("\\)")[0]);
        } else if (value.contains("Time Elapsed: ") && area.is(Island.Dungeon) && !Dungeon.isStarted()) {
            Dungeon.setStarted(true);
        }
    }

    @SubscribeEvent
    private void onSetTeam(PacketEvent.MainReceivePre event) {
        if (!(event.getPacket() instanceof ClientboundSetPlayerTeamPacket packet) || packet.getParameters().isEmpty()) return;
        ClientboundSetPlayerTeamPacket.Parameters params = packet.getParameters().get();
        if (TEAM_PATTERN.matcher(packet.getName()).find()) {
            String formatted = params.getPlayerPrefix().getString() + params.getPlayerSuffix().getString();
            String unformatted = ChatFormatting.stripFormatting(formatted);
            if (unformatted.contains("The Catacombs")) {
                Floor temp = Floor.findByName(unformatted.split("\\(")[1].split("\\)")[0]);
                if (floor == temp) return;
                floor = temp;
                Island oldArea = area;
                if (!Island.Dungeon.equals(oldArea)) {
                    area = Island.Dungeon;
                    new LocationEvent.Changed(Island.Dungeon, oldArea).post();
                }
                new DungeonEvent.Joined(floor).post();
            } else if(unformatted.contains("Kuudra's Hollow (")) {
                kuudraTier = Floor.findByName(unformatted.split("\\(")[1].split("\\)")[0]);
            } else if (unformatted.contains("Time Elapsed: ") && area.is(Island.Dungeon) && !Dungeon.isStarted()) {
                Dungeon.setStarted(true);
            }
            new ScoreboardEvent(formatted, unformatted).post();
        }
    }

    public int fakeFloor() {
        if (ClickGUI.getInstance().getForceSkyBlock().getValue()) return 7;
        return getFloor().getIndex() > 7 ? getFloor().getIndex() - 7 : getFloor().getIndex();
    }
}
