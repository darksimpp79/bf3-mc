package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ClassSelectionListener implements Listener {

    // Mapa przechowująca wybraną klasę gracza
    private final Map<UUID, String> playerClasses = new HashMap<>();

    // Mapa z zestawami ekwipunku (kitami) dla poszczególnych klas
    private final Map<String, ItemStack[]> classKits = new HashMap<>();

    // Inicjalizacja kitów – dodaliśmy także nową klasę Widmo
    public ClassSelectionListener() {
        BF3 plugin = BF3.getInstance();

        // Zestaw dla klasy "Assassin"
        classKits.put("Assassin", new ItemStack[]{
                plugin.stworzCustomItem(Material.IRON_SWORD, "§c Miecz Assasin'a", 1, "§aKliknij 2 razy PPM", "§7CHUJ CI W DUPE!"),
                plugin.stworzCustomItem(Material.STONE_HOE, "§a AK-47", 1, "§a Pif-paf"),
                plugin.stworzCustomItem(Material.GOLD_INGOT, "§aSkarbonka", 1, "§a Stan konta: 1000"),
                plugin.stworzCustomItem(Material.CHEST, "§a Plecak", 1, "§a Miejsce: 0"),
                plugin.stworzCustomItem(Material.BONE_MEAL, "§c Apteczka", 1, "§a Extra Life"),
                plugin.stworzCustomItem(Material.YELLOW_DYE, "§e Amunicja", 70, "§aAmunicja AK-47")
        });

        // Zestaw dla klasy "Medic"
        classKits.put("Medic", new ItemStack[]{
                plugin.stworzCustomItem(Material.GOLDEN_APPLE, "§a Apteczka", 3, "§a Leczenie")
        });

        // Zestaw dla klasy "Grenadier"
        classKits.put("Grenadier", new ItemStack[]{
                plugin.stworzCustomItem(Material.TNT, "§c Granatnik", 1, "§a Wystrzel granat (co 10s)"),
                plugin.stworzCustomItem(Material.DIAMOND_HOE, "§e Machine Gun", 1, "§a Szybkie strzały", "§a Wysoki RPM"),
                // plugin.stworzCustomItem(Material.BLAZE_ROD, "§c Bazooka", 1)
                plugin.stworzCustomItem(Material.IRON_NUGGET, "§e Amunicja Machine Gun", 200)
                // plugin.stworzCustomItem(Material.FIRE_CHARGE, "§e Amunicja Bazooka", 6)
        });

        // Nowy zestaw dla klasy "Widmo"
        // Nowy zestaw dla klasy "Widmo"
        classKits.put("Widmo", new ItemStack[]{
                // Specjalna kosa – po trafieniu wywoła nudności i dodatkowy efekt osłabienia
                plugin.stworzCustomItem(Material.DIAMOND_HOE, "§c Kosa Widma", 1, "§a Uderz, by wywołać nudności", "§a Dodatkowy efekt: osłabienie"),
                // Przedmiot, którego nie chcemy zużywać – "Mroczny Cień"
                plugin.stworzCustomItem(Material.ENDER_PEARL, "§5 Mroczny Cień", 1, "§a Niezużywalny przedmiot"),
                // Totem Psa – służy do ręcznego przywoływania psa (cooldown 1 minuta)
                plugin.stworzCustomItem(Material.NETHER_STAR, "§6 Totem Psa", 1, "§a Użyj, by przywołać psa Widma", "§c Cooldown: 1 minuta")
        });
    }

    /**
     * Tworzy GUI wyboru klasy.
     */
    private Inventory getClassSelectionInventory() {
        Inventory inventory = Bukkit.createInventory(null, 9, "§a Wybierz swoją klasę");
        inventory.setItem(3, createClassItem(Material.ENDER_PEARL, "Widmo", "👻 Duchowa moc", "⚡ Ultry: przywołanie psa", "✨ Faza przez bloki"));
        inventory.setItem(1, createClassItem(Material.IRON_SWORD, "Assassin", "⚔ Szybki atak", "💨 Mobilność"));
        inventory.setItem(5, createClassItem(Material.GOLDEN_APPLE, "Medic", "💉 Leczenie", "🛡 Wsparcie"));
        inventory.setItem(7, createClassItem(Material.TNT, "Grenadier", "💥 Obrażenia obszarowe", "🔥 Siła wybuchu"));
        // Dodajemy ikonę dla klasy Widmo – przykładowo Ender Pearl
        return inventory;
    }

    /**
     * Tworzy przedmiot reprezentujący klasę w GUI.
     */
    private ItemStack createClassItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Dodajemy prefiks "§e" do nazwy – przy wyborze usuniemy go
            meta.displayName(Component.text("§e" + name));
            List<Component> loreComponents = Arrays.stream(lore)
                    .map(Component::text)
                    .collect(Collectors.toList());
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerClasses.containsKey(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(BF3.getInstance(), () -> player.openInventory(getClassSelectionInventory()), 20L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;
        if (!event.getView().getTitle().equals("§a Wybierz swoją klasę")) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Pobieramy wybraną klasę (usuwając prefiks "§e")
        String displayName = meta.getDisplayName();
        String selectedClass = displayName.replace("§e", "");
        if (!classKits.containsKey(selectedClass)) {
            player.sendMessage(Component.text("Nieznana klasa!").color(NamedTextColor.RED));
            return;
        }

        playerClasses.put(player.getUniqueId(), selectedClass);
        assignClassKit(player, selectedClass);

        if ("Assassin".equals(selectedClass)) {
            // inicjalizacja amunicji itp.

        } else if ("Grenadier".equals(selectedClass)) {
            // inicjalizacja Machine Gun (przykładowo)

        } else if ("Widmo".equals(selectedClass)) {
            // Rejestrujemy gracza jako Widmo – dzięki temu nasz WidmoListener zacznie obsługiwać jego ultry
            WidmoListener.addWidmo(player);
            player.sendActionBar(Component.text("👻 Widmo: aktywowane").color(NamedTextColor.LIGHT_PURPLE));
        }

        player.sendMessage(Component.text("Wybrałeś klasę: " + selectedClass).color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("📦 Otrzymałeś spersonalizowany ekwipunek!").color(NamedTextColor.GOLD));
        player.closeInventory();
    }

    private void assignClassKit(Player player, String selectedClass) {
        ItemStack[] kit = classKits.get(selectedClass);
        if (kit != null) {
            player.getInventory().clear();
            for (ItemStack item : kit) {
                player.getInventory().addItem(item);
            }
        }
        player.setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        playerClasses.remove(player.getUniqueId());
        WidmoListener.removeWidmo(player);
        player.clearActivePotionEffects();
        player.getInventory().clear();
        Bukkit.getScheduler().runTaskLater(BF3.getInstance(), () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.openInventory(getClassSelectionInventory());
        }, 10L);
    }
}
