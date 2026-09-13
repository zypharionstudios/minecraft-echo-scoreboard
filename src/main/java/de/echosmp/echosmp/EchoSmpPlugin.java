package de.echosmp.echosmp;

import de.echosmp.echosmp.listeners.JoinListener;
import de.echosmp.echosmp.listeners.QuitListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/** Haupteinstiegspunkt des vollständig automatischen EchoSMP-Plugins. */
public final class EchoSmpPlugin extends JavaPlugin {
    private PlaytimeManager playtimeManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        playtimeManager = new PlaytimeManager(this);
        scoreboardManager = new ScoreboardManager(this, configManager, playtimeManager);

        Bukkit.getPluginManager().registerEvents(new JoinListener(playtimeManager, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(playtimeManager, scoreboardManager), this);
        scoreboardManager.start();

        Bukkit.getScheduler().runTaskTimer(this, playtimeManager::saveAll, 6000L, 6000L);
        getLogger().info("EchoSMP wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.stop();
        }
        if (playtimeManager != null) {
            playtimeManager.saveAll();
        }
        getLogger().info("EchoSMP wurde deaktiviert.");
    }
}
