package de.echosmp.echosmp;

import java.util.UUID;
import org.bukkit.inventory.ItemStack;

/** Ein einzelnes, zeitlich begrenztes Auktionhaus-Angebot. */
public final class AuctionListing {
    private final UUID id;
    private final UUID seller;
    private final String sellerName;
    private final ItemStack item;
    private final long price;
    private final long createdAt;

    public AuctionListing(UUID id, UUID seller, String sellerName, ItemStack item, long price, long createdAt) {
        this.id = id;
        this.seller = seller;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID seller() { return seller; }
    public String sellerName() { return sellerName; }
    public ItemStack item() { return item; }
    public long price() { return price; }
    public long createdAt() { return createdAt; }
    public boolean expired(long now) { return now - createdAt >= 24L * 60L * 60L * 1000L; }
}
