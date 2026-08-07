package org.rexi.customChat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.rexi.customChat.CustomChat;

import java.util.UUID;

public class ColorChatListener implements Listener {

    private final CustomChat plugin;

    public ColorChatListener(CustomChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerChatColorFromDB(player);
        plugin.getMentionEnabledFromDB(player);
        plugin.getIgnoredPlayersFromDB(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.cacheRemoveChatColor(player);
        plugin.cacheRemoveMentionEnabled(player);
        plugin.cacheRemoveIgnoredPlayers(player);

        UUID playerUUID = player.getUniqueId();
        plugin.spyGlobalPlayers.remove(playerUUID);
        plugin.removeSpy(playerUUID);
    }
}
