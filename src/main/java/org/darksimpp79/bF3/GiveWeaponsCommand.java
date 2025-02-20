package org.darksimpp79.bF3;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GiveWeaponsCommand implements CommandExecutor {

    private final WeaponManager weaponManager;

    public GiveWeaponsCommand(WeaponManager weaponManager) {
        this.weaponManager = weaponManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda tylko dla graczy!");
            return true;
        }
        Player player = (Player) sender;
        List<Weapon> weapons = weaponManager.getWeapons();
        for (Weapon weapon : weapons) {
            // Tworzymy przedmiot reprezentujący broń
            ItemStack item = new ItemStack(weapon.getItemMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(weapon.getDisplayName());
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
            // Opcjonalnie: ustaw pełny magazyn dla tej broni
            // (jeśli dostępna jest publiczna metoda – tutaj zakładamy, że broń zaczyna działać z pełnym magazynem)
        }
        player.sendMessage(ChatColor.GREEN + "Otrzymałeś wszystkie bronie!");
        return true;
    }
}
