package org.rexi.customChat.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;

import java.util.*;

public class MsgCommand implements CommandExecutor, TabCompleter {

    private final CustomChat plugin;

    public MsgCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("noconsole"));
            return true;
        }

        if (!player.hasPermission("customchat.message")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("msg_usage"));
            return true;
        }

        String targetName = args[0];
        String playerName = player.getName();

        if (targetName.equalsIgnoreCase(playerName)) {
            player.sendMessage(plugin.getMessage("msg_self"));
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null) {
            player.sendMessage(plugin.getMessage("msg_offline", "{player}", targetName));
            return true;
        }

        boolean ignoring = plugin.checkIgnoredPlayers(target.getUniqueId(), player.getUniqueId());
        if (ignoring) {
            player.sendMessage(plugin.getMessage("msg_ignoring", "{player}", targetName));
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        Component finalMessage = plugin.getMessage("msg", "{player}", playerName, "{target}", targetName, "{message}", message);

        plugin.messageReplies.put(target.getUniqueId(), player.getUniqueId());
        plugin.messageReplies.put(player.getUniqueId(), target.getUniqueId());

        player.sendMessage(finalMessage);
        target.sendMessage(finalMessage);

        List<UUID> spies1 = plugin.spyPlayers.get(target.getUniqueId());
        if (spies1 != null) {
            for (UUID uuid : spies1) {
                Player spy = plugin.getServer().getPlayer(uuid);
                if (spy != null) {
                    spy.sendMessage(finalMessage);
                }
            }
        }

        List<UUID> spies2 = plugin.spyPlayers.get(player.getUniqueId());
        if (spies2 != null) {
            for (UUID uuid : spies2) {
                Player spy = plugin.getServer().getPlayer(uuid);
                if (spy != null) {
                    spy.sendMessage(finalMessage);
                }
            }
        }

        List<UUID> globalSpies = plugin.spyGlobalPlayers;
        for (UUID uuid : globalSpies) {
            Player spy = plugin.getServer().getPlayer(uuid);
            if (spy != null) {
                spy.sendMessage(finalMessage);
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

        // A partir del segundo argumento es el mensaje
        return Collections.emptyList();
    }
}
