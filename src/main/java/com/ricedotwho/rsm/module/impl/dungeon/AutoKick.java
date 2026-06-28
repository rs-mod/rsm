package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Scheduler;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.api.DungeonData;
import com.ricedotwho.rsm.utils.api.HyApi;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Auto Kick", id = "AutoKick", category = Category.DUNGEONS)
public class AutoKick extends Module {
    // doc pls fix ur slider not able to put exact number in :( i would prefer to use dv tbh

    private final Map<String, DungeonData> PB_CACHE = new HashMap<>();
    private static final Pattern PF = Pattern.compile("^Party Finder > (?:\\[.{1,7}])? ?(.{1,16}) joined the dungeon group! \\(.*\\)$");
    private static final Pattern INVITE = Pattern.compile("^(?:\\[.{1,7}])? ?(\\w{1,16}) joined the party.$");
    private static final Pattern MMSS = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    private final ModeSetting floor = new ModeSetting("Floor", "M7", List.of("F1", "F2", "F3", "F4", "F5", "F6", "F7", "M1", "M2", "M3", "M4", "M5", "M6", "M7"));
    private final StringSetting time = new StringSetting("Time", "5:00", null, false, false, 4, null);
    private final NumberSetting secrets = new NumberSetting("Secrets", 0, 100000, 0,1000);
    private final NumberSetting secretsPerRun = new NumberSetting("S/R", 0, 50, 0,0.1);
    private final NumberSetting magicalPower = new NumberSetting("MP", 0, 2500, 0,50);
    private final BooleanSetting fromInvite = new BooleanSetting("Invites", false);
    private final BooleanSetting message = new BooleanSetting("Message", false);
    private final StringSetting kickMessage = new StringSetting("Kick Message", "kicking {name} for {floor} {reason} {value} < {req}", this.message::getValue, false, false, 64, null);

    public AutoKick() {
        this.registerProperty(
                floor,
                time,
                secrets,
                secretsPerRun,
                magicalPower,
                fromInvite,
                message,
                kickMessage
        );
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.DungeonHub)) return;
        Matcher matcher;
        if ((matcher = PF.matcher(event.getString())).find() || fromInvite.getValue() && (matcher = INVITE.matcher(event.getString())).find()) {
            check(matcher.group(1));
        }
    }

    private void check(String ign) {
        DungeonData dat0 = PB_CACHE.get(ign);
        if (dat0 != null) {
            runAutokick(dat0, ign);
            return;
        }
        EXECUTOR.submit(() -> {
            try {
                DungeonData dat = HyApi.getPbs(ign);
                if (dat == null) return;
                PB_CACHE.put(ign, dat);
                runAutokick(dat, ign);
            } catch (Throwable t) {
                ChatUtils.chat("Error while getting autokick data %s", t.getMessage());
                RSM.getLogger().error("Error getting autokick data", t);
            }
        });
    }

    private void runAutokick(DungeonData data, String ign) {
        Floor f = EnumUtils.getEnum(Floor.class, this.floor.getValue());
        long pb = data.pbs().get(f).sPlus();
        long req = mmssToMillis(this.time.getValue());

        String kickMsg;
        String value;
        String requirement;
        Reason reason;
        if (pb == -1 || pb > req) {
            value = pb == -1 ? "no s+" : NumberUtils.millisToMMSS(pb);
            requirement = this.time.getValue();
            reason = Reason.PB;
        }
        else if (data.secrets() < this.secrets.getValue().intValue()) {
            value = String.valueOf(data.secrets());
            requirement = String.valueOf(this.secrets.getValue().intValue());
            reason = Reason.SECRETS;
        }
        else if (data.secretsPerRun() < this.secretsPerRun.getValue().floatValue()) {
            value = String.valueOf(data.secretsPerRun());
            requirement = String.valueOf(this.secretsPerRun.getValue().floatValue());
            reason = Reason.SR;
        }
        else if (data.mp() < this.magicalPower.getValue().intValue()) {
            value = String.valueOf(data.mp());
            requirement = String.valueOf(this.magicalPower.getValue().intValue());
            reason = Reason.MP;
        }
        else {
            return;
        }

        kickMsg = StringUtils.format(this.kickMessage.getValue(), Map.of("{name}", ign, "{floor}", f.getName().toLowerCase(), "{reason}", reason.value, "{value}", value, "{req}", requirement));
        if (this.message.getValue()) {
            mc.getConnection().sendCommand("pc " + kickMsg);
            Scheduler.schedule(ClientTickEvent.Start.class, 8, () -> mc.getConnection().sendCommand("p kick " + ign));
        } else {
            ChatUtils.chat(kickMsg);
            mc.getConnection().sendCommand("p kick " + ign);
        }
    }

    private long mmssToMillis(String mmss) {
        Matcher matcher = MMSS.matcher(mmss);
        if (!matcher.find()) {
            return Integer.MAX_VALUE;
        }
        int m = Integer.parseInt(matcher.group(1));
        int s = Integer.parseInt(matcher.group(2));
        return ((m * 60L) + s) * 1000L;
    }

    private enum Reason {
        PB("pb"),
        MP("mp"),
        SECRETS("secrets"),
        SR("s/r");

        private final String value;
        Reason(String value) {
            this.value = value;
        }
    }
}
