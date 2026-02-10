package com.ricedotwho.rsm.component.impl.location;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.LocationEvent;
import com.ricedotwho.rsm.event.impl.game.ScoreboardEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.ConfigQOL;
import lombok.Getter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.game.*;

import java.util.regex.Pattern;

@Getter
public class Location extends ModComponent {
    private static boolean isHypixel = false;
    private static boolean inSkyblock = false;
    private static Floor floor = Floor.None;
    @Getter
    private static Island area = Island.Unknown;
    @Getter
    private static Floor kuudraTier = Floor.None;
    private static boolean joinSent = false;

    private static final Pattern TEAM_PATTERN = Pattern.compile("^team_(\\d+)$");

    public Location() {
        super("Loc");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String ip = handler.getConnection().getLoggableAddress(true);
            isHypixel = ip.contains("hypixel.") || ip.equals("local");
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isHypixel = false;
            reset();
        });
    }

    private void reset() {
        inSkyblock = false;
        floor = Floor.None;
        area = Island.Unknown;
        kuudraTier = Floor.None;
        joinSent = false;
    }

    public static void setArea(Island island) {
        area = island;
    }

    public static void setArea(String island) {
        setArea(Island.findByName(island));
    }

    @SubscribeEvent
    public void onHyEvent(PacketEvent.Receive event) {
        if (mc.isSingleplayer() || isHypixel || !(event.getPacket() instanceof ClientboundCustomPayloadPacket(
                net.minecraft.network.protocol.common.custom.CustomPacketPayload payload
        )) || !payload.type().equals(BrandPayload.TYPE)) return;
        BrandPayload brandPayload = (BrandPayload) payload;
        if (brandPayload.brand().toLowerCase().contains("hypixel")) {
            isHypixel = true;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onTablist(PacketEvent.Receive event) {
        if(!(event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket packet)) return;

        if(!inSkyblock) {
            if (mc.isSingleplayer()) return;
            reset();
            return;
        }

        for (ClientboundPlayerInfoUpdatePacket.Entry e : packet.entries()) {
            if (e.displayName() == null) continue;
            String text = ChatFormatting.stripFormatting(e.displayName().getString().trim());

            if (text.startsWith("Area: ") || text.startsWith("Dungeon: ")) {
                Island newArea = Island.findByName(text);
                Island oldArea = area;
                if(!newArea.equals(oldArea)) {
                    area = newArea;
                    new LocationEvent.Changed(newArea, oldArea).post();
                }
            }
        }
    }

    public static Floor getFloor() {
        if (Minecraft.getInstance().isSingleplayer() && RSM.getModule(ConfigQOL.class).isForceSkyblock()) return Floor.F7;
        return floor;
    }

    // this only works on 1.8 servers with viaversion (dungeonsim)
    @SubscribeEvent
    public void onSetScore(PacketEvent.Receive event) {
        if(!(event.getPacket() instanceof ClientboundSetScorePacket packet) || !inSkyblock) return;
        String value = ChatFormatting.stripFormatting(packet.owner());
        if (value.contains("The Catacombs")) {
            floor = Floor.findByName(value.split("\\(")[1].split("\\)")[0]);
            dungeonJoined();
        } else if(value.contains("Kuudra's Hollow (")) {
            kuudraTier = Floor.findByName(value.split("\\(")[1].split("\\)")[0]);
        }
    }

    @SubscribeEvent
    public void onSetTeam(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetPlayerTeamPacket packet) || packet.getParameters().isEmpty()) return;
        ClientboundSetPlayerTeamPacket.Parameters params = packet.getParameters().get();
        if (TEAM_PATTERN.matcher(packet.getName()).find()) {
            String formatted = params.getPlayerPrefix().getString() + params.getPlayerSuffix().getString();
            String unformatted = ChatFormatting.stripFormatting(formatted);
            if (unformatted.contains("The Catacombs")) {
                floor = Floor.findByName(unformatted.split("\\(")[1].split("\\)")[0]);
                dungeonJoined();
            } else if(unformatted.contains("Kuudra's Hollow (")) {
                kuudraTier = Floor.findByName(unformatted.split("\\(")[1].split("\\)")[0]);
            }
            new ScoreboardEvent(formatted, unformatted).post();
        }
    }

    @SubscribeEvent
    public void onScoreboardObjective(PacketEvent.Receive event) {
        if(!(event.getPacket() instanceof ClientboundSetObjectivePacket packet) || !isHypixel) return;
        if(ChatFormatting.stripFormatting(packet.getDisplayName().getString()).contains("SKYBLOCK")) {
            inSkyblock = true;
        }
    }

    @SubscribeEvent
    public void onLocation(LocationEvent.Changed event) {
        if (event.getNewIsland().is(Island.Dungeon)) {
            dungeonJoined();
        }
    }

    private void dungeonJoined() {
        if(!joinSent && floor != Floor.None && area.is(Island.Dungeon)) {
            new DungeonEvent.Joined(floor).post();
            joinSent = true;
        }
    }
}
