package org.darksimpp79.bF3;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class AmmoDropListener implements Listener {

    private final WeaponManager weaponManager;

    public AmmoDropListener(WeaponManager weaponManager) {
        this.weaponManager = weaponManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // Usuwamy standardowe dropy (jeśli chcesz, aby wypadała tylko amunicja)
        event.getDrops().clear();

        // Dla każdej broni sprawdzamy, czy gracz miał przyznaną amunicję
        for (Weapon weapon : weaponManager.getWeapons()) {
            if (weapon instanceof AbstractWeapon) {
                AbstractWeapon aw = (AbstractWeapon) weapon;
                int ammoCount = aw.getCurrentMagazine(player);
                if (ammoCount > 0) {
                    // Dodajemy do dropów przedmiot reprezentujący amunicję tej broni
                    ItemStack ammoDrop = weapon.getAmmoItem(ammoCount);
                    event.getDrops().add(ammoDrop);
                    // Resetujemy amunicję dla gracza (możesz ustawić 0, jeśli ma być tracona)
                    aw.setCurrentMagazine(player, 0);
                }
            }
        }
        player.sendMessage("§7Zginąłeś! Wypada amunicja Twoich broni.");
    }
}
