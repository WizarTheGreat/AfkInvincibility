package plugin.afkinvincibility;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public final class AfkInvincibility extends JavaPlugin implements Listener {
    private HashMap<UUID, Long> lastActivityTime = new HashMap<>();
    private HashMap<UUID, Boolean> invulnerablePlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startInactivityCheckTask();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getNearbyEntities(0.1, 0.1, 0.1).isEmpty()) {
            updateActivity(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        updateActivity(event.getPlayer());
    }

    private void updateActivity(Player player) {
        UUID playerId = player.getUniqueId();
        lastActivityTime.put(playerId, System.currentTimeMillis());
        invulnerablePlayers.put(playerId, false);
    }

    private void startInactivityCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    long lastActivity = lastActivityTime.getOrDefault(playerId, currentTime);
                    if (currentTime - lastActivity > 30000) { // 30 seconds
                        invulnerablePlayers.put(playerId, true);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // Run every second
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPlayerInvulnerable(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPlayerInvulnerable(player)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isPlayerInvulnerable(Player player) {
        UUID playerId = player.getUniqueId();
        return invulnerablePlayers.getOrDefault(playerId, false);
    }
}