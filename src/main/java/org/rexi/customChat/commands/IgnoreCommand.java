package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IgnoreCommand implements CommandExecutor, TabCompleter {

    private final CustomChat plugin;

    public IgnoreCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("noconsole"));
            return true;
        }

        if (!player.hasPermission("customchat.ignore")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessage("ignore_usage"));
            return true;
        }

        String targetName = args[0];
        String playerName = player.getName();

        if (targetName.equalsIgnoreCase(playerName)) {
            player.sendMessage(plugin.getMessage("ignore_self"));
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null) {
            player.sendMessage(plugin.getMessage("ignore_offline", "{player}", targetName));
            return true;
        }

        if (target.hasPermission("customchat.ignore.bypass")) {
            player.sendMessage(plugin.getMessage("ignore_bypass", "{player}", targetName));
            return true;
        }

        boolean isIgnoring = plugin.checkIgnoredPlayers(player.getUniqueId(), target.getUniqueId());
        if (isIgnoring) {
            plugin.setIgnoredPlayers(player.getUniqueId(), target.getUniqueId(), false);
            player.sendMessage(plugin.getMessage("ignore_removed", "{player}", targetName));
        } else {
            plugin.setIgnoredPlayers(player.getUniqueId(), target.getUniqueId(), true);
            player.sendMessage(plugin.getMessage("ignore_added", "{player}", targetName));
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
