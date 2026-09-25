package com.anticheat.core.checks.combat;

import com.anticheat.core.AntiCheatPlugin;
import com.anticheat.core.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * CPS (saniyedeki tiklama) sayacina dayali AutoClicker kontrolu.
 * Her saniye biriken vurus sayisi degerlendirilip sifirlanir.
 */
public class AutoClickerCheck implements Listener {

    private final AntiCheatPlugin plugin;

    public AutoClickerCheck(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::evaluateAll, 20L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        plugin.getPlayerDataManager().get(attacker.getUniqueId()).incrementClickCount();
    }

    private void evaluateAll() {
        if (!plugin.getConfig().getBoolean("checks.autoclicker.enabled", true)) return;
        int maxCps = plugin.getConfig().getInt("checks.autoclicker.max-cps", 21);
        int kickVl = plugin.getConfig().getInt("checks.autoclicker.kick-vl", 5);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(player.getUniqueId())) continue;
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
            int cps = data.getAndResetClickCount();
            if (cps > maxCps) {
                plugin.getViolationManager().flag(player, "AutoClicker", cps + " CPS", kickVl);
            }
        }
    }
}
