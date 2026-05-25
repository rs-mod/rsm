package com.ricedotwho.rsm.module.impl.render.opsec;

import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ricedotwho.rsm.component.impl.location.Location.TEAM_PATTERN;

@Getter
@SubModuleInfo(name = "Server ID Hider", alwaysDisabled = false, isEnabled = false)
public class ServerIdHider extends SubModule<OpSec> {
    private static final Pattern SERVER_ID = Pattern.compile("(?<date>\\d{2}/\\d{2}/\\d{2}) (?<server>[Mm]\\d{1,4}[A-Z]{1,4})");
    private final MultiBoolSetting modes = new MultiBoolSetting("Types", List.of("Server ID", "IP"), List.of());
    private final StringSetting replacement = new StringSetting("ID Replacement", "", true, false, () -> modes.get("Server ID"));
    private final StringSetting ipReplacement = new StringSetting("IP replacement", "", true, false, () -> modes.get("IP"));

    public ServerIdHider(OpSec opSec) {
        super(opSec);
        this.registerProperty(modes, replacement, ipReplacement);
    }

    public void onPostHandleSetPlayerTeam(ClientboundSetPlayerTeamPacket packet) {
        if (packet.getParameters().isEmpty()) return;
        ClientboundSetPlayerTeamPacket.Parameters params = packet.getParameters().get();
        String unformatted = ChatFormatting.stripFormatting(params.getPlayerPrefix().getString() + params.getPlayerSuffix().getString());
        if (!TEAM_PATTERN.matcher(packet.getName()).find()) return;

        if (modes.get("Server ID")) {
            Matcher matcher = SERVER_ID.matcher(unformatted);
            if (matcher.find()) {
                assert mc.getConnection() != null;
                PlayerTeam playerTeam = mc.getConnection().scoreboard().getPlayerTeam(packet.getName());
                if (playerTeam == null) return;
                playerTeam.setPlayerPrefix(Component.literal(matcher.group("date")).withStyle(ChatFormatting.GRAY));
                playerTeam.setPlayerSuffix(Component.literal(" " + StringUtils.format(replacement.getValue())));
                return;
            }
        }
        if (modes.get("IP") && unformatted.equals("www.hypixel.net")) {
            assert mc.getConnection() != null;
            PlayerTeam playerTeam = mc.getConnection().scoreboard().getPlayerTeam(packet.getName());
            if (playerTeam == null) return;
            playerTeam.setPlayerPrefix(Component.literal(StringUtils.format(ipReplacement.getValue())));
            playerTeam.setPlayerSuffix(Component.literal(""));
        }
    }
}