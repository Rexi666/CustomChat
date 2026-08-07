package org.rexi.customChat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.rexi.customChat.CustomChat;

public class DeathListener implements Listener {

    private final CustomChat plugin;

    public DeathListener(CustomChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();

        String message;

        if (lastDamage == null) {
            message = plugin.getConfig().getString("death_messages.default");
        } else {
            switch (lastDamage.getCause()) {
                case FALL:
                    message = plugin.getConfig().getString("death_messages.fall");
                    break;
                case LAVA:
                    message = plugin.getConfig().getString("death_messages.lava");
                    break;
                case FIRE:
                    message = plugin.getConfig().getString("death_messages.fire");
                    break;
                case FIRE_TICK:
                    message = plugin.getConfig().getString("death_messages.fire_tick");
                    break;
                case DROWNING:
                    message = plugin.getConfig().getString("death_messages.drowning");
                    break;
                case SUFFOCATION:
                    message = plugin.getConfig().getString("death_messages.suffocation");
                    break;
                case VOID:
                    message = plugin.getConfig().getString("death_messages.void");
                    break;
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    message = plugin.getConfig().getString("death_messages.explosion");
                    break;
                case ENTITY_ATTACK:
                    message = plugin.getConfig().getString("death_messages.entity_attack");
                    break;
                case PROJECTILE:
                    message = plugin.getConfig().getString("death_messages.projectile");
                    break;
                case MAGIC:
                    message = plugin.getConfig().getString("death_messages.magic");
                    break;
                case LIGHTNING:
                    message = plugin.getConfig().getString("death_messages.lightning");
                    break;
                case FREEZE:
                    message = plugin.getConfig().getString("death_messages.freeze");
                    break;
                case STARVATION:
                    message = plugin.getConfig().getString("death_messages.starvation");
                    break;
                default:
                    message = plugin.getConfig().getString("death_messages.default");
                    break;
            }
        }

        Player killer = event.getEntity().getKiller();

        String killerPlaceholder = "";
        if (killer != null) {
            killerPlaceholder = plugin.getConfig()
                    .getString("death_messages.placeholder_killer", "by {player}")
                    .replace("{player}", killer.getName());
        }

        event.deathMessage(plugin.deserialize(
                message.replace("{player}", event.getEntity().getName())
                        .replace("{killer}", killerPlaceholder)
        ));
    }
}
