package com.ricedotwho.rsm.module.impl.player;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

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
    public void onKeyPress(KeyInputEvent.Press event) {
        if (mc.player == null || Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) return;
        if (mc.options.keyDrop.matches(event.getKeyEvent()) && isProtected(mc.player.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onKeyRepeat(KeyInputEvent.Repeat event) {
        if (mc.player == null || Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) return;
        if (mc.options.keyDrop.matches(event.getKeyEvent()) && isProtected(mc.player.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onClick(GuiEvent.SlotClick event) {
        if (mc.player == null) return;
        AbstractContainerMenu menu = mc.player.containerMenu;
        int slot = event.getSlot();
        // something weird with moving the mouse and clicking at the same time makes it do a click with slot -999 QUICK_CRAFT, if u cancel it its really fucking annoying
        if (slot >= menu.slots.size() || slot == -999 && event.getActionType() == ClickType.QUICK_CRAFT) return;
        ItemStack item = slot < 0 ? menu.getCarried() : menu.getSlot(event.getSlot()).getItem();
        if (!isProtected(item)) return;
        if (event.getActionType() == ClickType.THROW || slot == -999 || inSellMenu(menu)) {
            event.setCancelled(true);
        }
    }

    private boolean inSellMenu(AbstractContainerMenu menu) {
        return menu.getItems().stream().anyMatch(it -> it.is(Items.HOPPER) && it.getHoverName().getString().contains("Sell Item")
                || ItemUtils.getCleanLore(it).contains("Click to buyback"));
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
            if (chat) ChatUtils.chat("No longer protecting \"%s\"", item.getHoverName().getString());
        } else {
            data.getValue().add(uuidOrId);
            if (chat) ChatUtils.chat("Protecting \"%s\"", item.getHoverName().getString());
        }
        data.save();
    }
}