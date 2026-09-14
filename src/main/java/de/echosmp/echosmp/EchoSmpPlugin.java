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
    private MoneyManager moneyManager;

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        playtimeManager = new PlaytimeManager(this);
        settingsManager = new PlayerSettingsManager(this);
        moneyManager = new MoneyManager(this);
        BlockPriceManager priceManager = new BlockPriceManager(this);
        scoreboardManager = new ScoreboardManager(this, configManager, playtimeManager, settingsManager, moneyManager);
        SettingsMenu settingsMenu = new SettingsMenu(settingsManager);
        SellMenu sellMenu = new SellMenu(priceManager);

        Bukkit.getPluginManager().registerEvents(new JoinListener(playtimeManager, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(playtimeManager, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new SettingsListener(settingsManager, settingsMenu, scoreboardManager), this);
        Bukkit.getPluginManager().registerEvents(new SellListener(sellMenu, moneyManager), this);
        getCommand("scoreboard").setExecutor(new SettingsCommand(settingsMenu));
        getCommand("sell").setExecutor(new SellCommand(sellMenu));
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
        if (moneyManager != null) {
            moneyManager.save();
        }
        getLogger().info("EchoSMP wurde deaktiviert.");
    }
}
