package com.anticheat.core.checks.packet;

import com.anticheat.core.AntiCheatPlugin;
import com.anticheat.core.data.PlayerData;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Timer hilesi kontrolu (SADECE ProtocolLib ile mumkun): istemcinin
 * gercek zamanda saniyede gonderdigi hareket paketi sayisini olcer.
 * Sunucu 20 TPS bekler; bunun belirgin uzerinde paket gonderen
 * istemciler, oyun dongusunu hizlandiran Timer hilesi kullaniyor
 * olabilir. Skript ile bu tespit yapilamaz (ham paket erisimi gerekir).
 */
public class TimerCheck {

    private final AntiCheatPlugin plugin;

    public TimerCheck(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;
                if (player.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(player.getUniqueId())) return;

                PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
                data.incrementPacketCount();
            }
        });

        Bukkit.getScheduler().runTaskTimer(plugin, this::evaluateAll, 20L, 20L);
    }

    private void evaluateAll() {
        if (!plugin.getConfig().getBoolean("checks.timer.enabled", true)) return;
        int maxPackets = plugin.getConfig().getInt("checks.timer.max-packets-per-second", 24);
        int kickVl = plugin.getConfig().getInt("checks.timer.kick-vl", 6);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(player.getUniqueId())) continue;
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
            int packets = data.getAndResetPacketCount();
            if (packets > maxPackets) {
                plugin.getViolationManager().flag(player, "Timer", packets + " paket/saniye (beklenen: ~20)", kickVl);
            }
        }
    }
}
