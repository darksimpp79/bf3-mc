package org.darksimpp79.bF3;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

public class Apteczka implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if ((event.getAction().toString().contains("RIGHT_CLICK")) &&
                item.getType() == Material.BONE_MEAL &&
                item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals("§c Apteczka")) {

            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 100, 20));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 30));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 20));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 30));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 10, 30));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 30));
            player.sendMessage("§bExtra HP.");
        }
    }
}
