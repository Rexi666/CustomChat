package org.rexi.customChat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.rexi.customChat.utils.ChatManager;

public class LegacyChatListener implements Listener {
    private final ChatManager chatManager;

    public LegacyChatListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        event.setCancelled(true); // Cancelar el manejo por defecto

        Player player = event.getPlayer();
        String message = event.getMessage();

        chatManager.handleChat(player, message);
    }
}