package de.echosmp.echosmp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Verarbeitet alle Klicks und Eingaben im Auktionshaus. */
public final class AuctionHouseListener implements Listener {
    private final AuctionHouseManager auctions;
    private final AuctionHouseMenu menu;
    private final MoneyManager money;
    private final SignInputManager signs;

    public AuctionHouseListener(AuctionHouseManager auctions, AuctionHouseMenu menu, MoneyManager money,
                                SignInputManager signs) {
        this.auctions = auctions;
        this.menu = menu;
        this.money = money;
        this.signs = signs;
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
            if (event.getRawSlot() >= 0 && event.getRawSlot() < 45) {
                AuctionListing listing = menu.ownListingAt(player, event.getRawSlot());
                if (listing != null && auctions.takeOwn(player, listing.id()) != null) {
                    give(player, listing.item());
                    player.sendMessage(ChatColor.GREEN + "Dein Angebot wurde entfernt und zurückgegeben.");
                    menu.openOwn(player);
                }
            } else if (event.getRawSlot() == 53) {
                menu.openAdd(player);
            }
            else if (event.getRawSlot() == 45) menu.openMain(player);
            return;
        }
        if (AuctionHouseMenu.ADD_TITLE.equals(title)) {
            int rawSlot = event.getRawSlot();
            if (rawSlot == AuctionHouseMenu.CONFIRM_SLOT) {
                event.setCancelled(true);
                ItemStack item = singleItem(event.getInventory());
                if (item == null) {
                    player.sendMessage(ChatColor.RED + "Lege genau einen Item-Stack ein.");
                    return;
                }
                for (int slot = 0; slot < 25; slot++) event.getInventory().setItem(slot, null);
                menu.setPending(player, item);
                if (!signs.open(player, SignInputManager.Mode.PRICE, this::finishPrice)) {
                    menu.takePending(player.getUniqueId());
                    give(player, item);
                }
                return;
            }
            if (rawSlot == 25) {
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick() && inputSlotCount(event.getInventory()) > 0) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Es darf nur ein Item-Stack eingestellt werden.");
            } else if (event.getRawSlot() >= 0 && event.getRawSlot() < AuctionHouseMenu.CONFIRM_SLOT
                    && hasDifferentInputItem(event.getInventory(), event.getRawSlot())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Es darf nur ein Item-Stack eingestellt werden.");
            }
            return;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (AuctionHouseMenu.MAIN_TITLE.equals(title) || AuctionHouseMenu.OWN_TITLE.equals(title)) event.setCancelled(true);
        if (AuctionHouseMenu.ADD_TITLE.equals(title)) {
            for (int slot : event.getRawSlots()) {
                if (slot == 25 || slot == AuctionHouseMenu.CONFIRM_SLOT
                    || (inputSlotCount(event.getInventory()) > 0 && slot >= 0 && slot < 25)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getRawSlots().stream().filter(slot -> slot >= 0 && slot < 25).count() > 1) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (AuctionHouseMenu.ADD_TITLE.equals(event.getView().getTitle())) returnItems(player, event.getInventory(), 25);
    }

    private void openSearch(Player player) {
        signs.open(player, SignInputManager.Mode.SEARCH, (target, search) -> {
            menu.setSearch(target.getUniqueId(), search);
            menu.openMain(target);
        });
    }

    private void finishPrice(Player player, String text) {
        long price;
        try { price = Long.parseLong(text.replace(" ", "")); }
        catch (NumberFormatException exception) { price = 0; }
        ItemStack item = menu.takePending(player.getUniqueId());
        if (item == null || price < 1) {
            player.sendMessage(ChatColor.RED + "Gib eine gültige ganze Zahl als Preis ein.");
            if (item != null) give(player, item);
            return;
        }
        auctions.create(player, item, price);
        player.sendMessage(ChatColor.GREEN + "Angebot für " + MoneyManager.format(price) + " eingestellt.");
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

    private int inputSlotCount(Inventory inventory) {
        int count = 0;
        for (int slot = 0; slot < AuctionHouseMenu.CONFIRM_SLOT; slot++) {
            if (inventory.getItem(slot) != null) count++;
        }
        return count;
    }

    private boolean hasDifferentInputItem(Inventory inventory, int slot) {
        for (int index = 0; index < AuctionHouseMenu.CONFIRM_SLOT; index++) {
            if (index != slot && inventory.getItem(index) != null) return true;
        }
        return false;
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

}
