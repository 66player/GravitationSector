package zero.nyc.gravitationsector;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class GravitationSector extends JavaPlugin implements Listener {

    private static GravitationSector instance;
    private RegionContainer container;

    private final Set<UUID> playersInSafeZone = new HashSet<>();
    private final Set<UUID> playersInSpace = new HashSet<>();
    private final Map<UUID, Long> airCooldown = new HashMap<>();

    private ProtectedRegion safezone;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("spacehelmet").setExecutor(new SpaceHelmet());
        getServer().getPluginManager().registerEvents(this, this);
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
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

    private void applyEffects(Player player, boolean isSafe) {
        UUID uuid = player.getUniqueId();

        if (isSafe) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, 20, 1, true, false
            ));
            player.setRemainingAir(player.getMaximumAir());
            airCooldown.remove(uuid);
            return;
        }

        // SPACE
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

            long now = System.currentTimeMillis();
            long last = airCooldown.getOrDefault(uuid, 0L);

            if (now - last >= 1000) {
                player.setRemainingAir(Math.max(0, air - 30));
                airCooldown.put(uuid, now);
            }
            return;
        }

        player.setRemainingAir(player.getMaximumAir());
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.WATER_BREATHING, 40, 0, true, false
        ));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        updatePlayerZone(player);

        if (playersInSafeZone.contains(uuid)) {
            applyEffects(player, true);
        } else {
            applyEffects(player, false);
        }
    }
}