package com.anticheat.core;

import com.anticheat.core.checks.combat.AutoClickerCheck;
import com.anticheat.core.checks.combat.KillauraCheck;
import com.anticheat.core.checks.combat.ReachCheck;
import com.anticheat.core.checks.movement.MovementListener;
import com.anticheat.core.checks.packet.NoSwingCheck;
import com.anticheat.core.checks.packet.TimerCheck;
import com.anticheat.core.command.AntiCheatCommand;
import com.anticheat.core.data.PlayerDataManager;
import com.anticheat.core.listener.ConnectionListener;
import com.anticheat.core.manager.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiCheatPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private ViolationManager violationManager;
    private boolean protocolLibEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerDataManager = new PlayerDataManager();
        this.violationManager = new ViolationManager(this);

        // --- Bukkit/Paper API kontrolleri (ProtocolLib gerektirmez) ---
        getServer().getPluginManager().registerEvents(new ConnectionListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new ReachCheck(this), this);
        getServer().getPluginManager().registerEvents(new KillauraCheck(this), this);
        getServer().getPluginManager().registerEvents(new AutoClickerCheck(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);

        AntiCheatCommand commandExecutor = new AntiCheatCommand(this);
        getCommand("anticheat").setExecutor(commandExecutor);
        getCommand("anticheat").setTabCompleter(commandExecutor);

        // --- ProtocolLib gerektiren paket-seviyesi kontroller ---
        this.protocolLibEnabled = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
        if (protocolLibEnabled) {
            new TimerCheck(this).register();
            new NoSwingCheck(this).register();
            getLogger().info("ProtocolLib bulundu: Timer ve NoSwing kontrolleri aktif.");
        } else {
            getLogger().warning("ProtocolLib bulunamadi! Timer ve NoSwing kontrolleri devre disi kaldi.");
            getLogger().warning("Bu kontroller icin ProtocolLib'i plugins klasorune ekleyip sunucuyu yeniden baslatin.");
        }

        violationManager.startDecayTask();

        getLogger().info("AntiCheat basariyla yuklendi. (v" + getDescription().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        if (violationManager != null) {
            violationManager.stopDecayTask();
        }
        getLogger().info("AntiCheat devre disi birakildi.");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }
}
