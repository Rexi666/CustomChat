package org.rexi.customChat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.config.ChatFormat;
import org.rexi.customChat.utils.WebhookManager;

import java.util.Optional;

public class NewChatListener implements Listener {
    private final CustomChat plugin;
    private final WebhookManager webhookManager;

    public NewChatListener(CustomChat plugin, WebhookManager webhookManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().extractUrls().build().serialize(event.message());

        // Ejecutar en el hilo principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            Optional<Component> fullMessage = plugin.getFormatFor(player).map(format -> {
                Component display = format.getDisplay(player);
                Component twoPoints = format.getTwoPoints();
                Component content = format.getFormattedMessage(message, player);

                // Hover y click
                Component hoverText = null;
                ClickEvent clickEvent = null;
                if (!format.getHover(player).isEmpty()) {
                    hoverText = Component.join(
                            net.kyori.adventure.text.JoinConfiguration.separator(Component.newline()),
                            format.getHover(player)
                    );
                }

                if (!format.getClickType().equals("none")) {
                    ClickEvent.Action action = ClickEvent.Action.valueOf(format.getClickType().toUpperCase());
                    String clickValue = format.getClickValue(player);
                    clickEvent = ClickEvent.clickEvent(action, clickValue);
                }

                Component fixedPart = Component.empty()
                        .append(display)
                        .append(Component.space())
                        .append(twoPoints)
                        .append(Component.space());

                if (hoverText != null) {
                    fixedPart = fixedPart.hoverEvent(HoverEvent.showText(hoverText));
                }
                if (clickEvent != null) {
                    fixedPart = fixedPart.clickEvent(clickEvent);
                }

                Component messageComponent = Component.empty()
                        .append(fixedPart)
                        .append(content);

                return messageComponent;
            });

            if (fullMessage.isPresent()) {
                Component messageToSend = fullMessage.get();
                boolean mention = plugin.getConfig().getBoolean("mentioning.enabled");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(messageToSend);
                    if (mention) {
                        ChatFormat.mention(player, p, messageToSend);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(messageToSend);
            } else {
                player.sendMessage(Component.text(message));
                Bukkit.getConsoleSender().sendMessage(Component.text(message));
            }

            if (plugin.getConfig().getBoolean("discord_hook.enabled", false)) {
                String finalmessage = message
                        .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                        .replaceAll("<[^>]*>", "")
                        .replaceAll("@\\s*(here|everyone)", "");

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                        webhookManager.send(finalmessage, player.getName(), player.getUniqueId().toString())
                );
            }
        });
    }
}
