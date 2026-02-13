package zero.nyc.gravitationsector;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class GravitationSector extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    private WorldGuardPlugin worldGuard() {
        Plugin pl = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(pl instanceof WorldGuardPlugin))
        {
            return null;
        }
        return (WorldGuardPlugin) pl;
    }

    public boolean PlayerInRegion(Player player) {
        ProtectedRegion region = worldGuard().
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        if (region.contains(x, y, z)) {
            return true;
        } else {
            return false;
        }
    }

}
