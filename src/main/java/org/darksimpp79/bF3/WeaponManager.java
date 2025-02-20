package org.darksimpp79.bF3;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WeaponManager implements Listener {
    private final List<Weapon> weapons = new ArrayList<>();
    private final JavaPlugin plugin;

    public WeaponManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Rejestrujemy bronie – tutaj tylko AK-47; możesz dodać kolejne (M4A4, Bazooka, etc.)
        weapons.add(new AK47(plugin));
        weapons.add(new MachineGun(plugin));
        weapons.add(new Pistol(plugin));
        weapons.add(new M4A4(plugin));
        weapons.add(new Bazooka(plugin));
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = meta.getDisplayName();

        for (Weapon weapon : weapons) {
            if (weapon.getItemMaterial() == item.getType() &&
                    weapon.getDisplayName().equals(displayName)) {
                // Lewy klik → strzał
                if (event.getAction() == Action.LEFT_CLICK_AIR ||
                        event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    weapon.shoot(event.getPlayer());
                    event.setCancelled(true);
                    return;
                }
                // Prawy klik → reload (manualny reload)
                if (event.getAction() == Action.RIGHT_CLICK_AIR ||
                        event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    weapon.reload(event.getPlayer());
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        for (Weapon weapon : weapons) {
            if (event.getEntity().hasMetadata(weapon.getBulletMetadata())) {
                weapon.onBulletHit(event);
                break;
            }
        }
    }
}
