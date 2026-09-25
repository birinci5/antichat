package com.anticheat.core.checks.combat;

import com.anticheat.core.AntiCheatPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Reach kontrolu: saldiran ile hedef arasindaki mesafeyi olcer.
 * Ping'e gore kucuk bir tolerans eklenir (yuksek pingli oyuncularda
 * istemci konumu sunucudan hafifce ileride olabilir).
 * Limit asilinca vurus IPTAL EDILIR (sadece uyarmakla kalinmaz).
 */
public class ReachCheck implements Listener {

    private final AntiCheatPlugin plugin;

    public ReachCheck(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("checks.reach.enabled", true)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        if (attacker.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(attacker.getUniqueId())) return;
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;

        double distance = attacker.getEyeLocation().distance(victim.getLocation());
        double tolerance = plugin.getConfig().getDouble("checks.reach.max-distance", 4.2);
        double pingDivisor = plugin.getConfig().getDouble("checks.reach.ping-tolerance-divisor", 3000.0);

        int ping = attacker.getPing();
        if (ping > 0) {
            tolerance += ping / pingDivisor;
        }

        if (distance > tolerance) {
            event.setCancelled(true);
            int kickVl = plugin.getConfig().getInt("checks.reach.kick-vl", 6);
            String info = String.format("%.2f blok (limit: %.2f)", distance, tolerance);
            plugin.getViolationManager().flag(attacker, "Reach", info, kickVl);
        }
    }
}
