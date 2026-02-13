package zero.nyc.gravitationsector;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class GravitationSector extends JavaPlugin implements Listener {

    public RegionContainer container;

    @Override
    public void onEnable() {
        getCommand("spacehelmet").setExecutor(new SpaceHelmet());
        getServer().getPluginManager().registerEvents(this, this);
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public void onDisable() {
    }

    public static List<String> playersInSafeZone = new ArrayList<>(); //UUID Gracza
    public static List<String> playersInSpace = new ArrayList<>(); //UUID Gracza
    public ProtectedRegion safezone;
    public ProtectedRegion space;

    public void PlayerZone(Player player) {
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regions != null) {
            safezone = regions.getRegion("safe-zone");
            space = regions.getRegion("zone");

            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            String uuid = player.getUniqueId().toString();

            if (safezone.contains(x,y,z)) {
                playersInSafeZone.add(uuid);
            } else if (space.contains(x,y,z)) {
                playersInSpace.add(uuid);
            }
            else {
                if (playersInSpace.contains(uuid)) {
                    playersInSpace.remove(uuid);
                } else if (playersInSafeZone.contains(uuid)) {
                    playersInSafeZone.remove(uuid);
                }
            }
        }
    }

    public void addEffect(Player player, boolean isSafe) {
        //TODO space_helmet
        if (isSafe) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 2));
            player.setRemainingAir(player.getMaximumAir());
        } else if (!isSafe) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1, 3));
            if (!player.getInventory().getHelmet().getItemMeta().getPersistentDataContainer().getKeys().contains(SpaceHelmet.createHelmet().getItemMeta().getPersistentDataContainer().getKeys())) {
                new BukkitRunnable() {
                    public void run() {
                        if (playersInSafeZone.contains(player.getUniqueId().toString())) {
                            cancel();
                            return;
                        }
                        int air = player.getRemainingAir();
                        player.setRemainingAir(air - 30);
                    }
                }.runTaskTimer(this, 0, 20L);
            } else {
                player.setRemainingAir(player.getMaximumAir());
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 2, 2));
            }
        }
    }
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
        Player p = playerMoveEvent.getPlayer();
        String uuid = p.getUniqueId().toString();
        PlayerZone(p);
        if (playersInSafeZone.contains(uuid)) {
            addEffect(p, true);
        } else if (playersInSpace.contains(uuid)) {
            addEffect(p, false);
        }
    }
}
