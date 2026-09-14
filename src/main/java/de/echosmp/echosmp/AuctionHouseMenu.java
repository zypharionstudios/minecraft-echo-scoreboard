package de.echosmp.echosmp;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Erstellt Haupt-, eigene-, Einstell- und Preis-Menüs des Auktionshauses. */
public final class AuctionHouseMenu {
    public static final String MAIN_TITLE = "EchoSMP AH";
    public static final String OWN_TITLE = "EchoSMP Meine Angebote";
    public static final String ADD_TITLE = "EchoSMP Angebot hinzufügen";
    public static final String SEARCH_TITLE = "EchoSMP AH Suche";
    public static final int BLOCKED_SLOT = 13;
    public static final int CONFIRM_SLOT = 26;
    private final AuctionHouseManager auctions;
    private final Map<UUID, AuctionHouseManager.Sort> sorts = new HashMap<>();
    private final Map<UUID, String> searches = new HashMap<>();
    private final Map<UUID, ItemStack> pendingItems = new HashMap<>();

    public AuctionHouseMenu(AuctionHouseManager auctions) { this.auctions = auctions; }

    public void openMain(Player player) { openMain(player, 0); }

    public AuctionListing listingAt(Player player, int slot) {
        if (slot < 0 || slot >= 45) return null;
        List<AuctionListing> listings = auctions.find(searches.get(player.getUniqueId()),
                sorts.getOrDefault(player.getUniqueId(), AuctionHouseManager.Sort.NEWEST));
        return slot < listings.size() ? listings.get(slot) : null;
    }

    private void openMain(Player player, int ignored) {
        Inventory inventory = Bukkit.createInventory(null, 54, MAIN_TITLE);
        List<AuctionListing> listings = auctions.find(searches.get(player.getUniqueId()),
                sorts.getOrDefault(player.getUniqueId(), AuctionHouseManager.Sort.NEWEST));
        for (int slot = 0; slot < Math.min(45, listings.size()); slot++) {
            AuctionListing listing = listings.get(slot);
            inventory.setItem(slot, displayItem(listing));
        }
        inventory.setItem(45, button(Material.CLOCK, ChatColor.YELLOW + "Aktualisieren"));
        inventory.setItem(49, button(Material.ANVIL, ChatColor.GOLD + "Sortierung: " + sortName(player)));
        inventory.setItem(50, button(Material.NAME_TAG, ChatColor.AQUA + "Suche: "
                + (searches.getOrDefault(player.getUniqueId(), "").isEmpty() ? "Alle" : searches.get(player.getUniqueId()))));
        inventory.setItem(53, button(Material.CHEST, ChatColor.GREEN + "Meine Angebote / Hinzufügen"));
        player.openInventory(inventory);
    }

    public void openOwn(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, OWN_TITLE);
        List<AuctionListing> own = auctions.sellerListings(player.getUniqueId());
        for (int slot = 0; slot < Math.min(45, own.size()); slot++) inventory.setItem(slot, displayItem(own.get(slot)));
        inventory.setItem(53, button(Material.BOOK, ChatColor.GREEN + "Neues Angebot erstellen"));
        inventory.setItem(45, button(Material.ARROW, ChatColor.YELLOW + "Zurück"));
        player.openInventory(inventory);
    }

    public void openAdd(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ADD_TITLE);
        inventory.setItem(BLOCKED_SLOT, button(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "Mitte gesperrt"));
        inventory.setItem(CONFIRM_SLOT, button(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Bestätigen"));
        player.openInventory(inventory);
    }

    public AuctionListing ownListingAt(Player player, int slot) {
        if (slot < 0 || slot >= 45) return null;
        List<AuctionListing> own = auctions.sellerListings(player.getUniqueId());
        return slot < own.size() ? own.get(slot) : null;
    }

    public void setPending(Player player, ItemStack item) {
        pendingItems.put(player.getUniqueId(), item.clone());
    }

    public void cancelPending(Player player) { pendingItems.remove(player.getUniqueId()); }

    public ItemStack takePending(UUID uuid) { return pendingItems.remove(uuid); }
    public ItemStack pending(UUID uuid) { return pendingItems.get(uuid); }
    public void setSearch(UUID uuid, String search) { searches.put(uuid, search == null ? "" : search); }
    public AuctionHouseManager.Sort cycleSort(UUID uuid) {
        AuctionHouseManager.Sort next = switch (sorts.getOrDefault(uuid, AuctionHouseManager.Sort.NEWEST)) {
            case NEWEST -> AuctionHouseManager.Sort.CHEAPEST;
            case CHEAPEST -> AuctionHouseManager.Sort.MOST_EXPENSIVE;
            case MOST_EXPENSIVE -> AuctionHouseManager.Sort.OLDEST;
            case OLDEST -> AuctionHouseManager.Sort.NEWEST;
        };
        sorts.put(uuid, next);
        return next;
    }

    private String sortName(Player player) {
        return switch (sorts.getOrDefault(player.getUniqueId(), AuctionHouseManager.Sort.NEWEST)) {
            case NEWEST -> "Neueste"; case CHEAPEST -> "Günstigste";
            case MOST_EXPENSIVE -> "Teuerste"; case OLDEST -> "Älteste";
        };
    }

    private ItemStack displayItem(AuctionListing listing) {
        ItemStack item = listing.item().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() && meta.getLore() != null ? meta.getLore() : new java.util.ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GREEN + "Preis: " + MoneyManager.format(listing.price()));
        lore.add(ChatColor.GRAY + "Verkäufer: " + listing.sellerName());
        long hours = Math.max(0, Duration.ofMillis(24L * 60L * 60L * 1000L -
                (System.currentTimeMillis() - listing.createdAt())).toHours());
        lore.add(ChatColor.GRAY + "Läuft ab in: " + hours + "h");
        lore.add(ChatColor.YELLOW + "Klicken zum Kaufen");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
