package com.anticheat.core.checks.packet;

import com.anticheat.core.AntiCheatPlugin;
import com.anticheat.core.data.PlayerData;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * NoSwing kontrolu (SADECE ProtocolLib ile mumkun): normal bir vurusta
 * istemci once "kol sallama" (ARM_ANIMATION) paketi, ardindan vurus
 * gonderir. Bu paket olmadan gerceklesen vurus, bazi Killaura/reach
 * hilelerinin animasyon adimini atladigini gosterebilir.
 *
 * Basitlestirme notu: Ilk vurustan once henuz hic swing paketi
 * gorulmediyse (lastSwingTime == 0) yanlis pozitifi onlemek icin
 * bayraklama yapilmaz. Bu yuzden HICBIR ZAMAN swing paketi gondermeyen
 * (surekli sifir) bir istemci bu kontrolle yakalanamaz; bu, event
 * seviyesindeki bir sinirlamadir.
 */
public class NoSwingCheck implements Listener {

    private final AntiCheatPlugin plugin;

    public NoSwingCheck(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;
                PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
                data.setLastSwingTime(System.currentTimeMillis());
            }
        });
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("checks.noswing.enabled", true)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.hasPermission("anticheat.bypass") || plugin.getViolationManager().isExempt(attacker.getUniqueId())) return;

        PlayerData data = plugin.getPlayerDataManager().get(attacker.getUniqueId());
        long windowMs = plugin.getConfig().getLong("checks.noswing.swing-window-ms", 250);
        long now = System.currentTimeMillis();

        if (data.getLastSwingTime() != 0 && (now - data.getLastSwingTime()) > windowMs) {
            int kickVl = plugin.getConfig().getInt("checks.noswing.kick-vl", 5);
            plugin.getViolationManager().flag(attacker, "NoSwing", "Kol sallama paketi olmadan vurus", kickVl);
        }
    }
}
