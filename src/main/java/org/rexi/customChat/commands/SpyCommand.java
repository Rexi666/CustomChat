package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;

import java.util.*;

public class SpyCommand implements CommandExecutor, TabCompleter {

    private final CustomChat plugin;

    public SpyCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("noconsole"));
            return true;
        }

        if (!player.hasPermission("customchat.spy")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        String playerName = player.getName();

        if (args.length >= 1) {
            String targetName = args[0];

            if (targetName.equalsIgnoreCase(playerName)) {
                player.sendMessage(plugin.getMessage("spy_self"));
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(targetName);

            if (target == null) {
                player.sendMessage(plugin.getMessage("spy_offline", "{player}", targetName));
                return true;
            }

            UUID playerUUID = player.getUniqueId();
            UUID targetUUID = target.getUniqueId();

            List<UUID> spies = plugin.spyPlayers.get(targetUUID);

            if (spies != null && spies.contains(playerUUID)) {
                spies.remove(playerUUID);
                plugin.spyPlayers.remove(targetUUID);
                if (!spies.isEmpty()) {
                    plugin.spyPlayers.put(targetUUID, spies);
                }
                player.sendMessage(plugin.getMessage("spy_player_disabled", "{player}", targetName));
            } else {
                plugin.spyPlayers.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(playerUUID);
                player.sendMessage(plugin.getMessage("spy_player_enabled", "{player}", targetName));
            }

        } else {
            UUID playerUUID = player.getUniqueId();

            if (plugin.spyGlobalPlayers.contains(playerUUID)) {
                plugin.spyGlobalPlayers.remove(playerUUID);
                player.sendMessage(plugin.getMessage("spy_disabled"));
            } else {
                plugin.spyGlobalPlayers.add(playerUUID);
                player.sendMessage(plugin.getMessage("spy_enabled"));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // No sugerirse a sí mismo
                if (sender instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }

                if (player.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    suggestions.add(player.getName());
                }
            }

            Collections.sort(suggestions);
            return suggestions;
        }

        return Collections.emptyList();
    }
}
