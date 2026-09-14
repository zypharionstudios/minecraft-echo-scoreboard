package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Persistiert Angebote und wickelt sichere Käufe ab. */
public final class AuctionHouseManager {
    private final EchoSmpPlugin plugin;
    private final MoneyManager money;
    private final File file;
    private final FileConfiguration data;
    private final Map<UUID, AuctionListing> listings = new LinkedHashMap<>();

    public AuctionHouseManager(EchoSmpPlugin plugin, MoneyManager money) {
        this.plugin = plugin;
        this.money = money;
        file = new File(plugin.getDataFolder(), "auctionhouse.yml");
        data = YamlConfiguration.loadConfiguration(file);
        load();
        removeExpired();
    }

    private void load() {
        ConfigurationSection section = data.getConfigurationSection("listings");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                String path = "listings." + key;
                AuctionListing listing = new AuctionListing(
                        UUID.fromString(key),
                        UUID.fromString(data.getString(path + ".seller")),
                        data.getString(path + ".seller-name", "Unbekannt"),
                        data.getItemStack(path + ".item"),
                        Math.max(1L, data.getLong(path + ".price")),
                        data.getLong(path + ".created-at"));
                if (listing.item() != null && !listing.expired(System.currentTimeMillis())) {
                    listings.put(listing.id(), listing);
                }
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("Ungültiges AH-Angebot: " + key);
            }
        }
    }

    public synchronized AuctionListing create(Player seller, ItemStack item, long price) {
        AuctionListing listing = new AuctionListing(UUID.randomUUID(), seller.getUniqueId(),
                seller.getName(), item.clone(), Math.max(1L, price), System.currentTimeMillis());
        listings.put(listing.id(), listing);
        save();
        return listing;
    }

    public synchronized List<AuctionListing> find(String search, Sort sort) {
        removeExpired();
        String query = search == null ? "" : search.trim().toLowerCase();
        List<AuctionListing> result = listings.values().stream()
                .filter(listing -> query.isEmpty()
                        || listing.item().getType().name().toLowerCase().contains(query)
                        || listing.sellerName().toLowerCase().contains(query))
                .filter(listing -> !listing.expired(System.currentTimeMillis()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Comparator<AuctionListing> comparator = switch (sort) {
            case CHEAPEST -> Comparator.comparingLong(AuctionListing::price);
            case NEWEST -> Comparator.comparingLong(AuctionListing::createdAt).reversed();
            case OLDEST -> Comparator.comparingLong(AuctionListing::createdAt);
            case MOST_EXPENSIVE -> Comparator.comparingLong(AuctionListing::price).reversed();
        };
        result.sort(comparator);
        return result;
    }

    public synchronized List<AuctionListing> sellerListings(UUID seller) {
        removeExpired();
        return listings.values().stream().filter(listing -> listing.seller().equals(seller)).toList();
    }

    public synchronized PurchaseResult buy(Player buyer, UUID listingId) {
        removeExpired();
        AuctionListing listing = listings.get(listingId);
        if (listing == null) return PurchaseResult.NOT_FOUND;
        if (listing.seller().equals(buyer.getUniqueId())) return PurchaseResult.OWN_ITEM;
        if (money.get(buyer.getUniqueId()) < listing.price()) return PurchaseResult.NOT_ENOUGH_MONEY;
        listings.remove(listingId);
        money.remove(buyer.getUniqueId(), listing.price());
        money.add(listing.seller(), listing.price());
        buyer.getInventory().addItem(listing.item().clone()).values().forEach(leftover ->
                buyer.getWorld().dropItemNaturally(buyer.getLocation(), leftover));
        save();
        return PurchaseResult.SUCCESS;
    }

    public synchronized void removeExpired() {
        long now = System.currentTimeMillis();
        List<AuctionListing> expired = listings.values().stream().filter(listing -> listing.expired(now)).toList();
        for (AuctionListing listing : expired) {
            listings.remove(listing.id());
            Player player = plugin.getServer().getPlayer(listing.seller());
            if (player != null) {
                give(player, listing.item());
            } else {
                data.set("returns." + listing.seller() + "." + listing.id(), listing.item());
            }
        }
        boolean changed = !expired.isEmpty();
        if (changed) save();
    }

    public synchronized void claimReturns(Player player) {
        String path = "returns." + player.getUniqueId();
        ConfigurationSection section = data.getConfigurationSection(path);
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ItemStack item = data.getItemStack(path + "." + key);
            if (item != null) give(player, item);
            data.set(path + "." + key, null);
        }
        data.set(path, null);
        save();
    }

    private void give(Player player, ItemStack item) {
        player.getInventory().addItem(item.clone()).values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }

    public synchronized void save() {
        data.set("listings", null);
        for (AuctionListing listing : listings.values()) {
            String path = "listings." + listing.id();
            data.set(path + ".seller", listing.seller().toString());
            data.set(path + ".seller-name", listing.sellerName());
            data.set(path + ".item", listing.item());
            data.set(path + ".price", listing.price());
            data.set(path + ".created-at", listing.createdAt());
        }
        try {
            if (!file.exists()) file.getParentFile().mkdirs();
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "AH-Daten konnten nicht gespeichert werden.", exception);
        }
    }

    public enum Sort { CHEAPEST, MOST_EXPENSIVE, NEWEST, OLDEST }
    public enum PurchaseResult { SUCCESS, NOT_FOUND, OWN_ITEM, NOT_ENOUGH_MONEY }
}
