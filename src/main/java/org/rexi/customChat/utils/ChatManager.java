package org.rexi.customChat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.config.ChatFormat;

import java.util.Optional;

public class ChatManager {
    private final CustomChat plugin;
    private final WebhookManager webhookManager;

    public ChatManager(CustomChat plugin, WebhookManager webhookManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
    }

    public void handleChat(Player player, String message) {
        if (plugin.chatlockActive && !player.hasPermission("customchat.chatlock")) {
            player.sendMessage(plugin.getMessage("chatlock_active"));
            return;
        }

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

                clickEvent = createClickEvent(
                        format.getClickType(),
                        format.getClickValue(player)
                );

                Component fixedPart = Component.empty()
                        .append(display)
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

    private ClickEvent createClickEvent(String type, String value) {
        if (type == null) {
            return null;
        }

        String normalizedType = type.trim()
                .toLowerCase(java.util.Locale.ROOT);

        if (normalizedType.isEmpty() || normalizedType.equals("none")) {
            return null;
        }

        if (value == null) {
            plugin.getLogger().warning(
                    "Couldnt create click event for type: " + type
            );
            return null;
        }

        try {
            switch (normalizedType) {
                case "open_url":
                    return ClickEvent.openUrl(value);

                case "open_file":
                    return ClickEvent.openFile(value);

                case "run_command":
                    return ClickEvent.runCommand(value);

                case "suggest_command":
                    return ClickEvent.suggestCommand(value);

                case "copy_to_clipboard":
                    return ClickEvent.copyToClipboard(value);

                case "change_page":
                    int page = Integer.parseInt(value.trim());

                    if (page < 1) {
                        throw new IllegalArgumentException(
                                "Page number must be greater than 0. Provided: " + page
                        );
                    }

                    return ClickEvent.changePage(page);

                default:
                    plugin.getLogger().warning(
                            "Unknown chat click type: " + type
                    );
                    return null;
            }
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(
                    "Invalid chat click event: type=" + type
                            + ", value=" + value
                            + ". " + exception.getMessage()
            );
            return null;
        }
    }

}
