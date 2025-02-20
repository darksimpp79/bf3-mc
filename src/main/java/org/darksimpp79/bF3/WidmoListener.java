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
    public static void summonGhostDog(Player player) {
        Location spawnLoc = player.getLocation().add(Math.random() - 0.5, 0, Math.random() - 0.5);
        Wolf dog = (Wolf) player.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
        dog.setCustomName("§5Pies Widma");
        dog.setCustomNameVisible(true);
        dog.setAngry(true);
        // Oznaczamy psa metadanymi, aby wiedzieć, że należy do klasy Widmo
        dog.setMetadata("widmoDog", new FixedMetadataValue(plugin, true));
        // Przechowujemy UUID właściciela, żeby pies nie atakował swojego gracza
        dog.setMetadata("owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        // Ustawiamy zwiększoną prędkość (speed 2)
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
        }.runTaskLater(plugin, 3 * 20L);
    }

    // Obsługa ręcznego przywoływania psa za pomocą Totemu Psa
    @EventHandler
    public void onTotemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (meta.getDisplayName().equals("§6 Totem Psa")) {
            long now = System.currentTimeMillis();
            long lastUsed = totemCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (now - lastUsed < 60000) { // 1 minuta
                player.sendMessage(Component.text("Totem Psa jest na cooldownie!").color(NamedTextColor.RED));
                return;
            }
            totemCooldown.put(player.getUniqueId(), now);
            summonGhostDog(player);
            player.sendMessage(Component.text("Przywołano psa Widma!").color(NamedTextColor.GREEN));
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
            // Przykładowy efekt – grantujemy graczowi wysoki poziom odporności przez 5 sekund
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 4, false, false));
            player.sendMessage(Component.text("Aktywowałeś Mroczny Cień! Jesteś teraz niemal nietykalny.").color(NamedTextColor.GREEN));
            event.setCancelled(true); // zapobiegamy fizycznemu zużyciu przedmiotu
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
            if (victim.getUniqueId().toString().equals(ownerUUID)) return;
            victim.setGlowing(true);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 1));
            victim.sendMessage(Component.text("Zostałeś ugryziony przez psa Widma!").color(NamedTextColor.DARK_PURPLE));
            new BukkitRunnable() {
                @Override
                public void run() {
                    victim.setGlowing(false);
                }
            }.runTaskLater(plugin, 15 * 20L);
            victim.setHealth(0);
        }
    }
}
