package de.echosmp.echosmp;

import de.echosmp.echosmp.listeners.JoinListener;
import de.echosmp.echosmp.listeners.QuitListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/** Haupteinstiegspunkt des vollständig automatischen EchoSMP-Plugins. */
public final class EchoSmpPlugin extends JavaPlugin {
    private PlaytimeManager playtimeManager;
    private ScoreboardManager scoreboardManager;
    private PlayerSettingsManager settingsManager;

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        playtimeManager = new PlaytimeManager(this);
        settingsManager = new PlayerSettingsManager(this);
        scoreboardManager = new ScoreboardManager(this, configManager, playtimeManager, settingsManager);
        SettingsMenu settingsMenu = new SettingsMenu(settingsManager);

        Bukkit.getPluginManager().registerEvents(new JoinListener(playtimeManager, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(playtimeManager, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new SettingsListener(settingsManager, settingsMenu, scoreboardManager), this);
        getCommand("scoreboard").setExecutor(new SettingsCommand(settingsMenu));
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
        if (settingsManager != null) {
            settingsManager.save();
        }
        getLogger().info("EchoSMP wurde deaktiviert.");
    }
}
