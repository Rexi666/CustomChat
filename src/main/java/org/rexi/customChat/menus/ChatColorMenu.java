package org.rexi.customChat.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.rexi.customChat.CustomChat;

import java.util.*;

public class ChatColorMenu{

    private final CustomChat plugin;
    private final Player player;

    public ChatColorMenu(CustomChat plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void openMainMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, plugin.deserialize(plugin.getChatColorString("menu.main.title")));

        ItemStack colors = createItem(
                plugin.getChatColorString("chatcolor.colors.material"),
                plugin.deserialize(plugin.getChatColorString("chatcolor.colors.name")),
                plugin.getChatColorList("menu.main.lore_color"),
                1001
        );

        ItemStack gradients = createItem(
                plugin.getChatColorString("chatcolor.gradients.material"),
                plugin.deserialize(plugin.getChatColorString("chatcolor.gradients.name")),
                plugin.getChatColorList("menu.main.lore_gradient"),
                1002
        );

        ItemStack removecurrent = createItem(
                plugin.getChatColorString("removecurrent.material"),
                plugin.deserialize(plugin.getChatColorString("removecurrent.name")),
                plugin.getChatColorList("removecurrent.lore"),
                1003
        );

        if (plugin.getChatColorConfig().getBoolean("chatcolor.gradients_enabled")) {
            inv.setItem(11, colors);
            inv.setItem(15, gradients);
        } else {
            inv.setItem(13, colors);
        }

        if (plugin.getChatColorConfig().getBoolean("removecurrent.enabled")) {
            inv.setItem(22, removecurrent);
        }

        player.openInventory(inv);
    }

    public void openColorSubMenu() {
        ConfigurationSection colorsSection = plugin.getChatColorConfig().getConfigurationSection("chatcolor.colors.colors");
        if (colorsSection == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, plugin.deserialize(plugin.getChatColorString("menu.submenus.submenu_color-title")));

        int custommodeldata = 10001;
        for (String key : colorsSection.getKeys(false)) {
            String permission = "customchat.colorchat.color." + key;
            boolean hasPermission = player.hasPermission(permission);
            String material = colorsSection.getString(key + ".material");
            Component name = plugin.deserialize(colorsSection.getString(key + ".name", "&7Color"));
            List<String> lorePath = plugin.getChatColorList(hasPermission ? "menu.submenus.lore_color-unlocked" : "menu.submenus.lore_color-blocked");

            ItemStack item = createItem(material, name, lorePath, custommodeldata);
            inv.addItem(item);

            custommodeldata++;
        }

        ConfigurationSection backSection = plugin.getChatColorConfig().getConfigurationSection("back");
        if (backSection != null && backSection.getBoolean("enabled")) {
            String material = backSection.getString("material", "ARROW");
            Component name = plugin.deserialize(backSection.getString("name", "&7Back"));
            List<String> lorePath = plugin.getChatColorList("back.lore");

            ItemStack item = createItem(material, name, lorePath, 10500);
            inv.setItem(45, item);
        }

        player.openInventory(inv);
    }

    public void openGradientSubMenu() {
        ConfigurationSection gradientsSection = plugin.getChatColorConfig().getConfigurationSection("chatcolor.gradients.colors");
        if (gradientsSection == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, plugin.deserialize(plugin.getChatColorString("menu.submenus.submenu_gradient-title")));

        int custommodeldata = 10001;
        for (String key : gradientsSection.getKeys(false)) {
            String permission = "customchat.colorchat.gradient." + key;
            boolean hasPermission = player.hasPermission(permission);
            String material = gradientsSection.getString(key + ".material");
            Component name = plugin.deserialize(gradientsSection.getString(key + ".name"));
            List<String> lorePath = plugin.getChatColorList(hasPermission ? "menu.submenus.lore_gradient-unlocked" : "menu.submenus.lore_gradient-blocked");

            ItemStack item = createItem(material, name, lorePath, custommodeldata);
            inv.addItem(item);

            custommodeldata++;
        }

        ConfigurationSection backSection = plugin.getChatColorConfig().getConfigurationSection("back");
        if (backSection != null && backSection.getBoolean("enabled")) {
            String material = backSection.getString("material", "ARROW");
            Component name = plugin.deserialize(backSection.getString("name", "&7Back"));
            List<String> lorePath = plugin.getChatColorList("back.lore");

            ItemStack item = createItem(material, name, lorePath, 10500);
            inv.setItem(45, item);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(String material, Component name, List<String> lore, int customModelData) {
        ItemStack item;
        if (material.startsWith("basehead-")) {
            String base64 = material.substring("basehead-".length());
            try {
                item = headFromBase64(base64);
            } catch (Exception ex) {
                plugin.getLogger().warning("Invalid base64 head for material: " + material);
                item = new ItemStack(Material.PLAYER_HEAD);
            }
        } else {
            Material mat;
            try {
                mat = Material.valueOf(Optional.ofNullable(material).orElse("STONE").toUpperCase());
            } catch (IllegalArgumentException e) {
                mat = Material.STONE;
            }
            item = new ItemStack(mat);
        }

        ItemMeta meta = item.getItemMeta();

        meta.displayName(name.decoration(TextDecoration.ITALIC, false));

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(plugin.deserialize(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreLines);

        if (customModelData != -1) {
            meta.setCustomModelData(customModelData);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack headFromBase64(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64 == null || base64.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return head;
    }
}
