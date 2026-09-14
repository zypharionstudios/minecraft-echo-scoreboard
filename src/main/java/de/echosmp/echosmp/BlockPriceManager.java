package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Erzeugt und lädt einen nachvollziehbaren Verkaufspreis für jeden Block. */
public final class BlockPriceManager {
    private final EchoSmpPlugin plugin;
    private final File file;
    private final FileConfiguration prices;

    public BlockPriceManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "prices.yml");
        prices = YamlConfiguration.loadConfiguration(file);
        boolean changed = false;
        for (Material material : Material.values()) {
            if (material.isBlock() && !prices.contains(material.name())) {
                prices.set(material.name(), defaultPrice(material));
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    public long get(Material material) {
        if (!material.isBlock()) {
            return 0L;
        }
        return Math.max(0L, prices.getLong(material.name(), defaultPrice(material)));
    }

    private long defaultPrice(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT);
        if (name.contains("netherite")) return 5000L;
        if (name.contains("diamond")) return 1000L;
        if (name.contains("emerald")) return 750L;
        if (name.contains("ancient_debris")) return 2500L;
        if (name.contains("gold")) return 250L;
        if (name.contains("iron")) return 100L;
        if (name.contains("copper")) return 50L;
        if (name.contains("redstone") || name.contains("lapis")) return 40L;
        if (name.contains("coal")) return 25L;
        if (name.contains("quartz") || name.contains("amethyst")) return 35L;
        if (name.contains("obsidian")) return 30L;
        if (name.contains("ore")) return 20L;
        if (name.contains("log") || name.contains("wood") || name.contains("planks")) return 8L;
        if (name.contains("leaves")) return 2L;
        if (name.contains("stone") || name.contains("deepslate") || name.contains("terracotta")) return 3L;
        if (name.contains("dirt") || name.contains("sand") || name.contains("gravel")) return 2L;
        return 1L;
    }

    private void save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            prices.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Blockpreise konnten nicht gespeichert werden.", exception);
        }
    }
}
