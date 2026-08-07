package org.rexi.customChat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.rexi.customChat.CustomChat;

public class JoinLeaveMessagesListener implements Listener {

    private final CustomChat plugin;

    public JoinLeaveMessagesListener(CustomChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("join_leave_messages.enabled", true)) {
            return;
        }

        event.joinMessage(null);

        sendMessage(event.getPlayer(), "join_leave_messages.join_message");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("join_leave_messages.enabled", true)) {
            return;
        }

        event.quitMessage(null);

        sendMessage(event.getPlayer(), "join_leave_messages.leave_message");
    }

    private void sendMessage(Player player, String path) {
        String message = plugin.getConfig()
                .getString(path, "")
                .replace("{player}", player.getName());

        String replaced = PlaceholderAPI.setPlaceholders(player, message);

        Component component = plugin.deserialize(replaced);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(component);
        }
        Bukkit.getConsoleSender().sendMessage(component);
    }
}
