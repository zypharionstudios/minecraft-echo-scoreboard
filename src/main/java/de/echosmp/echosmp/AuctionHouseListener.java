package de.echosmp.echosmp;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Verarbeitet alle Klicks und Eingaben im Auktionshaus. */
public final class AuctionHouseListener implements Listener {
    private final AuctionHouseManager auctions;
    private final AuctionHouseMenu menu;
    private final MoneyManager money;

    public AuctionHouseListener(AuctionHouseManager auctions, AuctionHouseMenu menu, MoneyManager money) {
        this.auctions = auctions;
        this.menu = menu;
        this.money = money;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (AuctionHouseMenu.MAIN_TITLE.equals(title)) {
            event.setCancelled(true);
            if (event.getRawSlot() < 45 && event.getRawSlot() >= 0) {
                AuctionListing listing = menu.listingAt(player, event.getRawSlot());
                if (listing == null) return;
                AuctionHouseManager.PurchaseResult result = auctions.buy(player, listing.id());
                switch (result) {
                    case SUCCESS -> player.sendMessage(ChatColor.GREEN + "Gekauft für "
                            + MoneyManager.format(listing.price()) + ".");
                    case OWN_ITEM -> player.sendMessage(ChatColor.RED + "Du kannst dein eigenes Angebot nicht kaufen.");
                    case NOT_ENOUGH_MONEY -> player.sendMessage(ChatColor.RED + "Du hast nicht genug Geld.");
                    case NOT_FOUND -> player.sendMessage(ChatColor.RED + "Dieses Angebot ist nicht mehr verfügbar.");
                }
                menu.openMain(player);
                return;
            }
            switch (event.getRawSlot()) {
                case 45 -> menu.openMain(player);
                case 49 -> { menu.cycleSort(player.getUniqueId()); menu.openMain(player); }
                case 50 -> openSearch(player);
                case 53 -> menu.openOwn(player);
                default -> { }
            }
            return;
        }
        if (AuctionHouseMenu.OWN_TITLE.equals(title)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 53) menu.openAdd(player);
            else if (event.getRawSlot() == 45) menu.openMain(player);
            return;
        }
        if (AuctionHouseMenu.ADD_TITLE.equals(title)) {
            if (event.getRawSlot() == AuctionHouseMenu.CONFIRM_SLOT) {
                event.setCancelled(true);
                ItemStack item = singleItem(event.getInventory());
                if (item == null) {
                    player.sendMessage(ChatColor.RED + "Lege genau ein Item in das Menü.");
                    return;
                }
                for (int slot = 0; slot < AuctionHouseMenu.CONFIRM_SLOT; slot++) event.getInventory().setItem(slot, null);
                menu.openPrice(player, item);
            } else if (event.getRawSlot() >= AuctionHouseMenu.CONFIRM_SLOT) {
                event.setCancelled(true);
            }
            return;
        }
        if (AuctionHouseMenu.PRICE_TITLE.equals(title) && event.getSlotType() == InventoryType.SlotType.RESULT) {
            event.setCancelled(true);
            String text = ((AnvilInventory) event.getInventory()).getRenameText().trim();
            long price;
            try { price = Long.parseLong(text); } catch (NumberFormatException exception) { price = 0; }
            ItemStack item = menu.takePending(player.getUniqueId());
            if (item == null || price < 1) {
                player.sendMessage(ChatColor.RED + "Gib einen gültigen Preis ein.");
                return;
            }
            auctions.create(player, item, price);
            player.sendMessage(ChatColor.GREEN + "Angebot für " + MoneyManager.format(price) + " eingestellt.");
            player.closeInventory();
            return;
        }
        if (AuctionHouseMenu.SEARCH_TITLE.equals(title) && event.getSlotType() == InventoryType.SlotType.RESULT) {
            event.setCancelled(true);
            String search = ((AnvilInventory) event.getInventory()).getRenameText().trim();
            menu.setSearch(player.getUniqueId(), search);
            player.closeInventory();
            menu.openMain(player);
            return;
        }
        if (event.getView().getType() == InventoryType.ANVIL) return;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (AuctionHouseMenu.MAIN_TITLE.equals(title) || AuctionHouseMenu.OWN_TITLE.equals(title)
                || AuctionHouseMenu.PRICE_TITLE.equals(title)) event.setCancelled(true);
        if (AuctionHouseMenu.ADD_TITLE.equals(title)) {
            for (int slot : event.getRawSlots()) if (slot >= AuctionHouseMenu.CONFIRM_SLOT) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (AuctionHouseMenu.ADD_TITLE.equals(event.getView().getTitle())) returnItems(player, event.getInventory(), 26);
        if (AuctionHouseMenu.PRICE_TITLE.equals(event.getView().getTitle())) {
            ItemStack pending = menu.takePending(player.getUniqueId());
            if (pending != null) give(player, pending);
        }
    }

    private void openSearch(Player player) {
        org.bukkit.inventory.Inventory inventory = org.bukkit.Bukkit.createInventory(null, InventoryType.ANVIL,
            AuctionHouseMenu.SEARCH_TITLE);
        inventory.setItem(0, named(Material.PAPER, "Suchbegriff"));
        player.openInventory(inventory);
    }

    private ItemStack singleItem(Inventory inventory) {
        ItemStack found = null;
        for (int slot = 0; slot < AuctionHouseMenu.CONFIRM_SLOT; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) continue;
            if (found != null) return null;
            found = item.clone();
        }
        return found;
    }

    private void returnItems(Player player, Inventory inventory, int end) {
        for (int slot = 0; slot < end; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) { inventory.setItem(slot, null); give(player, item); }
        }
    }

    private void give(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }

    private ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta(); meta.setDisplayName(name); item.setItemMeta(meta);
        return item;
    }
}
