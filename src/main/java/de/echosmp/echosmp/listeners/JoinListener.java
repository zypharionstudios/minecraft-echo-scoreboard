package de.echosmp.echosmp.listeners;

import de.echosmp.echosmp.PlaytimeManager;
import de.echosmp.echosmp.ScoreboardManager;
import de.echosmp.echosmp.AuctionHouseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Startet Spielzeit und Sidebar beim Betreten des Servers. */
public final class JoinListener implements Listener {
    private final PlaytimeManager playtimeManager;
    private final ScoreboardManager scoreboardManager;
    private final AuctionHouseManager auctionHouseManager;

    public JoinListener(PlaytimeManager playtimeManager, ScoreboardManager scoreboardManager,
                        AuctionHouseManager auctionHouseManager) {
        this.playtimeManager = playtimeManager;
        this.scoreboardManager = scoreboardManager;
        this.auctionHouseManager = auctionHouseManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playtimeManager.playerJoined(event.getPlayer());
        auctionHouseManager.claimReturns(event.getPlayer());
        scoreboardManager.show(event.getPlayer());
    }
}
