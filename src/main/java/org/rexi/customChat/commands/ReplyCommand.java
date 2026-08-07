package org.rexi.customChat.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;

import java.util.List;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final CustomChat plugin;

    public ReplyCommand(CustomChat plugin) {
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

        if (args.length < 1) {
            player.sendMessage(plugin.getMessage("reply_usage"));
            return true;
        }

        UUID targetUUID = plugin.messageReplies.get(player.getUniqueId());

        if (targetUUID == null) {
            player.sendMessage(plugin.getMessage("reply_offline"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(targetUUID);

        if (target == null) {
            player.sendMessage(plugin.getMessage("reply_offline"));
            return true;
        }

        String targetName = target.getName();
        String playerName = player.getName();

        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(plugin.getMessage("msg_self"));
            return true;
        }

        String message = String.join(" ", args);

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
}
