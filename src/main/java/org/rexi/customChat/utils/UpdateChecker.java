package org.rexi.customChat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.rexi.customChat.CustomChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class UpdateChecker implements Listener {

    private final CustomChat plugin;
    private final String currentVersion;
    private final String updateUrl;

    public UpdateChecker(CustomChat plugin, String currentVersion, String updateUrl) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.updateUrl = updateUrl;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                URL url = new URL(updateUrl);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    Component message = plugin.getMessage("new_version_available",
                            "{current_version}", currentVersion,
                            "{new_version}", latestVersion,
                            "{url}", "https://www.spigotmc.org/resources/customchat.127697/");
                    Bukkit.getConsoleSender().sendMessage(message);
                }
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(Component.text("[CustomChat] Error validating updates").color(NamedTextColor.RED));
            }
        }, 3 * 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("customchat.admin")) {
            checkForUpdatesPlayer(player);
        }
    }

    public void checkForUpdatesPlayer(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                URL url = new URL(updateUrl);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    Component message = plugin.getMessage("new_version_available",
                            "{current_version}", currentVersion,
                            "{new_version}", latestVersion,
                            "{url}", "https://www.spigotmc.org/resources/customchat.127697/");
                    Component tpLine = message
                            .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/customchat.127697/"));
                    player.sendMessage(tpLine);
                }
            } catch (IOException e) {
                player.sendMessage(
                        Component.text("[CustomChat] Error validating updates.").color(NamedTextColor.RED));
            }
        }, 3 * 20L);
    }

    public void checkForUpdatesCommand(CommandSender sender) {
        try {
            URL url = new URL(updateUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String latestVersion = reader.readLine().trim();
            reader.close();

            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                Component message = plugin.getMessage("new_version_available",
                        "{current_version}", currentVersion,
                        "{new_version}", latestVersion,
                        "{url}", "https://www.spigotmc.org/resources/customchat.127697/");
                Component tpLine = message
                        .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/customchat.127697/"));
                sender.sendMessage(tpLine);
            } else {
                Component message = plugin.getMessage("latest_version",
                        "{version}", currentVersion);
                sender.sendMessage(message);
            }
        } catch (IOException e) {
            sender.sendMessage(
                    Component.text("[CustomChat] Error validating updates.").color(NamedTextColor.RED));
        }
    }
}