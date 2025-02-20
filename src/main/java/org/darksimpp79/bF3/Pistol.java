package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Pistol extends AbstractWeapon {
    public static final String NAME = "§e Pistolet";
    public static final Material ITEM_MATERIAL = Material.GOLDEN_HOE;
    public static final int MAGAZINE_SIZE = 15;
    public static final int DAMAGE = 5;
    public static final long RELOAD_TIME = 40L; // 40 ticków
    public static final String BULLET_METADATA = "PISTOL_BULLET";

    public Pistol(JavaPlugin plugin) {
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
        Arrow bullet = player.launchProjectile(Arrow.class);
        bullet.setVelocity(player.getLocation().getDirection().normalize().multiply(2));
        bullet.setMetadata(BULLET_METADATA, new FixedMetadataValue(plugin, true));

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.2F);
        player.spawnParticle(Particle.WHITE_SMOKE, player.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);

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
        ItemStack item = new ItemStack(Material.ARROW, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e Amunicja Pistolet");
            item.setItemMeta(meta);
        }
        return item;
    }
}
