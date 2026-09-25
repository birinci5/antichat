package com.anticheat.core.command;

import com.anticheat.core.AntiCheatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * /ac (yani /anticheat) komutu: calisma zamaninda (restart gerekmeden)
 * kontrolleri acip kapatma, VL goruntuleme, muafiyet ve ayar degistirme.
 */
public class AntiCheatCommand implements CommandExecutor, TabCompleter {

    // config.yml yollariyla birebir eslesen kucuk harfli anahtarlar
    private static final List<String> CHECK_KEYS = Arrays.asList(
            "reach", "speed", "fly", "nofall", "killaura", "autoclicker", "timer", "noswing");

    // ViolationManager.flag() cagrilarinda kullanilan tam (case-sensitive) isimler
    private static final List<String> CHECK_DISPLAY = Arrays.asList(
            "Reach", "Speed", "Fly", "NoFall", "Killaura", "AutoClicker", "Timer", "NoSwing");

    private static final List<String> SUBCOMMANDS = Arrays.asList("toggle", "vl", "exempt", "reset", "reload", "config");

    private final AntiCheatPlugin plugin;

    public AntiCheatCommand(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanma yetkiniz yok.")));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> handleToggle(sender, args);
            case "vl" -> handleVl(sender, args);
            case "exempt" -> handleExempt(sender, args);
            case "reset" -> handleReset(sender, args);
            case "reload" -> handleReload(sender);
            case "config" -> handleConfig(sender, args);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&7Kullanim: /ac toggle <" + String.join("/", CHECK_KEYS) + ">"));
            return;
        }
        String check = args[1].toLowerCase();
        String path = "checks." + check + ".enabled";
        if (!plugin.getConfig().isSet(path)) {
            sender.sendMessage(color("&cGecersiz kontrol adi: " + check));
            return;
        }
        boolean newValue = !plugin.getConfig().getBoolean(path, true);
        plugin.getConfig().set(path, newValue);
        plugin.saveConfig();
        sender.sendMessage(color((newValue ? "&a" : "&c") + check + " &7kontrolu " + (newValue ? "acildi." : "kapatildi.")));
    }

    private void handleVl(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&7Kullanim: /ac vl <oyuncu>"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        sender.sendMessage(color("&7" + args[1] + " &7icin VL durumu:"));
        StringBuilder sb = new StringBuilder(" &7");
        for (int i = 0; i < CHECK_DISPLAY.size(); i++) {
            String display = CHECK_DISPLAY.get(i);
            int vl = plugin.getViolationManager().getVL(uuid, display);
            sb.append(display).append(": &e").append(vl).append("&7");
            if (i < CHECK_DISPLAY.size() - 1) sb.append(" | ");
        }
        sender.sendMessage(color(sb.toString()));
    }

    private void handleExempt(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&7Kullanim: /ac exempt <oyuncu>"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean nowExempt = plugin.getViolationManager().toggleExempt(target.getUniqueId());
        sender.sendMessage(color((nowExempt ? "&c" : "&a") + args[1] + " &7" + (nowExempt ? "korumadan muaf tutuldu." : "artik korumaya tabi.")));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&7Kullanim: /ac reset <oyuncu>"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        plugin.getViolationManager().resetAll(target.getUniqueId());
        sender.sendMessage(color("&a" + args[1] + " &7oyuncusunun tum VL'leri sifirlandi."));
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(color("&aconfig.yml yeniden yuklendi."));
    }

    private void handleConfig(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(color("&7Kullanim: /ac config <kontrol> <ayar> <deger>  (ornek: /ac config reach max-distance 4.5)"));
            return;
        }
        String check = args[1].toLowerCase();
        String key = args[2].toLowerCase();
        String value = args[3];
        String path = "checks." + check + "." + key;

        if (!plugin.getConfig().isSet(path)) {
            sender.sendMessage(color("&cGecersiz ayar yolu: " + path));
            return;
        }

        try {
            double numeric = Double.parseDouble(value);
            plugin.getConfig().set(path, numeric);
            plugin.saveConfig();
            sender.sendMessage(color("&a" + path + " &7= &e" + numeric));
        } catch (NumberFormatException ex) {
            sender.sendMessage(color("&cGecersiz sayisal deger: " + value));
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(color("&7Komutlar: &f/ac toggle <kontrol> &7| &f/ac vl <oyuncu> &7| &f/ac exempt <oyuncu> &7| &f/ac reset <oyuncu> &7| &f/ac reload &7| &f/ac config <kontrol> <ayar> <deger>"));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("config"))) {
            for (String s : CHECK_KEYS) {
                if (s.startsWith(args[1].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(p.getName());
            }
        }
        return completions;
    }
}
