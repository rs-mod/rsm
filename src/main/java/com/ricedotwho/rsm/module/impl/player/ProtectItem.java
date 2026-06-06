package com.ricedotwho.rsm.module.impl.player;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.notification.Notification;
import com.ricedotwho.rsm.component.impl.notification.NotificationComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWord;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.im.InputContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ModuleInfo(aliases = "Protect Item", id = "ProtectItem", category = Category.PLAYER)
public class ProtectItem extends Module {
    private static final SaveSetting<Set<String>> data = new SaveSetting<>("Protected", "player", "protected_items.json", HashSet::new, new TypeToken<Set<String>>(){}.getType());
    private static final BooleanSetting starred = new BooleanSetting("Starred", true);
    private static final BooleanSetting recom = new BooleanSetting("Recom", false);

    public ProtectItem() {
        this.registerProperty(
                data,
                starred,
                recom
        );
    }

    @SubscribeEvent
    public void onKey(KeyInputEvent.Press event) {
        if (mc.player == null || Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) return;
        if (mc.options.keyDrop.matches(event.getKeyEvent()) && isProtected(mc.player.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onClick(GuiEvent.SlotClick event) {
        if (mc.player == null
                || !(mc.screen instanceof ContainerScreen container)
                || !(mc.player.containerMenu instanceof ChestMenu)
        ) return;
        // may throw indexoutofbounds i guess...
        ItemStack item = event.getSlot() == -999 ? container.getMenu().getCarried() : container.getMenu().getSlot(event.getSlot()).getItem();
        if (!isProtected(item)) return;
        if (event.getActionType() == ContainerInput.THROW || inSellMenu(container)) {
            event.setCancelled(true);
        }
    }

    private boolean inSellMenu(ContainerScreen container) {
        if (container.getMenu().slots.size() < 54) return false;
        ItemStack sell = container.getMenu().getSlot(54).getItem();
        return sell.is(Items.HOPPER) || ItemUtils.getCleanLore(sell).contains("Click to buyback");
    }

    public static boolean isProtected(ItemStack item) {
        if (starred.getValue() && ItemUtils.getUpgradeLevel(item) > 0
                || recom.getValue() && ItemUtils.getRarityUpgrades(item) > 0) return true;

        String uuidOrId = ItemUtils.getCustomData(item).getString(ItemUtils.UUID_KEY).orElse(ItemUtils.getID(item));
        return data.getValue().contains(uuidOrId);
    }

    public static void addOrRemove(ItemStack item, boolean chat) {
        String uuidOrId = ItemUtils.getCustomData(item).getString(ItemUtils.UUID_KEY).orElse(ItemUtils.getID(item));
        if (data.getValue().contains(uuidOrId)) {
            data.getValue().remove(uuidOrId);
            if (chat) ChatUtils.chat("Protecting \"%s\"", item.getHoverName().getString());
        } else {
            data.getValue().add(uuidOrId);
            if (chat) ChatUtils.chat("No longer protecting \"%s\"", item.getHoverName().getString());
        }
        data.save();
    }
}
