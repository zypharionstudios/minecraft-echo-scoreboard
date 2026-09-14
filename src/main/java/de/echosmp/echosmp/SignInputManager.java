package de.echosmp.echosmp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Öffnet geschützte temporäre Schilder für Preis- und Sucheingaben. */
public final class SignInputManager implements Listener {
    private final EchoSmpPlugin plugin;
    private final Map<UUID, Request> requests = new HashMap<>();

    public SignInputManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean open(Player player, Mode mode, BiConsumer<Player, String> callback) {
        return open(player, mode, callback, () -> { });
    }

    public boolean open(Player player, Mode mode, BiConsumer<Player, String> callback, Runnable onCancel) {
        cancel(player);
        Block block = findAirBlock(player);
        if (block == null) {
            player.sendMessage(ChatColor.RED + "Kein Platz für das Eingabeschild gefunden.");
            return false;
        }
        BlockData original = block.getBlockData().clone();
        block.setType(Material.OAK_SIGN, false);
        if (!(block.getState() instanceof Sign sign)) {
            block.setBlockData(original, false);
            return false;
        }
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update(true, false);
        requests.put(player.getUniqueId(), new Request(block, original, mode, callback, onCancel));
        player.openSign(sign);
        return true;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Request request = requests.remove(event.getPlayer().getUniqueId());
        if (request == null || !request.block().equals(event.getBlock())) return;
        StringBuilder input = new StringBuilder();
        for (int index = 0; index < 4; index++) {
            String line = event.getLine(index);
            if (line != null && !line.isBlank()) {
                if (input.length() > 0) input.append(' ');
                input.append(line.trim());
            }
        }
        restore(request);
        request.callback().accept(event.getPlayer(), input.toString().trim());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }

    public void cancel(Player player) {
        Request request = requests.remove(player.getUniqueId());
        if (request != null) {
            restore(request);
            request.onCancel().run();
        }
    }

    private Block findAirBlock(Player player) {
        Block base = player.getLocation().getBlock();
        for (int height = 2; height <= 5; height++) {
            Block block = base.getRelative(0, height, 0);
            if (block.getType().isAir()) return block;
        }
        return null;
    }

    private void restore(Request request) {
        if (request.block().getType() == Material.OAK_SIGN) {
            request.block().setBlockData(request.original(), false);
        }
    }

    public enum Mode { PRICE, SEARCH }

    private record Request(Block block, BlockData original, Mode mode, BiConsumer<Player, String> callback,
                           Runnable onCancel) { }
}
