package org.rexi.customChat.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.rexi.customChat.CustomChat;

import java.util.Optional;

public class LegacyChatListener implements Listener {
    private final CustomChat plugin;

    public LegacyChatListener(CustomChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        event.setCancelled(true); // Cancelar el manejo por defecto

        Player player = event.getPlayer();
        String message = event.getMessage();

        // Ejecutar en el hilo principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            Optional<Component> fullMessage = plugin.getFormatFor(player).map(format -> {
                Component display = format.getDisplay(player);
                Component twoPoints = format.getTwoPoints();
                Component content = format.getFormattedMessage(message, player);

                // Construir mensaje completo: display + space + twoPoints + content
                Component messageComponent = Component.empty()
                        .append(display)
                        .append(Component.space())
                        .append(twoPoints)
                        .append(Component.space())
                        .append(content);

                // Hover y click
                if (!format.getHover(player).isEmpty()) {
                    Component hoverText = Component.join(net.kyori.adventure.text.JoinConfiguration.separator(Component.newline()), format.getHover(player));
                    messageComponent = messageComponent.hoverEvent(HoverEvent.showText(hoverText));
                }

                if (!format.getClickType().equals("none")) {
                    ClickEvent.Action action = ClickEvent.Action.valueOf(format.getClickType().toUpperCase());
                    String clickValue = format.getClickValue(player);
                    messageComponent = messageComponent.clickEvent(ClickEvent.clickEvent(action, clickValue));
                }

                return messageComponent;
            });

            if (fullMessage.isPresent()) {
                Component messageToSend = fullMessage.get();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(messageToSend);
                }
                Bukkit.getConsoleSender().sendMessage(messageToSend);
            } else {
                player.sendMessage(Component.text(message));
                Bukkit.getConsoleSender().sendMessage(Component.text(message));
            }
        });
    }
}