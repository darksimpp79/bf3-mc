package org.darksimpp79.bF3;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PixaCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public PixaCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        startPixaScheduler(); // Uruchamiamy automatyczne efekty
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tylko gracze mogą używać tej komendy!");
            return true;
        }

        Player player = (Player) sender;
        applyRandomEffect(player);
        return true;
    }

    private void applyRandomEffect(Player player) {
        int effectType = random.nextInt(6);

        switch (effectType) {
            case 0 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));
                player.sendMessage(ChatColor.GRAY + "👻 Stałeś się niewidzialny na 10 sekund!");
            }
            case 1 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
                player.sendMessage(ChatColor.AQUA + "🌀 Unosisz się w powietrzu!");
            }
            case 2 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                player.sendMessage(ChatColor.DARK_GREEN + "☠ Zjadłeś trującą pixę!");
            }
            case 3 -> {
                Location loc = player.getLocation();
                loc.add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
                player.teleport(loc);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🌍 Pixą teleportowała cię w dziwne miejsce!");
            }
            case 4 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GOLD + "🔮 Usłyszałeś tajemniczy dźwięk... to na pewno przez pixę!");
            }
            case 5 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.sendMessage(ChatColor.BLUE + "⚡ Pixą dała ci super prędkość!");
            }
        }
    }

    private void startPixaScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.GOLD + "⚡ Pixa aktywowana! Każdy dostaje losowy efekt!");
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyRandomEffect(player);
            }
        }, 0L, 6000L); // 6000 ticków = 5 minut
    }
}
