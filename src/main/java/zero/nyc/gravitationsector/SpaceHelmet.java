package zero.nyc.gravitationsector;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpaceHelmet implements CommandExecutor {

    public static ItemStack createHelmet() {
        ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
        ItemMeta meta = helmet.getItemMeta();

        meta.setDisplayName("§bSpace Helmet");

        Plugin plugin = GravitationSector.getPlugin(GravitationSector.class);

        NamespacedKey key = new NamespacedKey(plugin, "space_helmet");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        helmet.setItemMeta(meta);
        return helmet;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] label) {
        if (sender instanceof Player p) {
            if (p.isOp()) {
                p.getInventory().addItem(createHelmet());
            } else p.sendMessage(Color.RED + "Only admin can use that !");
        } else sender.sendMessage(Color.RED + "Only player can use that !");
        return false;
    }
}
