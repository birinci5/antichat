package com.anticheat.core.checks.movement;

import com.anticheat.core.AntiCheatPlugin;
import com.anticheat.core.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Speed, Fly ve NoFall kontrollerini tek bir PlayerMoveEvent uzerinden
 * yurutur (mesafe/Y hesaplarini tekrarlamamak icin performans amacli
 * birlestirilmistir). "movement-tick-throttle" ile agir hesaplamalar
 * her tick yerine N tick'te bir calisir.
 */
public class MovementListener implements Listener {

    private final AntiCheatPlugin plugin;

    public MovementListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;

        if (player.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(player.getUniqueId())) return;

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            data.setLastLocation(null);
            data.setAirTicks(0);
            data.setFallDistance(0);
            return;
        }

        // Elytra ve araclar (at, kayik vb.) kendi fizigine sahiptir, muaf tutulur.
        if (player.isGliding() || player.isInsideVehicle()) {
            data.setLastLocation(player.getLocation());
            return;
        }

        int throttle = Math.max(1, plugin.getConfig().getInt("movement-tick-throttle", 3));
        if (data.incrementAndGetMoveTickCounter() < throttle) {
            return;
        }
        data.setMoveTickCounter(0);

        checkSpeed(player, data, throttle);
        checkFlyAndNoFall(player, data);
    }

    private void checkSpeed(Player player, PlayerData data, int throttle) {
        if (data.getLastLocation() == null) {
            data.setLastLocation(player.getLocation());
            return;
        }

        Location last = data.getLastLocation();
        Location current = player.getLocation();

        if (last.getWorld() == null || current.getWorld() == null || !last.getWorld().equals(current.getWorld())) {
            data.setLastLocation(current);
            return;
        }

        double distance = last.distance(current);
        double speed = distance / throttle;

        if (plugin.getConfig().getBoolean("checks.speed.enabled", true)) {
            double tolerance = plugin.getConfig().getDouble("checks.speed.max-blocks-per-tick", 0.65);

            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                tolerance *= plugin.getConfig().getDouble("checks.speed.speed-potion-multiplier", 1.5);
            }

            Block below = current.clone().subtract(0, 1, 0).getBlock();
            Material belowType = below.getType();
            if (belowType == Material.ICE || belowType == Material.PACKED_ICE || belowType == Material.BLUE_ICE) {
                tolerance *= plugin.getConfig().getDouble("checks.speed.ice-multiplier", 1.6);
            }
            if (current.getBlock().getType() == Material.WATER) {
                tolerance *= plugin.getConfig().getDouble("checks.speed.water-multiplier", 1.4);
            }

            if (speed > tolerance) {
                player.teleport(last);
                int kickVl = plugin.getConfig().getInt("checks.speed.kick-vl", 8);
                String info = String.format("%.2f blok/tick (limit: %.2f)", speed, tolerance);
                plugin.getViolationManager().flag(player, "Speed", info, kickVl);
            }
        }

        data.setLastLocation(current);
    }

    private void checkFlyAndNoFall(Player player, PlayerData data) {
        Location loc = player.getLocation();
        Block below = loc.clone().subtract(0, 1, 0).getBlock();
        double currentY = loc.getY();

        if (below.getType() == Material.AIR) {
            double deltaY = data.getLastY() - currentY;

            // Gercekten dusuyorsa (yer cekimi) mesafeyi biriktir; sabit/artan
            // yukseklikte "havada asili kalmak" (hover) suphelidir.
            if (deltaY > 0) {
                data.addFallDistance(deltaY);
            } else if (deltaY < -0.08) {
                data.setFallDistance(0);
            }

            if (deltaY <= 0.02) {
                data.incrementAirTicks();
            } else {
                data.setAirTicks(0);
            }

            if (plugin.getConfig().getBoolean("checks.fly.enabled", true)) {
                int maxAirTicks = plugin.getConfig().getInt("checks.fly.max-air-ticks", 60);
                if (data.getAirTicks() > maxAirTicks) {
                    if (data.getLastLocation() != null) {
                        player.teleport(data.getLastLocation());
                    }
                    int kickVl = plugin.getConfig().getInt("checks.fly.kick-vl", 6);
                    plugin.getViolationManager().flag(player, "Fly", data.getAirTicks() + " tick havada asili kaldi", kickVl);
                    data.setAirTicks(0);
                }
            }
        } else {
            // Yere inis ani -> NoFall kontrolu
            if (plugin.getConfig().getBoolean("checks.nofall.enabled", true)) {
                double minFall = plugin.getConfig().getDouble("checks.nofall.min-fall-distance", 4.5);
                if (data.getFallDistance() >= minFall
                        && !player.hasPotionEffect(PotionEffectType.SLOW_FALLING)
                        && below.getType() != Material.WATER
                        && below.getType() != Material.HAY_BLOCK
                        && below.getType() != Material.SLIME_BLOCK
                        && below.getType() != Material.HONEY_BLOCK) {

                    double fallDistanceSnapshot = data.getFallDistance();
                    double healthBefore = player.getHealth();

                    // Dusme hasari genelde ayni tick'te uygulanir; birkac tick
                    // bekleyip hasar gercekten geldi mi diye kontrol ediyoruz.
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (!player.isOnline()) return;
                        if (player.getHealth() >= healthBefore) {
                            int kickVl = plugin.getConfig().getInt("checks.nofall.kick-vl", 5);
                            String info = String.format("%.1f blok dustu, hasar almadi", fallDistanceSnapshot);
                            plugin.getViolationManager().flag(player, "NoFall", info, kickVl);
                        }
                    }, 3L);
                }
            }
            data.setAirTicks(0);
            data.setFallDistance(0);
        }

        data.setLastY(currentY);
    }
}
