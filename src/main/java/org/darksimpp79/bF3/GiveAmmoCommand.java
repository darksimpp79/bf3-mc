package org.darksimpp79.bF3;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiveAmmoCommand implements CommandExecutor {

    private final WeaponManager weaponManager;

    public GiveAmmoCommand(WeaponManager weaponManager) {
        this.weaponManager = weaponManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda dostępna tylko dla graczy!");
            return true;
        }
        Player player = (Player) sender;
        List<Weapon> weapons = weaponManager.getWeapons();
        // Dla każdej zarejestrowanej broni dajemy graczowi 64 sztuki amunicji
        for (Weapon weapon : weapons) {
            ItemStack ammoStack = weapon.getAmmoItem(64);
            player.getInventory().addItem(ammoStack);
        }
        player.sendMessage(ChatColor.GREEN + "Otrzymałeś pełne staki amunicji do każdej broni!");
        return true;
    }
}
