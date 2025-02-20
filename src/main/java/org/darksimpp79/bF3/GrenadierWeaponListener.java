package org.darksimpp79.bF3;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class GrenadierWeaponListener implements Listener {

    // Mapa liczby zabójstw przy użyciu granatnika
    private final HashMap<UUID, Integer> killCount = new HashMap<>();
    // Mapa do przechowywania czasu aktywacji aury (opcjonalnie)
    private final HashMap<UUID, Long> auraActive = new HashMap<>();
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player killer) {
            ItemStack item = killer.getInventory().getItemInMainHand();
            if (item.getType() == Material.TNT &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§c Granatnik")) {

                UUID killerId = killer.getUniqueId();
                int kills = killCount.getOrDefault(killerId, 0) + 1;
                killCount.put(killerId, kills);
                killer.sendMessage("§aZabiłeś: " + kills + " graczy!");

                // Po 3 zabójstwach aktywujemy aurę eksplozji
                if (kills == 1) {
                    activateExplosionAura(killer);
                    killCount.put(killerId, 0); // Reset licznika
                }
            }
        }
    }

    /**
     * Aktywacja aury eksplozji – gracz otrzymuje dodatkowe efekty na 30 sekund,
     * w tym wizualne (efekt pioruna) oraz zwiększone obrażenia i spowolnienie.
     */
    private void activateExplosionAura(Player player) {
        auraActive.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§eTwoja aura eksplozji została aktywowana na 30 sekund!");

        // Efekty wizualne i dźwiękowe
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Na 30 sekund: zwiększenie obrażeń i lekkie spowolnienie (efekt "recoil")
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 1)); // 600 ticków = 30 s
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 1));

        // Dodatkowy efekt – przez 5 sekund dodatkowe obrażenia
        Bukkit.getScheduler().runTaskLater(BF3.getInstance(), () -> {
            player.sendMessage("§cAura eksplozji wzmocniona na 5 sekund!");
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 3));
        }, 100L); // 100 ticków = 5 s
    }
}
