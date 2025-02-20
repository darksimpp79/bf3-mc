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

public class AK47 extends AbstractWeapon {
    public static final String NAME = "§a AK-47";
    public static final Material ITEM_MATERIAL = Material.STONE_HOE;
    public static final int MAGAZINE_SIZE = 30;
    public static final int DAMAGE = 8;
    public static final long RELOAD_TIME = 60L; // 60 ticków = 3 sekundy
    public static final String BULLET_METADATA = "AK-47_BULLET";

    public AK47(JavaPlugin plugin) {
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
        int currentMag = getCurrentMagazine(player);
        if (currentMag <= 0) {
            player.sendActionBar(Component.text("⚠ Brak amunicji!").color(NamedTextColor.RED));
            reload(player);
            return;
        }
        Snowball bullet = player.launchProjectile(Snowball.class);
        bullet.setVelocity(player.getLocation().getDirection().multiply(2));
        bullet.setMetadata(BULLET_METADATA, new FixedMetadataValue(plugin, true));

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 1.2F);
        player.spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);

        setCurrentMagazine(player, currentMag - 1);
        player.sendActionBar(Component.text("🔫 Amunicja: " + (currentMag - 1) + "/" + MAGAZINE_SIZE)
                .color(NamedTextColor.GREEN));

        if (getCurrentMagazine(player) <= 0) {
            reload(player);
        }
    }

    @Override
    public void onBulletHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata(BULLET_METADATA)) {
            event.getEntity().getWorld().spawnParticle(Particle.EXPLOSION, event.getEntity().getLocation(), 1);
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.damage(DAMAGE);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 10, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    @Override
    public ItemStack getAmmoItem(int amount) {
        ItemStack item = new ItemStack(Material.YELLOW_DYE, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e Amunicja AK-47");
            item.setItemMeta(meta);
        }
        return item;
    }
}
