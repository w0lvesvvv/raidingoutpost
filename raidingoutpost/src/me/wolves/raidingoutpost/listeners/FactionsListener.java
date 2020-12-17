package me.wolves.raidingoutpost.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.massivecore.ps.PS;

import me.wolves.raidingoutpost.RaidingOutpostPlugin;
import me.wolves.raidingoutpost.entity.RaidingOutpost;
import me.wolves.raidingoutpost.util.Utils;

public class FactionsListener implements Listener{

	private RaidingOutpostPlugin routpostPlugin;
	private RaidingOutpost routpost;

	public FactionsListener(RaidingOutpostPlugin plugin, RaidingOutpost routpost) {
		this.routpostPlugin = plugin;
		this.routpost = routpost;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onFactionsDisband(EventFactionsDisband event) {
		if (event.getFaction().getId().equals(routpost.getOutpostOwnerID())) {
			routpost.setOutpostOwnerId(FactionColl.get().getByName(routpostPlugin.getConfig().getString("routpost.tag")).getId());
			Bukkit.broadcastMessage(Utils.chat(routpostPlugin.getConfig().getString("routpost.disbandMessage")));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		
		MPlayer fPlayer = MPlayer.get(event.getPlayer());
		if (!fPlayer.hasFaction())
			return;
		if (routpost.getOutpostOwnerID() == null)
			return;
		if (fPlayer.getFaction().getId() == (routpost.getOutpostOwnerID())) {
			Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(event.getBlock().getLocation()));
			if (factionAt.getId().equals(routpost.getOutpostOwnerID()))
				event.setCancelled(false);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

			MPlayer fPlayer = MPlayer.get(event.getPlayer());
			if (fPlayer == null || fPlayer.getFaction() == null || !fPlayer.hasFaction())
				return;
			if (routpost.getOutpostOwnerID() == null)
				return;
			if (fPlayer.getFaction().getId().equals(routpost.getOutpostOwnerID())) {
				if (event.getClickedBlock() == null)
					return;
				if (fPlayer.getFactionId().equals(routpost.getOutpostOwnerID()))
					event.setCancelled(false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		MPlayer fPlayer = MPlayer.get(event.getPlayer());
		if (!fPlayer.hasFaction())
			return;
		if (routpost.getOutpostOwnerID() == null)
			return;
		if (fPlayer.getFaction().getId().equals(routpost.getOutpostOwnerID())) {
			Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(event.getBlock().getLocation()));
			if (factionAt.getId().equals(routpost.getOutpostOwnerID()))
				if (event.getBlock().getType() == Material.TNT) {
					event.setCancelled(true);
				} else {
					event.setCancelled(false);
				}
		}
	}
}
