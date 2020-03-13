// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.TownyMessaging;
import org.bukkit.Color;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.Listener;

public class TownyLoginListener implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(final PlayerLoginEvent event) throws NotRegisteredException {
        final String npcPrefix = TownySettings.getNPCPrefix();
        final String warChest = "towny-war-chest";
        final String serverAccount = TownySettings.getString(ConfigNodes.ECO_CLOSED_ECONOMY_SERVER_ACCOUNT);
        boolean disallowed = false;
        final Player player = event.getPlayer();
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (player.getName().startsWith(npcPrefix)) {
            if (townyUniverse.getDataSource().hasResident(player.getName()) && townyUniverse.getDataSource().getResident(player.getName()).isMayor()) {
                event.disallow((PlayerLoginEvent.Result)null, "Towny is preventing you from logging in using this account name.");
                disallowed = true;
            }
        }
        else if (player.getName().equals(warChest) || player.getName().equals(warChest.replace("-", "_"))) {
            event.disallow((PlayerLoginEvent.Result)null, "Towny is preventing you from logging in using this account name.");
            disallowed = true;
        }
        else if (player.getName().equals(serverAccount) || player.getName().equals(serverAccount.replace("-", "_"))) {
            event.disallow((PlayerLoginEvent.Result)null, "Towny is preventing you from logging in using this account name.");
            disallowed = true;
        }
        else if (player.getName().startsWith(TownySettings.getTownAccountPrefix()) || player.getName().startsWith(TownySettings.getTownAccountPrefix().replace("-", "_")) || player.getName().startsWith(TownySettings.getNationAccountPrefix()) || player.getName().startsWith(TownySettings.getNationAccountPrefix().replace("-", "_"))) {
            event.disallow((PlayerLoginEvent.Result)null, "Towny is preventing you from logging in using this account name.");
            disallowed = true;
        }
        if (disallowed) {
            String ip = event.getAddress().toString();
            ip = ip.substring(1);
            final String msg = "A player using the IP address " + Color.RED + ip + Color.GREEN + " tried to log in using am accountname which could damage your server's economy, but was prevented by Towny. Consider banning this IP address!";
            TownyMessaging.sendMsg(msg);
            for (final Player ops : Bukkit.getOnlinePlayers()) {
                if (ops.isOp() || ops.hasPermission("towny.admin")) {
                    TownyMessaging.sendMsg(ops, msg);
                }
            }
        }
    }
}
