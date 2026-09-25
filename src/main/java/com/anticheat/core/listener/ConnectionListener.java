package com.anticheat.core.listener;

import com.anticheat.core.data.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Oyuncu giris/cikislarinda PlayerData olusturur/temizler.
 * Bellek sizintisini onlemek icin cikiste mutlaka silinmelidir.
 */
public class ConnectionListener implements Listener {

    private final PlayerDataManager dataManager;

    public ConnectionListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dataManager.get(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dataManager.remove(event.getPlayer().getUniqueId());
    }
}
