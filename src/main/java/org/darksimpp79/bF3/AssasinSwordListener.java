package org.darksimpp79.bF3;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

class AssassinSwordListener implements Listener {
    private final HashMap<UUID, Integer> killCount = new HashMap<>();
    private final HashMap<UUID, Long> auraActive = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if ((event.getAction().toString().contains("RIGHT_CLICK")) &&
                item.getType() == Material.IRON_SWORD &&
                item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals("§c Miecz Assasin'a")) {

            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 360, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 360, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 3));
            player.sendMessage("§bRęce do góry. Zobacz na swoje serca.");
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player killer) {
            ItemStack item = killer.getInventory().getItemInMainHand();
            if (item.getType() == Material.IRON_SWORD &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§c Miecz Assasin'a")) {

                killCount.put(killer.getUniqueId(), killCount.getOrDefault(killer.getUniqueId(), 0) + 1);
                killer.sendMessage("§aZabiłeś: " + killCount.get(killer.getUniqueId()) + " graczy!");

                // Aktywacja aury po 3 zabójstwach
                if (killCount.get(killer.getUniqueId()) == 1) {
                    activateAura(killer);
                    killCount.put(killer.getUniqueId(), 0); // Reset zabójstw
                }
            }
        }
    }

    private void activateAura(Player player) {
        auraActive.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§eTwoja aura została aktywowana na 30 sekund!");

        // Efekty wizualne i dźwiękowe
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);

        // Efekty na 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 2));

        // Timer na 5 sekund dla "dwóch hitów"
        Bukkit.getScheduler().runTaskLater(BF3.getInstance(), () -> {
            player.sendMessage("§cPrzez 5 sekund możesz zabijać na 2 hity!");
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 4));
        }, 100L); // 5 sekund (100 ticków)
    }
}

