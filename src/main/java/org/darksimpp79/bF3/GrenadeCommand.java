package org.darksimpp79.bF3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GrenadeCommand implements CommandExecutor {
    private final BF3 plugin;

    public GrenadeCommand(BF3 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Komenda dostępna tylko dla graczy
        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda może być użyta tylko przez gracza.");
            return true;
        }
        Player player = (Player) sender;

        // Tworzymy granaty przy użyciu metody stworzCustomItem
        ItemStack explosiveGrenade = plugin.stworzCustomItem(Material.TNT, "§c Granat Wybuchowy", 1, "§a Eksploduje przy kontakcie (nie niszczy bloków)");
        ItemStack smokeGrenade = plugin.stworzCustomItem(Material.TNT, "§e Granat Dymny", 1, "§a Tworzy gęstą chmurę dymu");
        ItemStack incendiaryGrenade = plugin.stworzCustomItem(Material.TNT, "§a Granat Palący", 1, "§a Ustawia cel w ogniu, pali otoczenie (bez zniszczeń)");
        ItemStack toxicGrenade = plugin.stworzCustomItem(Material.TNT, "§2 Granat Trujący", 1, "§a Nakłada efekt zatrucia");
        ItemStack nightVisionGrenade = plugin.stworzCustomItem(Material.TNT, "§5 Granat Noktowizyjny", 1, "§a Nadaje efekt świecenia");

        // Dodajemy granaty do ekwipunku gracza
        player.getInventory().addItem(explosiveGrenade, smokeGrenade, incendiaryGrenade, toxicGrenade, nightVisionGrenade);
        player.sendMessage(Component.text("Otrzymałeś wszystkie granaty!").color(NamedTextColor.GREEN));
        return true;
    }
}
