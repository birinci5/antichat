package com.anticheat.core.checks.combat;

import com.anticheat.core.AntiCheatPlugin;
import com.anticheat.core.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Basitlestirilmis Killaura kontrolu: bir oyuncunun cok kisa surede
 * (varsayilan <150ms) farkli hedeflere vurmasi, insan reaksiyon
 * suresinin cok altinda oldugu icin supheli kabul edilir.
 * Not: Gercek Killaura'lar acy/rotasyon manipulasyonu da yapar; bu,
 * event seviyesinde ulasilabilecek pratik bir yaklasimdir.
 */
public class KillauraCheck implements Listener {

    private final AntiCheatPlugin plugin;

    public KillauraCheck(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("checks.killaura.enabled", true)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (attacker.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(attacker.getUniqueId())) return;

        PlayerData data = plugin.getPlayerDataManager().get(attacker.getUniqueId());
        long now = System.currentTimeMillis();
        int minSwitchMs = plugin.getConfig().getInt("checks.killaura.min-switch-ms", 150);

        if (data.getLastTarget() != null && !data.getLastTarget().equals(victim.getUniqueId())) {
            long diff = now - data.getLastHitTime();
            if (diff < minSwitchMs) {
                int kickVl = plugin.getConfig().getInt("checks.killaura.kick-vl", 4);
                plugin.getViolationManager().flag(attacker, "Killaura", "Hedef degisimi: " + diff + "ms", kickVl);
            }
        }

        data.setLastTarget(victim.getUniqueId());
        data.setLastHitTime(now);
    }
}
