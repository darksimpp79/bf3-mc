package org.darksimpp79.bF3;

import org.bukkit.*;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GrenadeListener implements Listener {

    @EventHandler
    public void onGrenadeUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return;

        switch (name) {
            case "§c Granat Wybuchowy": handleGrenade(player, "explosive"); break;
            case "§a Granat Palący": handleGrenade(player, "incendiary"); break;
            case "§2 Granat Trujący": handleGrenade(player, "toxic"); break;
            case "§e Granat Dymny": handleGrenade(player, "smoke"); break;
            default: return;
        }
        consumeGrenade(player);
        event.setCancelled(true);
    }

    private void consumeGrenade(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private void handleGrenade(Player player, String type) {
        TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
        tnt.setFuseTicks(60);
        tnt.setVelocity(player.getLocation().getDirection().multiply(1.5));
        tnt.setYield(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = tnt.getLocation();
                switch (type) {
                    case "explosive":
                        loc.getWorld().createExplosion(loc, 4.0F, false, false);
                        player.sendMessage("Granat wybuchowy eksplodował!");
                        break;
                    case "incendiary":
                        createFire(loc, 5);
                        player.sendMessage("Granat palący wybuchł!");
                        break;
                    case "toxic":
                        createPoisonCloud(loc, 5);
                        player.sendMessage("Granat trujący stworzył strefę toksyczną!");
                        break;
                    case "smoke":
                        createSmoke(loc, 3);
                        player.sendMessage("Granat dymny wytworzył gęstą chmurę!");
                        break;
                }
            }
        }.runTaskLater(BF3.getInstance(), 60L);
    }

    private void createFire(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setType(Material.FIRE);
                    }
                }
            }
        }
    }

    private void createPoisonCloud(Location center, int radius) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 200) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.WITCH, center, 100, radius, radius, radius, 0.1);
                for (Player p : center.getWorld().getPlayers()) {
                    if (p.getLocation().distance(center) <= radius) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    }
                }
            }
        }.runTaskTimer(BF3.getInstance(), 0L, 10L);
    }

    private void createSmoke(Location center, int radius) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 200) {
                    cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.WHITE_SMOKE, center, 500, radius, radius, radius, 0.1);
            }
        }.runTaskTimer(BF3.getInstance(), 0L, 5L);
    }
}
