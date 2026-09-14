package de.echosmp.echosmp;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Schützt und verarbeitet das Verkaufsinventar. */
public final class SellListener implements Listener {
    private final SellMenu menu;
    private final MoneyManager money;
    private final Set<UUID> confirmed = new HashSet<>();

    public SellListener(SellMenu menu, MoneyManager money) {
        this.menu = menu;
        this.money = money;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!SellMenu.TITLE.equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getRawSlot() == SellMenu.CONFIRM_SLOT) {
            sell(player, event.getInventory());
            return;
        }
        if (event.getRawSlot() < 25 && event.getRawSlot() >= 0) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!SellMenu.TITLE.equals(event.getView().getTitle())) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 25) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!SellMenu.TITLE.equals(event.getView().getTitle())
                || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (confirmed.remove(player.getUniqueId())) {
            return;
        }
        returnItems(player, event.getInventory());
    }

    private void sell(Player player, Inventory inventory) {
        long total = menu.preview(inventory);
        if (total <= 0L) {
            player.sendMessage(ChatColor.RED + "Lege verkaufbare Blöcke in das Menü.");
            return;
        }
        for (int slot = 0; slot < 25; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType().isBlock()) {
                inventory.setItem(slot, null);
            }
        }
        returnItems(player, inventory);
        confirmed.add(player.getUniqueId());
        money.add(player.getUniqueId(), total);
        player.sendMessage(ChatColor.GREEN + "Verkauft für " + MoneyManager.format(total) + ".");
        player.closeInventory();
    }

    private void returnItems(Player player, Inventory inventory) {
        for (int slot = 0; slot < 25; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                inventory.setItem(slot, null);
                player.getInventory().addItem(item).values()
                        .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
        }
    }
}
