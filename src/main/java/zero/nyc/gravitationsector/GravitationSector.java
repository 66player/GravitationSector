package zero.nyc.gravitationsector;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GravitationSector extends JavaPlugin implements Listener {

    private static GravitationSector instance;
    private RegionContainer container;

    private final Set<UUID> playersInSafeZone = new HashSet<>();
    private final Set<UUID> playersInSpace = new HashSet<>();

    private ProtectedRegion safezone;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("spacehelmet").setExecutor(new SpaceHelmet());
        getServer().getPluginManager().registerEvents(this, this);
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        startGravityTask();
    }

    public static GravitationSector getInstance() {
        return instance;
    }

    private boolean hasSpaceHelmet(Player player) {
        if (player.getInventory().getHelmet() == null) return false;
        if (!player.getInventory().getHelmet().hasItemMeta()) return false;

        ItemMeta meta = player.getInventory().getHelmet().getItemMeta();
        NamespacedKey key = new NamespacedKey(this, "space_helmet");

        return meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    private void updatePlayerZone(Player player) {
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regions == null) return;

        safezone = regions.getRegion("safe-zone");
        if (safezone == null) return;

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        UUID uuid = player.getUniqueId();

        if (safezone.contains(x, y, z)) {
            playersInSafeZone.add(uuid);
            playersInSpace.remove(uuid);
        } else {
            // wszystko poza safe = space
            playersInSpace.add(uuid);
            playersInSafeZone.remove(uuid);
        }
    }

    // Task co tick – wszystkie efekty i powietrze
    public void startGravityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    // sprawdzamy strefę co tick
                    updatePlayerZone(player);
                    boolean isSafe = playersInSafeZone.contains(uuid);

                    // SAFE
                    if (isSafe) {
                        player.setRemainingAir(player.getMaximumAir());
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.REGENERATION, 20, 1, true, false
                        ));
                    }
                    // SPACE
                    else {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOW_FALLING, 20, 1, true, false
                        ));
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.JUMP, 20, 2, true, false
                        ));

                        if (!hasSpaceHelmet(player)) {
                            int maxAir = player.getMaximumAir();
                            int air = player.getRemainingAir();

                            if (air >= maxAir) {
                                player.setRemainingAir(maxAir - 1);
                            }

                            player.setRemainingAir(Math.max(0, player.getRemainingAir() - 1));
                        } else {
                            player.setRemainingAir(player.getMaximumAir());
                            player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.WATER_BREATHING, 40, 0, true, false
                            ));
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L); // co tick
    }
}