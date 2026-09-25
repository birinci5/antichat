package com.anticheat.core.manager;

import com.anticheat.core.AntiCheatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ihlal Seviyesi (VL) sistemini yonetir: puanlama, uyari yayini,
 * otomatik kick ve zamanla azalma (decay).
 */
public class ViolationManager {

    private final AntiCheatPlugin plugin;
    private final Map<UUID, Map<String, Integer>> violations = new ConcurrentHashMap<>();
    private final Set<UUID> exempt = ConcurrentHashMap.newKeySet();
    private BukkitTask decayTask;

    public ViolationManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Bir hile suphesini bildirir: VL'yi artirir, yetkililere haber verir,
     * konsola loglar ve VL esigi asildiysa oyuncuyu atar.
     */
    public void flag(Player player, String check, String info, int kickVl) {
        if (isExempt(player.getUniqueId()) || player.hasPermission("anticheat.bypass")) {
            return;
        }

        Map<String, Integer> playerViolations = violations.computeIfAbsent(player.getUniqueId(), u -> new ConcurrentHashMap<>());
        int vl = playerViolations.merge(check, 1, Integer::sum);

        String prefix = color(plugin.getConfig().getString("prefix", "&8[&cAntiCheat&8]&7"));
        String alertTemplate = plugin.getConfig().getString("messages.alert",
                "%prefix% &e%player% &7oyuncusu &b%check% &7suphesi! &7(VL: &c%vl%&7/%maxvl%) &8- %info%");

        String alert = color(alertTemplate
                .replace("%prefix%", prefix)
                .replace("%player%", player.getName())
                .replace("%check%", check)
                .replace("%vl%", String.valueOf(vl))
                .replace("%maxvl%", String.valueOf(kickVl))
                .replace("%info%", info));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("anticheat.admin")) {
                online.sendMessage(alert);
            }
        }
        plugin.getLogger().info(ChatColor.stripColor(alert));

        if (vl >= kickVl) {
            String kickReason = plugin.getConfig().getString("messages.kick-reason", "Hile Suphesi / Baglantinizi Kontrol Edin");
            String kickBroadcastTemplate = plugin.getConfig().getString("messages.kick-broadcast",
                    "%prefix% &c%player% &7oyuncusu &c%check%&7 VL limitini asarak &4ATILDI&7.");
            String kickBroadcast = color(kickBroadcastTemplate
                    .replace("%prefix%", prefix)
                    .replace("%player%", player.getName())
                    .replace("%check%", check));

            // Kick islemi ana thread'de calismali (ozellikle ProtocolLib
            // paket listener'lari async calisabildigi icin onemli).
            Bukkit.getScheduler().runTask(plugin, () -> {
                //noinspection deprecation - Spigot/Paper API'nin her ikisinde de
                // bulunan, geriye donuk uyumlu String tabanli kick metodu.
                player.kickPlayer(kickReason);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("anticheat.admin")) {
                        online.sendMessage(kickBroadcast);
                    }
                }
                plugin.getLogger().warning(player.getName() + " - " + check + " VL limitini asti, otomatik kick uygulandi.");
            });

            playerViolations.put(check, 0);
        }
    }

    public int getVL(UUID uuid, String check) {
        return violations.getOrDefault(uuid, Map.of()).getOrDefault(check, 0);
    }

    public void resetAll(UUID uuid) {
        violations.remove(uuid);
    }

    public boolean isExempt(UUID uuid) {
        return exempt.contains(uuid);
    }

    /** @return islem sonrasi muafiyet durumu (true = artik muaf) */
    public boolean toggleExempt(UUID uuid) {
        if (exempt.contains(uuid)) {
            exempt.remove(uuid);
            return false;
        } else {
            exempt.add(uuid);
            return true;
        }
    }

    public void clearPlayer(UUID uuid) {
        violations.remove(uuid);
    }

    public void startDecayTask() {
        long interval = Math.max(1, plugin.getConfig().getLong("violation-decay-seconds", 20)) * 20L;
        decayTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map<String, Integer> playerMap : violations.values()) {
                for (String check : playerMap.keySet()) {
                    playerMap.computeIfPresent(check, (k, v) -> v > 0 ? v - 1 : 0);
                }
            }
        }, interval, interval);
    }

    public void stopDecayTask() {
        if (decayTask != null) {
            decayTask.cancel();
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
