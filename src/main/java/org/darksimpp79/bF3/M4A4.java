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

public class M4A4 extends AbstractWeapon {
    public static final String NAME = "§b M4A4";
    public static final Material ITEM_MATERIAL = Material.IRON_HOE;
    public static final int MAGAZINE_SIZE = 30;
    public static final int DAMAGE = 7;
    public static final long RELOAD_TIME = 50L; // 50 ticków
    public static final String BULLET_METADATA = "M4A4_BULLET";

    public M4A4(JavaPlugin plugin) {
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
        Snowball bullet = player.launchProjectile(Snowball.class);
        bullet.setVelocity(player.getLocation().getDirection().multiply(2));
        bullet.setMetadata(BULLET_METADATA, new FixedMetadataValue(plugin, true));

        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.9F, 1.3F);
        player.spawnParticle(Particle.FLAME, player.getLocation(), 7, 0.3, 0.3, 0.3, 0.02);

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
            event.getEntity().getWorld().spawnParticle(Particle.FLAME, event.getEntity().getLocation(), 5);
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.damage(DAMAGE);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 8, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    @Override
    public ItemStack getAmmoItem(int amount) {
        ItemStack item = new ItemStack(Material.ORANGE_DYE, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e Amunicja M4A4");
            item.setItemMeta(meta);
        }
        return item;
    }
}
