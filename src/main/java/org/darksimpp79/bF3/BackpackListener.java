package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackListener implements Listener {

    // Mapa przechowująca plecaki graczy
    private final Map<UUID, Inventory> plecaki = new HashMap<>();

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        UUID playerId = player.getUniqueId();

        // Sprawdź, czy kliknięto PPM w powietrze lub blok
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.CHEST && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                // Pobieranie nazwy przedmiotu w poprawny sposób
                if (meta.hasDisplayName()) {
                    String itemName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());

                    if (itemName.equals("§a Plecak")) {
                        event.setCancelled(true); // Zapobiega otwieraniu bloku
                        openBackpack(player);
                    }
                }
            }
        }
    }

    private void openBackpack(Player player) {
        UUID playerId = player.getUniqueId();

        // Sprawdzenie, czy plecak już istnieje
        if (!plecaki.containsKey(playerId)) {
            Inventory backpack = Bukkit.createInventory(null, 9, "§a Twój Plecak");
            plecaki.put(playerId, backpack);
        }

        // Otwórz plecak
        player.openInventory(plecaki.get(playerId));
    }
}
