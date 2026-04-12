package com.ricedotwho.rsm.module.impl.render.opsec;

import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.StringUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ricedotwho.rsm.component.impl.location.Location.TEAM_PATTERN;

@Getter
@SubModuleInfo(name = "ServerID Hider", alwaysDisabled = false, isEnabled = false)
public class ServerIdHider extends SubModule<OpSec> {
    private static final Pattern SERVER_ID = Pattern.compile("(?<date>\\d{2}/\\d{2}/\\d{2}) (?<server>[Mm]\\d{1,4}[A-Z]{1,4})");
    private final StringSetting replacement = new StringSetting("Replacement", "");

    public ServerIdHider(OpSec opSec) {
        super(opSec);
        this.registerProperty(replacement);
    }

    public void onPostHandleSetPlayerTeam(ClientboundSetPlayerTeamPacket packet) {
        if (packet.getParameters().isEmpty()) return;
        ClientboundSetPlayerTeamPacket.Parameters params = packet.getParameters().get();
        String unformatted = ChatFormatting.stripFormatting(params.getPlayerPrefix().getString() + params.getPlayerSuffix().getString());
        if (!TEAM_PATTERN.matcher(packet.getName()).find()) return;
        Matcher matcher = SERVER_ID.matcher(unformatted);
        if (matcher.find()) {
            assert mc.getConnection() != null;
            PlayerTeam playerTeam = mc.getConnection().scoreboard().getPlayerTeam(packet.getName());
            if (playerTeam == null) return;
            playerTeam.setPlayerPrefix(Component.literal(matcher.group("date")).withStyle(ChatFormatting.GRAY));
            playerTeam.setPlayerSuffix(Component.literal(" " + StringUtils.format(replacement.getValue())));
        }
    }
}