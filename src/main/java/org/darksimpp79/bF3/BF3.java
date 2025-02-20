package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

public final class BF3 extends JavaPlugin implements Listener {
    private static BF3 instance;
    private static final int MAX_JUMPS = 2; // Maksymalna liczba skoków
    private Map<UUID, Integer> playerJumps = new HashMap<>(); // Liczba skoków dla każdego gracza

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new AssassinSwordListener(), this);
        getServer().getPluginManager().registerEvents(new BackpackListener(), this);
        getServer().getPluginManager().registerEvents(new ClassSelectionListener(), this);
        getServer().getPluginManager().registerEvents(new Apteczka(), this);
        Bukkit.getPluginManager().registerEvents(new GrenadierWeaponListener(), this);
        PixaCommand pixaCommand = new PixaCommand(this);
        Bukkit.getPluginManager().registerEvents(new WeaponManager(this), this);
        getCommand("pixa").setExecutor(pixaCommand);
        WeaponManager weaponManager = new WeaponManager(this);
        Bukkit.getPluginManager().registerEvents(weaponManager, this);
        getCommand("giveweapons").setExecutor(new GiveWeaponsCommand(weaponManager));
        Bukkit.getPluginManager().registerEvents(new AmmoDropListener(weaponManager), this);
        getCommand("giveammo").setExecutor(new GiveAmmoCommand(weaponManager));
        getServer().getPluginManager().registerEvents(new KillStreakListener(this), this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        Bukkit.getPluginManager().registerEvents(new WidmoListener(), this);
    }
    public static BF3 getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Przywitanie gracza
        Bukkit.getServer().broadcast(
                Component.text("[")
                        .append(Component.text("+").color(NamedTextColor.GREEN))
                        .append(Component.text("]").color(NamedTextColor.GRAY))
                        .append(Component.text(" " + event.getPlayer().getName()).color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC))
        );
    }

    public ItemStack stworzCustomItem(Material material, String name, Integer amount, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name)); // Nowe API dla nazwy
            List<Component> loreComponents = Arrays.stream(lore)
                    .map(Component::text)
                    .collect(Collectors.toList());
            meta.lore(loreComponents); // Nowe API dla lore
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getServer().broadcast(
                Component.text("[")
                        .append(Component.text("-").color(NamedTextColor.RED))
                        .append(Component.text("]").color(NamedTextColor.GRAY))
                        .append(Component.text(" " + event.getPlayer().getName())
                                .color(NamedTextColor.RED)
                                .decorate(TextDecoration.ITALIC))
        );
    }

    @EventHandler
    public void onPlayerJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (!player.getGameMode().equals(GameMode.CREATIVE) && player.getAllowFlight()) {
            event.setCancelled(true); // Wyłącz domyślny lot Minecrafta
            player.setAllowFlight(false); // Po skoku gracz nie może latać
            player.setFlying(false);

            int jumps = playerJumps.getOrDefault(player.getUniqueId(), 0);

            if (jumps < MAX_JUMPS - 1) { // Jeśli gracz może wykonać podwójny skok
                player.setVelocity(player.getVelocity().setY(1.0)); // Podwójny skok
                playerJumps.put(player.getUniqueId(), jumps + 1); // Zwiększ licznik skoków
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0F, 1.0F); // Efekt dźwiękowy
                player.sendMessage(Component.text("Podwójny skok!").color(NamedTextColor.GOLD));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.isOnGround()) {
            // Zresetowanie liczby skoków, jeśli gracz dotknie ziemi
            playerJumps.put(player.getUniqueId(), 0);
            player.setAllowFlight(true); // Pozwól na aktywację podwójnego skoku
        }
    }

    @EventHandler
    public void onPlayerJumpAttempt(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isOnGround()) {
            player.setAllowFlight(true); // Aktywujemy podwójny skok po zwykłym skoku
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity(); // Ofiara
        player.sendMessage("§7Zginąłeś! Wypada tylko amunicja.");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Random rand = new Random();

        // Losowe współrzędne w obrębie zdefiniowanego prostokątnego obszaru
        int x = rand.nextInt(-29 - (-174) + 1) + (-174);  // Z zakresu X: -174 do -29
        int y = 94;  // Stała wysokość
        int z = rand.nextInt(209 - 50 + 1) + 50;  // Z zakresu Z: 50 do 209

        // Ustalamy nowe miejsce respawnu
        Player player = event.getPlayer();
        Location spawnLocation = new Location(player.getWorld(), x, y, z);
        event.setRespawnLocation(spawnLocation);
    }
}
