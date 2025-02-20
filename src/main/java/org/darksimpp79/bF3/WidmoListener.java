package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WidmoListener implements Listener {

    private static final Plugin plugin = BF3.getInstance();

    // Gracze z klasą Widmo
    private static final Set<UUID> widmoPlayers = new HashSet<>();
    // Ostatni czas automatycznego przywołania psa (co 30 sekund)
    private static final HashMap<UUID, Long> lastDogSummon = new HashMap<>();
    // Cooldown dla Totemu Psa (manualne przywołanie, 1 minuta = 60000 ms)
    private static final HashMap<UUID, Long> totemCooldown = new HashMap<>();
    // Cooldown dla Mrocznego Cienia (1 minuta = 60000 ms)
    private static final HashMap<UUID, Long> mrocznyCienCooldown = new HashMap<>();

    public WidmoListener() {

    }

    public static void addWidmo(Player player) {
        widmoPlayers.add(player.getUniqueId());
    }

    public static void removeWidmo(Player player) {
        widmoPlayers.remove(player.getUniqueId());
        lastDogSummon.remove(player.getUniqueId());
        totemCooldown.remove(player.getUniqueId());
        mrocznyCienCooldown.remove(player.getUniqueId());
    }

    // Metoda przywołująca psa – teraz pies zostaje na 30 sekund, dzięki czemu możesz go fizycznie zobaczyć
    public static void summonGhostDog(Player owner) {
        // Ustawiamy lokalizację psa w pobliżu gracza (właściciela)
        Location spawnLoc = owner.getLocation().add(Math.random() - 0.5, 0, Math.random() - 0.5);
        Wolf dog = (Wolf) owner.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);

        // Ustawienia psa – nie ustawiamy właściciela, aby pies pozostał dziki i agresywny
        dog.setCustomName("§5Pies Widma");
        dog.setCustomNameVisible(true);
        dog.setAngry(true);      // Pies jest agresywny
        dog.setSitting(false);   // Nie siedzi – swobodnie biega

        // Oznaczamy psa metadanymi: zapamiętujemy, kto jest "właścicielem" (używamy tylko do sprawdzenia przy ugryzieniu)
        dog.setMetadata("widmoDog", new FixedMetadataValue(plugin, true));
        dog.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));

        // Ustawiamy zwiększoną prędkość psa (speed 2)
        if (dog.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            dog.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(2.0);
        }

        // Pies zostaje usunięty po 30 sekundach
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dog.isDead()) {
                    dog.remove();
                }
            }
        }.runTaskLater(plugin, 30 * 20L); // 30 sekund

        // Zadanie cykliczne: wyszukiwanie najbliższego wroga (gracza, który nie jest właścicielem) i ustawienie go jako celu
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dog == null || dog.isDead() || !owner.isOnline()) {
                    cancel();
                    return;
                }
                double radius = 10.0;
                Player target = null;
                double closestDistance = Double.MAX_VALUE;
                for (Player p : dog.getWorld().getPlayers()) {
                    // Pomijamy właściciela
                    if (p.getUniqueId().equals(owner.getUniqueId())) continue;
                    double distance = p.getLocation().distance(dog.getLocation());
                    if (distance < radius && distance < closestDistance) {
                        closestDistance = distance;
                        target = p;
                    }
                }
                if (target != null) {
                    dog.setTarget(target);
                } else {
                    // Jeśli nie ma wrogów w zasięgu – można wyczyścić cel lub pozostawić poprzedni
                    // dog.setTarget(null);
                }
            }
        }.runTaskTimer(plugin, 0, 20L); // co 1 sekundę
    }


    // Obsługa ręcznego przywoływania psa za pomocą Totemu Psa
    @EventHandler
    public void onTotemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Sprawdzamy, czy przedmiot w ręce nie jest pusty i czy ma odpowiednią nazwę
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Sprawdzamy, czy to jest "Totem Psa"
        if (meta.getDisplayName().equals("§6 Totem Psa")) {
            long now = System.currentTimeMillis();
            long lastUsed = totemCooldown.getOrDefault(player.getUniqueId(), 0L);

            // Sprawdzamy, czy nie jest jeszcze na cooldownie
            if (now - lastUsed < 60000) { // 1 minuta
                player.sendMessage(Component.text("Totem Psa jest na cooldownie!").color(NamedTextColor.RED));
                event.setCancelled(true);  // Anulujemy dalsze przetwarzanie zdarzenia
                return;
            }

            // Ustawiamy nowy czas użycia totemu
            totemCooldown.put(player.getUniqueId(), now);

            // Przywołujemy psa
            summonGhostDog(player);

            // Informujemy gracza
            player.sendMessage(Component.text("Przywołano psa Widma!").color(NamedTextColor.GREEN));

            // Anulowanie zdarzenia, aby nie wykonywać innych działań związanych z przedmiotem
            event.setCancelled(true);
        }
    }

    // Obsługa aktywacji Mrocznego Cienia – po użyciu daje efekt "nietykalności" (np. wysoka odporność)
    @EventHandler
    public void onMrocznyCienUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (meta.getDisplayName().equals("§5 Mroczny Cień")) {
            long now = System.currentTimeMillis();
            long lastUsed = mrocznyCienCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (now - lastUsed < 60000) { // 1 minuta cooldown
                player.sendMessage(Component.text("Mroczny Cień jest na cooldownie!").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
            mrocznyCienCooldown.put(player.getUniqueId(), now);

            // Dodajemy efekt niewidzialności dla gracza
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 4, false, false));

            // Zapisujemy cały ekwipunek gracza
            ItemStack[] originalInventory = player.getInventory().getContents();
            ItemStack[] originalArmor = player.getInventory().getArmorContents();

            // Usuwamy cały ekwipunek (ustawiamy na AIR)
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);

            // Po 10 sekundach (200 ticków) przywracamy ekwipunek
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Przywracamy ekwipunek i zbroję
                player.getInventory().setContents(originalInventory);
                player.getInventory().setArmorContents(originalArmor);
            }, 200L);

            player.sendMessage(Component.text("Aktywowałeś Mroczny Cień! Jesteś teraz niemal nietykalny.").color(NamedTextColor.GREEN));
            event.setCancelled(true);
        }
    }


    // Obsługa trafienia specjalną Kosą Widma
    @EventHandler
    public void onKosaWidmaHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!meta.getDisplayName().equals("§c Kosa Widma")) return;
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 5 * 20, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 1));
            victim.sendMessage(Component.text("Zostałeś trafiony przez Kosę Widma!").color(NamedTextColor.DARK_PURPLE));
        }
    }

    // Obsługa ugryzienia przez psa Widma
    @EventHandler
    public void onGhostDogBite(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Wolf)) return;
        Wolf dog = (Wolf) event.getDamager();
        if (!dog.hasMetadata("widmoDog")) return;
        List<?> metaValues = dog.getMetadata("owner");
        if (metaValues.isEmpty()) return;
        String ownerUUID = metaValues.get(0).toString();

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            // Nie atakujemy właściciela
            if (victim.getUniqueId().toString().equals(ownerUUID)) return;

            // Nadajemy efekty: spowolnienie oraz nudności (nausea)
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 1)); // 15 sekund, poziom 1
            victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 15 * 20, 1));     // 15 sekund, poziom 1

            // Dodatkowo ustawiamy efekt świecenia (glowing) na 15 sekund, aby ofiara była bardziej widoczna
            victim.setGlowing(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    victim.setGlowing(false);
                }
            }.runTaskLater(plugin, 15 * 20L);

            victim.sendMessage(Component.text("Zostałeś ugryziony przez psa Widma!").color(NamedTextColor.DARK_PURPLE));
        }
    }
}
