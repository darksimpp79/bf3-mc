package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MachineGun extends AbstractWeapon {
    public static final String NAME = "§e Machine Gun";
    public static final Material ITEM_MATERIAL = Material.DIAMOND_HOE;
    public static final int MAGAZINE_SIZE = 50;
    public static final int DAMAGE = 3;
    public static final long RELOAD_TIME = 40L; // 40 ticków
    public static final String BULLET_METADATA = "MACHINEGUN_BULLET";

    public MachineGun(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }

    @Override
    public Material getItemMaterial() {
        return ITEM_MATERIAL;
    }

    @Override
    public int getMagazineSize() {
        return MAGAZINE_SIZE;
    }

    @Override
    public int getDamage() {
        return DAMAGE;
    }

    @Override
    public long getReloadTime() {
        return RELOAD_TIME;
    }

    @Override
    public String getBulletMetadata() {
        return BULLET_METADATA;
    }

    @Override
    public void shoot(Player player) {
        if (isReloading(player)) {
            player.sendActionBar(Component.text("⏳ Przeładowanie...").color(NamedTextColor.YELLOW));
            return;
        }
        int currentAmmo = getCurrentMagazine(player);
        if (currentAmmo <= 0) {
            player.sendActionBar(Component.text("⚠ Brak amunicji!").color(NamedTextColor.RED));
            reload(player);
            return;
        }
        // Szybkie strzelanie – seria 10 pocisków w krótkich odstępach
        new BukkitRunnable() {
            int shotsFired = 0;
            @Override
            public void run() {
                if (shotsFired >= 10 || getCurrentMagazine(player) <= 0) {
                    cancel();
                    if (getCurrentMagazine(player) <= 0) {
                        reload(player);
                    }
                    return;
                }
                Snowball bullet = player.launchProjectile(Snowball.class);
                bullet.setVelocity(player.getLocation().getDirection().multiply(3));
                bullet.setMetadata(BULLET_METADATA, new FixedMetadataValue(plugin, true));
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 2.0F);
                player.spawnParticle(Particle.SMOKE, player.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);
                setCurrentMagazine(player, getCurrentMagazine(player) - 1);
                player.sendActionBar(Component.text("🔫 Amunicja: " + getCurrentMagazine(player) + "/" + MAGAZINE_SIZE)
                        .color(NamedTextColor.GREEN));
                shotsFired++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void onBulletHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata(BULLET_METADATA)) {
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.damage(DAMAGE);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 5, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    @Override
    public ItemStack getAmmoItem(int amount) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e Amunicja Machine Gun");
            item.setItemMeta(meta);
        }
        return item;
    }
}
