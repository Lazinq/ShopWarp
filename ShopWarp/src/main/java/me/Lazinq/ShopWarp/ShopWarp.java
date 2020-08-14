package me.Lazinq.ShopWarp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class ShopWarp extends JavaPlugin implements CommandExecutor {

    private final HashMap<UUID, Location> shopWarp = new HashMap<>();

    public void onEnable() {
        this.saveDefaultConfig();
        loadData();
    }
    public void onDisable(){
        saveData();
    }

    private void saveData() {
        for (UUID uuid : shopWarp.keySet()) {
            getConfig().set("PlayerData." + uuid.toString(), locToString(shopWarp.get(uuid)));
        }
        saveConfig();
    }

    private void loadData() {
        if (getConfig().getConfigurationSection("PlayerData") != null) {
            for (String shops : getConfig().getConfigurationSection("PlayerData").getKeys(false)) {
                UUID uuid = UUID.fromString(shops);
                Location loc = stringToLoc(getConfig().getConfigurationSection("PlayerData").getString(shops));
                shopWarp.put(uuid, loc);
            }
        }
        getConfig().set("PlayerData", null);
        saveConfig();
    }

    public String locToString(Location loc) {
        return loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
    public Location stringToLoc(final String input) {
        final String[] args = input.split(", ");
        return new Location(Bukkit.getWorld(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (label.equalsIgnoreCase("SetShop") || label.equalsIgnoreCase("setloja")) {
            if (player.hasPermission(getConfig().getString("permission.setshop"))) {
                shopWarp.put(uuid, player.getLocation());
                for (String msg : getConfig().getStringList("message.setshop")) {
                    player.sendMessage(applyCC(msg));
                }
                getConfig().set("Data." + Bukkit.getPlayer(uuid), locToString(shopWarp.get(uuid)));
                saveConfig();
            }
        }
        if (label.equalsIgnoreCase("RemoveShop") || label.equalsIgnoreCase("removeloja")) {
            if (player.hasPermission(getConfig().getString("permission.removeshop"))) {
                if (args.length > 0) {
                    Player target = Bukkit.getServer().getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage("Incorrect input, try /RemoveShop (Player).");
                        return true;
                    }
                    shopWarp.remove(target.getUniqueId());
                    for (String msg : getConfig().getStringList("message.removeshop")) {
                        player.sendMessage(applyCC(msg));
                    }
                }
            }
        }
        if (label.equalsIgnoreCase("Shop") || label.equalsIgnoreCase("loja")) {
            if (player.hasPermission(getConfig().getString("permission.warpshop"))) {
                if (args.length == 0) {
                    if (shopWarp.get(player.getUniqueId()) == null){
                        for (String msg : getConfig().getStringList("message.invalidshop")) {
                            player.sendMessage(applyCC(msg));
                        }
                        return true;
                    }
                    player.teleport(shopWarp.get(player.getUniqueId()));
                    for (String msg : getConfig().getStringList("message.warpshop")) {
                        msg = msg.replace("$Player", sender.getName());
                        player.sendMessage(applyCC(msg));
                        return true;
                    }
                }
            if (args.length > 0) {
                Player target = Bukkit.getServer().getPlayer(args[0]);
                if (target == null) return true;
                if (shopWarp.get(target.getUniqueId()) == null) {
                    for (String msg : getConfig().getStringList("message.invalidshop")) {
                        player.sendMessage(applyCC(msg));
                    }
                    return true;
                }
                player.teleport(shopWarp.get(target.getUniqueId()));
                for (String msg : getConfig().getStringList("message.warpshop")) {
                    msg = msg.replace("$Player", target.getName());
                    player.sendMessage(applyCC(msg));
                    return true;
                    }
                }
            }
        }
        if (label.equalsIgnoreCase("ShopReload") || label.equalsIgnoreCase("LojaReload")) {
            if (player.hasPermission(getConfig().getString("permission.reloadshop"))) {
                for (String msg : this.getConfig().getStringList("message.reload")) {
                    sender.sendMessage(applyCC(msg));
                }
            this.reloadConfig();
                return true;
            }
        }
        if (label.equalsIgnoreCase("Shops") || label.equalsIgnoreCase("Lojas")) {
            if (!shopWarp.isEmpty()) {
            for (String msg : getConfig().getStringList("message.shops")) {
                msg = msg.replace("$Players", shopWarp.keySet().stream().map(Bukkit::getPlayer).map(Player::getName).collect(Collectors.joining(",")));
                player.sendMessage(applyCC(msg));
                return true;
                }
            }
        }
        if (label.equalsIgnoreCase("ShopHelp") || label.equalsIgnoreCase("LojaHelp")) {
            for (String msg : getConfig().getStringList("message.shophelp")) {
                msg = msg.replace("$Player", sender.getName());
                player.sendMessage(applyCC(msg));
                return true;
            }
        }
        return false;
    }

    public String applyCC(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

}
