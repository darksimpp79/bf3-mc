package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractWeapon implements Weapon {
    // Przechowujemy stan magazynu dla danego gracza – ilość naboi załadowanych do broni.
    // Domyślnie, gdy gracz nie wykonał jeszcze reloadu, magazyn wynosi 0.
    protected final Map<UUID, Integer> magazine = new HashMap<>();
    // Flaga informująca, czy dla danego gracza reload jest w toku.
    protected final Map<UUID, Boolean> reloading = new HashMap<>();
    protected final JavaPlugin plugin;

    public AbstractWeapon(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Zwraca aktualną ilość naboi w magazynku.
     * Jeśli gracz jeszcze nie miał ustawionego stanu, zwracamy 0.
     */
    protected int getCurrentMagazine(Player player) {
        return magazine.getOrDefault(player.getUniqueId(), 0);
    }

    protected void setCurrentMagazine(Player player, int value) {
        magazine.put(player.getUniqueId(), value);
    }

    protected boolean isReloading(Player player) {
        return reloading.getOrDefault(player.getUniqueId(), false);
    }

    protected void setReloading(Player player, boolean value) {
        reloading.put(player.getUniqueId(), value);
    }

    @Override
    public void reload(Player player) {
        if (isReloading(player)) {
            player.sendActionBar(Component.text("⏳ Przeładowanie w toku...").color(NamedTextColor.YELLOW));
            return;
        }
        int currentMag = getCurrentMagazine(player);
        int magSize = getMagazineSize();
        int ammoNeeded = magSize - currentMag;

        if (ammoNeeded <= 0) {
            player.sendActionBar(Component.text("✅ Magazynek już pełny!").color(NamedTextColor.GREEN));
            return;
        }

        // Pobieramy informacje o amunicji (typ i nazwa)
        ItemStack ammoRef = getAmmoItem(1);
        Material ammoMat = ammoRef.getType();
        String ammoDisplay = ammoRef.hasItemMeta() && ammoRef.getItemMeta().hasDisplayName()
                ? ammoRef.getItemMeta().getDisplayName()
                : "";

        // Liczymy amunicję w ekwipunku
        int ammoFound = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == ammoMat) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                        item.getItemMeta().getDisplayName().equals(ammoDisplay)) {
                    ammoFound += item.getAmount();
                }
            }
        }
        if (ammoFound < ammoNeeded) {
            player.sendActionBar(Component.text("❌ Brak wystarczającej amunicji!").color(NamedTextColor.RED));
            return;
        }

        // Oznaczamy gracza jako przeładowującego
        setReloading(player, true);
        player.sendActionBar(Component.text("🔄 Przeładowywanie...").color(NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0F, 1.0F);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Usuwamy amunicję dopiero teraz
                int toRemove = ammoNeeded;
                for (int i = 0; i < player.getInventory().getSize() && toRemove > 0; i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item == null) continue;
                    if (item.getType() == ammoMat &&
                            item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            Objects.equals(item.getItemMeta().getDisplayName(), ammoDisplay)) {
                        int count = item.getAmount();
                        if (count <= toRemove) {

                            toRemove -= count;

                            player.getInventory().setItem(i, null);
                        } else {
                            item.setAmount(count - toRemove);
                            toRemove = 0;
                        }
                    }
                }

                // Aktualizacja magazynka
                setCurrentMagazine(player, magSize);
                setReloading(player, false);
                player.sendActionBar(Component.text("🔫 " + getDisplayName() + " przeładowany! Amunicja: " + magSize)
                        .color(NamedTextColor.GREEN));
            }
        }.runTaskLater(plugin, getReloadTime());
    }

    @Override
    public void shoot(Player player) {
        if (isReloading(player)) {
            player.sendActionBar(Component.text("⏳ Przeładowanie w toku...").color(NamedTextColor.YELLOW));
            return;
        }
        int currentMag = getCurrentMagazine(player);
        if (currentMag <= 0) {
            player.sendActionBar(Component.text("❌ Brak amunicji!").color(NamedTextColor.RED));
            return;
        }
        setCurrentMagazine(player, currentMag - 1);
        player.sendActionBar(Component.text("🔫 Strzał! Pozostała amunicja: " + (currentMag - 1) + "/" + getMagazineSize())
                .color(NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
    }
}
