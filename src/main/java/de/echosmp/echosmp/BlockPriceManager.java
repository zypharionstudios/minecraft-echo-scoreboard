package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Erzeugt und lädt einen nachvollziehbaren Verkaufspreis für jeden Block. */
public final class BlockPriceManager {
    private static final int PRICE_SCHEMA_VERSION = 2;
    private final EchoSmpPlugin plugin;
    private final File file;
    private final FileConfiguration prices;

    public BlockPriceManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "prices.yml");
        prices = YamlConfiguration.loadConfiguration(file);
        boolean changed = prices.getInt("_schema-version", 0) != PRICE_SCHEMA_VERSION;
        if (changed) {
            prices.set("_schema-version", PRICE_SCHEMA_VERSION);
        }
        for (Material material : Material.values()) {
            if (material.isBlock() && (changed || !prices.contains(material.name()))) {
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
        return Math.max(2L, prices.getLong(material.name(), defaultPrice(material)));
    }

    private long defaultPrice(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT);
        if (containsAny(name, "netherite_block")) return 50000L;
        if (containsAny(name, "ancient_debris")) return 12500L;
        if (containsAny(name, "diamond_block")) return 9000L;
        if (containsAny(name, "emerald_block")) return 7000L;
        if (containsAny(name, "gold_block")) return 2500L;
        if (containsAny(name, "iron_block")) return 1000L;
        if (containsAny(name, "copper_block")) return 400L;
        if (containsAny(name, "diamond_ore")) return 1800L;
        if (containsAny(name, "emerald_ore")) return 1400L;
        if (containsAny(name, "gold_ore")) return 700L;
        if (containsAny(name, "lapis_ore")) return 500L;
        if (containsAny(name, "redstone_ore")) return 450L;
        if (containsAny(name, "iron_ore")) return 300L;
        if (containsAny(name, "copper_ore")) return 150L;
        if (containsAny(name, "coal_ore")) return 120L;
        if (containsAny(name, "quartz_ore")) return 220L;
        if (containsAny(name, "redstone_block")) return 900L;
        if (containsAny(name, "lapis_block")) return 1000L;
        if (containsAny(name, "quartz_block", "amethyst_block")) return 300L;
        if (containsAny(name, "gold", "raw_gold")) return 250L;
        if (containsAny(name, "iron", "raw_iron")) return 100L;
        if (containsAny(name, "copper", "raw_copper")) return 60L;
        if (containsAny(name, "redstone")) return 75L;
        if (containsAny(name, "lapis")) return 80L;
        if (containsAny(name, "coal")) return 35L;
        if (containsAny(name, "quartz")) return 90L;
        if (containsAny(name, "obsidian", "crying_obsidian")) return 125L;
        if (containsAny(name, "beacon", "conduit", "enchanting_table")) return 1500L;
        if (containsAny(name, "end_portal_frame", "dragon_egg")) return 10000L;
        if (containsAny(name, "nether_star")) return 25000L;
        if (containsAny(name, "prismarine", "sea_lantern")) return 80L;
        if (containsAny(name, "purpur", "end_stone")) return 45L;
        if (containsAny(name, "nether_brick", "soul_sand", "soul_soil")) return 35L;
        if (containsAny(name, "netherrack", "basalt", "blackstone")) return 12L;
        if (containsAny(name, "log", "wood", "stem", "hyphae")) return 35L;
        if (containsAny(name, "planks")) return 15L;
        if (containsAny(name, "leaves")) return 5L;
        if (containsAny(name, "wool", "carpet")) return 18L;
        if (containsAny(name, "concrete", "terracotta", "glazed")) return 16L;
        if (containsAny(name, "brick", "mossy", "chiseled", "polished")) return 22L;
        if (containsAny(name, "glass", "ice", "snow")) return 10L;
        if (containsAny(name, "stone", "deepslate", "cobblestone", "tuff")) return 8L;
        if (containsAny(name, "dirt", "grass_block", "mud", "sand", "gravel", "clay")) return 6L;
        if (containsAny(name, "flower", "mushroom", "coral", "vine", "cactus")) return 8L;
        return 5L;
    }

    private boolean containsAny(String name, String... values) {
        for (String value : values) {
            if (name.contains(value)) {
                return true;
            }
        }
        return false;
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
