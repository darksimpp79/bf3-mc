package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Bazooka extends AbstractWeapon {
    public static final String NAME = "§c Bazooka";
    public static final Material ITEM_MATERIAL = Material.BLAZE_ROD;
    public static final int MAGAZINE_SIZE = 1;
    public static final int DAMAGE = 20;
    public static final long RELOAD_TIME = 80L; // 80 ticków
    public static final String BULLET_METADATA = "BAZOOKA_BULLET";

    public Bazooka(JavaPlugin plugin) {
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
        Fireball bullet = player.launchProjectile(Fireball.class);
        bullet.setShooter(player);
        bullet.setVelocity(player.getLocation().getDirection().multiply(1.5));
        bullet.setMetadata(BULLET_METADATA, new FixedMetadataValue(plugin, true));

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F);
        player.spawnParticle(Particle.GUST_EMITTER_LARGE, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.1);

        setCurrentMagazine(player, currentAmmo - 1);
        player.sendActionBar(Component.text("🔫 Amunicja: " + (currentAmmo - 1) + "/" + MAGAZINE_SIZE)
                .color(NamedTextColor.GREEN));

        if (getCurrentMagazine(player) <= 0) {
            reload(player);
        }
    }

    @Override
    public void onBulletHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata(BULLET_METADATA)) {
            // Zapobiegamy obrażeniom, jeśli trafiony jest strzelający
            if (event.getEntity() instanceof Fireball) {
                Fireball fireball = (Fireball) event.getEntity();
                if (fireball.getShooter() != null && event.getHitEntity() != null) {
                    if (fireball.getShooter().equals(event.getHitEntity())) {
                        return;
                    }
                }
            }
            Location loc = event.getEntity().getLocation();
            World world = loc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.GUST_EMITTER_LARGE, loc, 1);
                if (event.getHitEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getHitEntity();
                    target.damage(DAMAGE);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                }
                world.createExplosion(loc, 2.0F, false);
            }
        }
    }

    @Override
    public ItemStack getAmmoItem(int amount) {
        ItemStack item = new ItemStack(Material.FIRE_CHARGE, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e Amunicja Bazooka");
            item.setItemMeta(meta);
        }
        return item;
    }
}
