package org.rexi.customChat.config;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatFormat {
    private final CustomChat plugin;

    private final String permission;
    private final String display;
    private final String twoPoints;
    private final String messageFormat;
    private final List<String> hover;
    private final String clickType;
    private final String clickValue;

    public ChatFormat(String sectionName, ConfigurationSection section, CustomChat plugin) {
        this.plugin = plugin;

        this.permission = "customchat.format."+sectionName;
        this.display = section.getString("display", "{displayName}");
        this.twoPoints = section.getString("two_points", "&8: ");
        this.messageFormat = section.getString("message", "&7{message}");
        this.hover = section.getStringList("hover.message");
        this.clickType = section.getString("hover.click_action.type", "none");
        this.clickValue = section.getString("hover.click_action.value", "");
    }

    public String getPermission() { return permission; }

    public Component getDisplay(Player player) {
        String replaced = PlaceholderAPI.setPlaceholders(player, display.replace("{displayName}", player.getName()));
        return plugin.deserialize(replaced);
    }

    public Component getTwoPoints() {
        return plugin.deserialize(twoPoints);
    }

    public Component getFormattedMessage(String rawMessage, Player player) {
        String message = rawMessage;
        boolean customColor = false;

        // Si NO tiene permiso, quitar los códigos de color (&a, &b, etc.)
        if (!player.hasPermission("customchat.changecolor")) {
            message = message.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
            message = message.replaceAll("<[^>]*>", "");
        } else {
            if (!player.hasPermission("customchat.changeformat")) {
                message = message.replaceAll("&[lkmnorLKMNOR]|</?(?i:obf|bold|st|u|i|reset|b)>", "");
            } else if (message.contains("&k") || message.contains("<obf>")) {
                if (plugin.getConfig().getBoolean("block_k")) {
                    message = message.replaceAll("(?i)&k", "");
                    message = message.replaceAll("(?i)</?obf>", "");
                    player.sendMessage(plugin.getMessage("k_blocked"));
                }
            }

            if (message.contains("&") || (message.contains("<") && message.contains(">"))) {
                customColor = true;
            }
        }

        String formatted;
        if (!customColor) {
            formatted = messageFormat.replace("{message}", message);
        } else {
            formatted = message;
        }
        String withPlaceholders = PlaceholderAPI.setPlaceholders(player, formatted);
        return plugin.deserialize(withPlaceholders);
    }

    public List<Component> getHover(Player player) {
        return hover.stream()
                .map(line -> {
                    String replaced = PlaceholderAPI.setPlaceholders(player, line.replace("{displayName}", player.getName()));
                    return plugin.deserialize(replaced);
                })
                .collect(Collectors.toList());
    }

    public String getClickType() {
        Set<String> validTypes = Set.of("run_command", "open_url", "suggest_command");
        return validTypes.contains(clickType.toLowerCase()) ? clickType.toLowerCase() : "none";
    }

    public String getClickValue(Player player) {
        return PlaceholderAPI.setPlaceholders(player, clickValue.replace("{displayName}", player.getName()));
    }
}

