package org.darksimpp79.bF3;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public interface Weapon {
    String getDisplayName();      // np. "§a AK-47"
    Material getItemMaterial();   // Materiał przedmiotu (np. STONE_HOE)
    int getMagazineSize();        // Ilość naboi w magazynku
    int getDamage();              // Obrażenia zadawane przez pocisk
    long getReloadTime();         // Czas przeładowania (ticki)
    String getBulletMetadata();   // Unikalny identyfikator metadanych pocisku

    // Metody operujące na broni:
    void shoot(Player player);
    void reload(Player player);
    void onBulletHit(ProjectileHitEvent event);

    // Metoda zwracająca przedmiot reprezentujący amunicję tej broni
    ItemStack getAmmoItem(int amount);
}
