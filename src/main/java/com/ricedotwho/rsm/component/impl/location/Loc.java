package com.ricedotwho.rsm.component.impl.location;

import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.LocationEvent;
import com.ricedotwho.rsm.event.impl.game.WorldEvent;
import lombok.Getter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.game.*;

import java.awt.*;

@Getter
public class Loc extends ModComponent {
    public static boolean isHypixel = false;
    public static boolean inSkyblock = false;
    public static Floor floor = Floor.None;
    public static Island area = Island.Unknown;
    public static Floor kuudraTier = Floor.None;
    private static boolean joinSent = false;

    public Loc() {
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
    public void onUnload(WorldEvent.Unload event) {
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

    @SubscribeEvent
    public void onScoreboard(PacketEvent.Receive event) {
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
    public void onScoreboardObjective(PacketEvent.Receive event) {
        if(!(event.getPacket() instanceof ClientboundSetObjectivePacket packet) || !isHypixel) return;
        if(ChatFormatting.stripFormatting(packet.getDisplayName().getString()).contains("SKYBLOCK")) {
            inSkyblock = true;
        }
    }

    @SubscribeEvent
    public void onLocation(LocationEvent.Changed event) {
        if (event.getNewIsland().equals(Island.Dungeon)) {
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
