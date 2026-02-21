package org.rexi.customChat.config;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.utils.InventoryManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatFormat {
    private final CustomChat plugin;

    private final String permission;
    private final String display;
    private final String twoPoints;
    private final String format;
    private final List<String> hover;
    private final String clickType;
    private final String clickValue;

    public ChatFormat(String sectionName, ConfigurationSection section, CustomChat plugin) {
        this.plugin = plugin;

        this.permission = "customchat.format."+sectionName;
        this.display = section.getString("display", "{displayName}");
        this.twoPoints = section.getString("two_points", "&8 »");
        this.format = section.getString("format", "&f");
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

        String chatColor = plugin.getPlayerChatColor(player);
        String formatted;
        if (customColor) {
            formatted = message;
        } else if (chatColor != null && !chatColor.isEmpty()) {
            formatted = chatColor+message;
        } else {
            formatted = format+message;
        }
        String withPlaceholders = PlaceholderAPI.setPlaceholders(player, formatted);

        Component finalComponent = plugin.deserialize(withPlaceholders);

        if (withPlaceholders.contains("[item]") && player.hasPermission("customchat.hover.item")) {
            finalComponent = hoverItem(player, withPlaceholders, customColor, chatColor);
        }
        if (withPlaceholders.contains("[inv]") && player.hasPermission("customchat.hover.inv")) {
            finalComponent = hoverInv(player, withPlaceholders, customColor, chatColor);
        }
        if (withPlaceholders.contains("[ender]") && player.hasPermission("customchat.hover.ender")) {
            finalComponent = hoverEnder(player, withPlaceholders, customColor, chatColor);
        }
        if (withPlaceholders.contains("[location]") && player.hasPermission("customchat.hover.location")) {
            finalComponent = hoverLocation(player, withPlaceholders, customColor, chatColor);
        }
        if (withPlaceholders.contains("[ping]") && player.hasPermission("customchat.hover.ping")) {
            finalComponent = hoverPing(player, withPlaceholders, customColor, chatColor);
        }

        return finalComponent;
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

    private Component hoverItem(Player player, String withPlaceholders, boolean customColor, String chatcolor) {
        String[] parts = withPlaceholders.split("(?=\\[item\\])|(?<=\\[item\\])");
        Component finalComponent = Component.empty();

        for (String part : parts) {
            if (part.equalsIgnoreCase("[item]")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                ItemMeta meta = item.getItemMeta();

                // Nombre del ítem (si no tiene displayname, el nombre por defecto)
                Component baseName = meta != null && meta.hasDisplayName()
                        ? plugin.deserialize(meta.getDisplayName())
                        : Component.translatable(item.translationKey());

                // Si cantidad > 1 → "64x Nombre"
                Component shownName = item.getAmount() > 1
                        ? Component.text(item.getAmount() + "x ").append(baseName)
                        : baseName;

                // Hover con nombre, lore y encantamientos
                Component hover = Component.empty()
                        .append(shownName);

                if (meta != null) {
                    boolean more = false;
                    if (meta.hasEnchants()) {
                        for (var e : meta.getEnchants().entrySet()) {
                            String ench = e.getKey().getKey().getKey();
                            int lvl = e.getValue();
                            hover = hover.append(Component.newline()).append(Component.text(ench + " " + lvl, TextColor.color(NamedTextColor.GRAY)));
                        }
                        more = true;
                    }
                    if (meta.hasLore()) {
                        for (String line : meta.getLore()) {
                            hover = hover.append(Component.newline()).append(plugin.deserialize(line));
                        }
                        more = true;
                    }

                    if (more) hover = hover.append(Component.newline());
                    hover = hover.append(Component.newline())
                            .append(Component.text(item.getType().getKey().toString(), NamedTextColor.DARK_GRAY));
                }

                Component message = plugin.getMessage("item",
                        "{item}", PlainTextComponentSerializer.plainText().serialize(shownName));

                Component itemComponent = message.hoverEvent(HoverEvent.showText(hover));

                finalComponent = finalComponent.append(itemComponent);
            } else {
                if (customColor) {
                    finalComponent = finalComponent.append(plugin.deserialize(part));
                } else if (chatcolor != null && !chatcolor.isEmpty()) {
                    finalComponent = finalComponent.append(plugin.deserialize(chatcolor + part));
                } else {
                    finalComponent = finalComponent.append(plugin.deserialize(format + part));
                }
            }
        }
        return finalComponent;
    }

    private Component hoverInv(Player player, String withPlaceholders, boolean customColor, String chatcolor) {
        String[] parts = withPlaceholders.split("(?=\\[inv\\])|(?<=\\[inv\\])");
        Component finalComponent = Component.empty();

        for (String part : parts) {
            if (part.equalsIgnoreCase("[inv]")) {
                String sha1 = InventoryManager.registerInventory(player);

                Component itemComponent = plugin.getMessage("inv")
                        .hoverEvent(HoverEvent.showText(plugin.getMessage("inv_hover")))
                        .clickEvent(ClickEvent.runCommand("/customchat viewinv " + sha1));

                finalComponent = finalComponent.append(itemComponent);
            } else {
                if (customColor) {
                    finalComponent = finalComponent.append(plugin.deserialize(part));
                } else if (chatcolor != null && !chatcolor.isEmpty()) {
                    finalComponent = finalComponent.append(plugin.deserialize(chatcolor + part));
                } else {
                    finalComponent = finalComponent.append(plugin.deserialize(format + part));
                }
            }
        }
        return finalComponent;
    }

    private Component hoverEnder(Player player, String withPlaceholders, boolean customColor, String chatcolor) {
        String[] parts = withPlaceholders.split("(?=\\[ender\\])|(?<=\\[ender\\])");
        Component finalComponent = Component.empty();

        for (String part : parts) {
            if (part.equalsIgnoreCase("[ender]")) {
                String sha1 = InventoryManager.registerEnderChest(player);

                Component itemComponent = plugin.getMessage("ender")
                        .hoverEvent(HoverEvent.showText(plugin.getMessage("ender_hover")))
                        .clickEvent(ClickEvent.runCommand("/customchat viewender " + sha1));

                finalComponent = finalComponent.append(itemComponent);
            } else {
                if (customColor) {
                    finalComponent = finalComponent.append(plugin.deserialize(part));
                } else if (chatcolor != null && !chatcolor.isEmpty()) {
                    finalComponent = finalComponent.append(plugin.deserialize(chatcolor + part));
                } else {
                    finalComponent = finalComponent.append(plugin.deserialize(format + part));
                }
            }
        }
        return finalComponent;
    }

    private Component hoverLocation(Player player, String withPlaceholders, boolean customColor, String chatcolor) {
        String[] parts = withPlaceholders.split("(?=\\[location\\])|(?<=\\[location\\])");
        Component finalComponent = Component.empty();

        for (String part : parts) {
            if (part.equalsIgnoreCase("[location]")) {
                String location = plugin.messagesFile.getConfig().getString("messages.location_placeholder", "&cMessage not found: location_placeholder")
                        .replace("{world}", player.getWorld().getName())
                        .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                        .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                        .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
                Component itemComponent = plugin.getMessage("location", "{location}", location)
                        .hoverEvent(HoverEvent.showText(plugin.getMessage("location_hover")))
                        .clickEvent(ClickEvent.copyToClipboard(location));

                finalComponent = finalComponent.append(itemComponent);
            } else {
                if (customColor) {
                    finalComponent = finalComponent.append(plugin.deserialize(part));
                } else if (chatcolor != null && !chatcolor.isEmpty()) {
                    finalComponent = finalComponent.append(plugin.deserialize(chatcolor + part));
                } else {
                    finalComponent = finalComponent.append(plugin.deserialize(format + part));
                }
            }
        }
        return finalComponent;
    }

    private Component hoverPing(Player player, String withPlaceholders, boolean customColor, String chatcolor) {
        String[] parts = withPlaceholders.split("(?=\\[ping\\])|(?<=\\[ping\\])");
        Component finalComponent = Component.empty();

        for (String part : parts) {
            if (part.equalsIgnoreCase("[ping]")) {
                int ping = player.getPing();
                String pingColor;
                if (ping < 80) {
                    pingColor = "&a";
                } else if (ping < 150) {
                    pingColor = "&e";
                } else if (ping < 300) {
                    pingColor = "&c";
                } else {
                    pingColor = "&4";
                }
                Component itemComponent = plugin.getMessage("ping", "{ping}", String.valueOf(ping), "{ping_color}", pingColor)
                        .hoverEvent(HoverEvent.showText(plugin.getMessage("ping_hover", "{ping}", String.valueOf(ping), "{ping_color}", pingColor)));

                finalComponent = finalComponent.append(itemComponent);
            } else {
                if (customColor) {
                    finalComponent = finalComponent.append(plugin.deserialize(part));
                } else if (chatcolor != null && !chatcolor.isEmpty()) {
                    finalComponent = finalComponent.append(plugin.deserialize(chatcolor + part));
                } else {
                    finalComponent = finalComponent.append(plugin.deserialize(format + part));
                }
            }
        }
        return finalComponent;
    }

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    public static void mention(Player player, Player target, Component content) {
        if (player.equals(target)) return;

        String message = PLAIN.serialize(content);
        if (!message.contains(target.getName())) return;

        boolean enabled = CustomChat.getInstance().getMentionEnabled(target);
        if (!enabled) return;

        boolean needTo = CustomChat.getInstance().getConfig().getBoolean("mentioning.requires_@");
        if (needTo && !message.contains("@"+target.getName())) return;

        String sound = CustomChat.getInstance().getConfig().getString("mentioning.sound", "BLOCK_NOTE_BLOCK_PLING");
        boolean hasSound = !sound.isEmpty();
        if (hasSound) {
            Sound finalSound;
            try {
                finalSound = Sound.valueOf(sound.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                finalSound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }
            target.playSound(target.getLocation(), finalSound, 1, 1);
        }

        String title = CustomChat.getInstance().getConfig()
                .getString("mentioning.title.title", "&b{player} &ementioned you")
                .replace("{player}", player.getName());
        boolean hasTitle = !title.isEmpty();
        String subTitle = CustomChat.getInstance().getConfig().getString("mentioning.title.subtitle", "");
        boolean hasSubTitle = !subTitle.isEmpty();

        int fadeIn = CustomChat.getInstance().getConfig().getInt("mentioning.title.fadeIn", 10);
        int stay = CustomChat.getInstance().getConfig().getInt("mentioning.title.stay", 40);
        int fadeOut = CustomChat.getInstance().getConfig().getInt("mentioning.title.fadeOut", 10);

        if (hasTitle || hasSubTitle) {
            target.sendTitle(ChatColor.translateAlternateColorCodes('&', title),
                    ChatColor.translateAlternateColorCodes('&', subTitle),
                    fadeIn, stay, fadeOut);
        }

        String mentionMessage = CustomChat.getInstance().getConfig()
                .getString("mentioning.message", "&b{player} &ementioned you. You can toggle this notification with &b/customchat mentiontoggle")
                .replace("{player}", player.getName());
        boolean hasmentionMessage = !mentionMessage.isEmpty();
        if (hasmentionMessage) {
            target.sendMessage(CustomChat.getInstance().deserialize(mentionMessage));
        }
    }
}

