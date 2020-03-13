// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.object.CellBorder;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.DrawSmokeTaskFactory;
import com.palmergames.bukkit.towny.utils.BorderUtil;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import com.palmergames.bukkit.towny.ChunkNotification;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.command.TownyCommand;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyCustomListener implements Listener
{
    private final Towny plugin;
    
    public TownyCustomListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangePlotEvent(final PlayerChangePlotEvent event) {
        final Player player = event.getPlayer();
        final WorldCoord from = event.getFrom();
        final WorldCoord to = event.getTo();
        if (this.plugin.hasPlayerMode(player, "townclaim")) {
            TownCommand.parseTownClaimCommand(player, new String[0]);
        }
        if (this.plugin.hasPlayerMode(player, "townunclaim")) {
            TownCommand.parseTownUnclaimCommand(player, new String[0]);
        }
        if (this.plugin.hasPlayerMode(player, "map")) {
            TownyCommand.showMap(player);
        }
        try {
            if (to.getTownyWorld().isUsingTowny() && TownySettings.getShowTownNotifications()) {
                final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
                final ChunkNotification chunkNotifier = new ChunkNotification(from, to);
                final String msg = chunkNotifier.getNotificationString(resident);
                if (msg != null) {
                    if (Towny.isSpigot && TownySettings.isNotificationsAppearingInActionBar()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
                    }
                    else {
                        player.sendMessage(msg);
                    }
                }
            }
        }
        catch (NotRegisteredException ex) {}
        if (this.plugin.hasPlayerMode(player, "plotborder")) {
            final CellBorder cellBorder = BorderUtil.getPlotBorder(to);
            cellBorder.runBorderedOnSurface(1, 2, DrawSmokeTaskFactory.sendToPlayer(player));
        }
    }
}
