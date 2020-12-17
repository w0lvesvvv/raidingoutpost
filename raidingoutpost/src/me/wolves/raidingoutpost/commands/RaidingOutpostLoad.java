package me.wolves.raidingoutpost.commands;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;

import me.wolves.raidingoutpost.RaidingOutpostPlugin;
import me.wolves.raidingoutpost.entity.RaidingOutpost;
import me.wolves.raidingoutpost.util.SchematicUtil;
import me.wolves.raidingoutpost.util.Utils;
import me.wolves.raidingoutpost.util.Worlds;

public class RaidingOutpostLoad implements CommandExecutor {

	private RaidingOutpostPlugin plugin;
	private RaidingOutpost routpost;

	// REGION: MAIN METHODS
	public RaidingOutpostLoad(RaidingOutpostPlugin plugin, RaidingOutpost routpost) {
		this.plugin = plugin;
		this.routpost = routpost;

		plugin.getCommand("routpost").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length == 0) {

			commandRoutpost(sender);

		} else if (args.length > 0) {

			if (args[0].equals("load")) {

				commandLoad(sender);

			} else if (args[0].equals("setup")) {

				commandSetUp(sender);

			} else if (args[0].equals("claim")) {

				commandClaim(sender);

			} else if (args[0].equals("setHome")) {

				commandSetHome(sender);

			} else if (args[0].equals("reload")) {

				commandReload(sender);

			} else if (args[0].equals("owner") && args.length == 1) {

				commandOwner(sender);

			} else if (args[0].equals("owner") && args[1].equals("restart")) {

				commandOwnerRestart(sender);

			} else if (args[0].equals("setHologram")) {

				commandSetHologram(sender);

			} else if (args[0].equals("saveBackup")) {

				commandSaveBackup();

			} else if (args[0].equals("restartWorld")) {

				commandRestartWorld();

			} else {

				commandHelp(sender);

			}
		} else {

			commandHelp(sender);

		}
		return true;
	}
	// ENDREGION: MAIN METHODS

	// REGION: COMMANDS
	public void commandSetUp(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		player.sendMessage(Utils.chat("&b[Routpost] &fYou can find all the steps to set up the plugin on the config."));
	}

	public void commandReload(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		plugin.reloadConfig();
		player.sendMessage(Utils.chat("&b[Routpost] &fConfig has been reloaded."));
	}

	public void commandOwner(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		player.sendMessage(Utils.chat("&b[Routpost] &fOwner: " + routpost.getOutpostOwnerTag()));
	}

	public void commandOwnerRestart(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		Faction factionRoutpost = FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag"));
		Faction factionOwner = FactionColl.get().get(routpost.getOutpostOwnerID());

		// Quito las relaciones
		factionOwner.setRelationWish(factionRoutpost, Rel.NEUTRAL);
		factionRoutpost.setRelationWish(factionOwner, Rel.NEUTRAL);

		routpost.setOutpostOwnerId(FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag")).getId());
		routpost.setCappedAtMillis(0);
		routpost.cappedAtMillis = 0;
		routpost.nextPrizeMillis = 0;
		player.sendMessage(Utils.chat("&b[Routpost] &fOwner has been removed."));
	}

	public void commandHelp(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		player.sendMessage(Utils.chat("&b[Raiding Outpost] &fCommands list:"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost setup &7- info about how to set up the plugin"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost reload &7- reload the config"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost owner &7- check who is the routpost owner"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost owner restart &7- restart the outpost owner"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost load &7- paste the schematics"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost claim &7- claim the routpost base"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost setHologram &7- set the hologram with the timer"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost setHome &7- set the home for /f home Routpost"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost saveBackup &7- save a world back up"));
		player.sendMessage(Utils.chat("&7[&bRO&7]: &b/routpost restartWorld &7- load the world back up"));
	}

	@SuppressWarnings("deprecation")
	public void commandRoutpost(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		String routpostOwner = routpost.getOutpostOwnerTag();
		Date unclaimDate = Utils.getDate(routpost.getMillisUntilUnclaim());

		if (routpostOwner.equals(plugin.getConfig().getString("routpost.tag")) || routpostOwner.equals("None")) {
			player.sendMessage(Utils.chat("&b[Routpost] &fRoutpost hasn't been captured. "
					+ "Routpost world will be unclaimed in " + unclaimDate.getHours() + ":" + unclaimDate.getMinutes()
					+ ":" + unclaimDate.getSeconds()));
		}

	}

	public void commandLoad(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		if (plugin.getConfig().getString("schematics.buffer1.name").length() > 0
				&& plugin.getConfig().getString("schematics.base.name").length() > 0) {
			SchematicUtil.pasteBaseBuffer(plugin.getConfig().getString("schematics.buffer1.name"),
					Integer.parseInt(plugin.getConfig().getString("schematics.buffer1.X")),
					Integer.parseInt(plugin.getConfig().getString("schematics.buffer1.Y")),
					Integer.parseInt(plugin.getConfig().getString("schematics.buffer1.Z")));

			if (plugin.getConfig().getString("schematics.buffer2.name").length() > 0) {
				SchematicUtil.pasteBaseBuffer(plugin.getConfig().getString("schematics.buffer2.name"),
						Integer.parseInt(plugin.getConfig().getString("schematics.buffer2.X")),
						Integer.parseInt(plugin.getConfig().getString("schematics.buffer2.Y")),
						Integer.parseInt(plugin.getConfig().getString("schematics.buffer2.Z")));
			}

			SchematicUtil.pasteBaseInterior(plugin.getConfig().getString("schematics.base.name"),
					Integer.parseInt(plugin.getConfig().getString("schematics.base.X")),
					Integer.parseInt(plugin.getConfig().getString("schematics.base.Y")),
					Integer.parseInt(plugin.getConfig().getString("schematics.base.Z")));
			player.sendMessage(Utils.chat(plugin.getConfig().getString("routpost.loadMessage")));
		} else {
			player.sendMessage(Utils.chat("&cThe config file is empty. Write the fields and use &b/routpost reload"));
		}
	}

	public void commandClaim(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		if (plugin.getConfig().getString("buffer.maxX").length() > 0) {
			claimRoutpost();
			player.sendMessage(Utils.chat("&b[Routpost] &fBuffer has been claimed!"));
		} else {
			player.sendMessage(Utils.chat("&cThe config file is empty. Write the fields and use &b/routpost reload"));
		}
	}

	public void commandSetHologram(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (plugin.getConfig().getString("routpost.chestCoords.X").length() > 0) {
			Location chestLocation = new Location(Bukkit.getWorld(plugin.getConfig().getString("routpost.worldName")),
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.X")),
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Y")),
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Z")));
			routpost.chestLocation = chestLocation;
			chestLocation.getBlock().setType(Material.CHEST);
			player.sendMessage(Utils.chat("&b[Raiding Outpost] &bHologram has been placed."));

			Location hologramLocation = new Location(
					Bukkit.getWorld(plugin.getConfig().getString("routpost.worldName")),
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.X")) + 0.5,
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Y")) - 0.5,
					Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Z")) + 0.5);
			ArmorStand hologram = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation,
					EntityType.ARMOR_STAND);

			hologram.setGravity(false);
			hologram.setCustomName(Utils.chat("&bPrize will be available in XX:XX"));
			hologram.setCustomNameVisible(true);
			hologram.setVisible(false);
		} else {
			player.sendMessage(Utils.chat("&cThe config file is empty. Write the fields and use &b/routpost reload"));
		}
	}

	public void commandSetHome(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		Faction routpostFaction = FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag"));
		routpostFaction.setHome(PS.valueOf(player.getLocation()));
		sender.sendMessage(Utils.chat("&b[Routpost] &fHome has been placed."));
	}

	public void commandSaveBackup() {

		SchematicUtil.teleportAllPlayerOutWorld(
				new Location(Bukkit.getWorld(plugin.getConfig().getString("teleportSpawn.worldName")),
						Double.parseDouble(plugin.getConfig().getString("teleportSpawn.X")),
						Double.parseDouble(plugin.getConfig().getString("teleportSpawn.Y")),
						Double.parseDouble(plugin.getConfig().getString("teleportSpawn.Z"))));

		if (new File("routpostBackup").exists()) {
			Worlds.delete(new File("routpostBackup"));
		}
		try {
			Worlds.copyDir(new File("routpost"), new File("routpostBackup"));
			Worlds.delete(new File("routpostBackup/uid.dat"));
			Bukkit.broadcastMessage(Utils.chat("&b[Routpost] &fWorld saved in a BackUp. &cRestart the server"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void commandRestartWorld() {
		unclaimRoutpostWorld();
		Worlds.restartRoutpost();
		claimRoutpost();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Location hologramLocation = new Location(
						Bukkit.getWorld(plugin.getConfig().getString("routpost.worldName")),
						Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.X")) + 0.5,
						Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Y")) - 0.5,
						Integer.parseInt(plugin.getConfig().getString("routpost.chestCoords.Z")) + 0.5);
				ArmorStand hologram = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation,
						EntityType.ARMOR_STAND);

				hologram.setGravity(false);
				hologram.setCustomName(Utils.chat("&bPrize will be available in XX:XX"));
				hologram.setCustomNameVisible(true);
				hologram.setVisible(false);
			}
		}, 200L);
	}
	// ENDREGION: COMMANDS

	// REGION: PRIVATE METHODS
	private Set<PS> getChunksInCuboid() {
		int maxX = Integer.parseInt(plugin.getConfig().getString("buffer.maxX"));
		int minX = Integer.parseInt(plugin.getConfig().getString("buffer.minX"));
		int maxZ = Integer.parseInt(plugin.getConfig().getString("buffer.maxZ"));
		int minZ = Integer.parseInt(plugin.getConfig().getString("buffer.minZ"));

		Set<PS> chunksInCuboid = new HashSet<>();
		for (int x = minX; x <= maxX; x += 16) {
			for (int z = minZ; z <= maxZ; z += 16) {
				Location loc = new Location(plugin.getServer().getWorld("routpost"), x, 120.0D, z);
				chunksInCuboid.add(PS.valueOf(loc));
			}
		}
		return chunksInCuboid;
	}

	private void claimRoutpost() {
		Set<PS> chunksInCuboid = getChunksInCuboid();
		// Faction FactionRoutpost =
		// Factions.getInstance().getByTag(plugin.getConfig().getString("routpost.tag"));
		Faction FactionRoutpost = FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag"));
		for (PS chunkPs : chunksInCuboid) {
			if (!BoardColl.get().getFactionAt(chunkPs).getId().equals(FactionRoutpost.getId())) {
				BoardColl.get().setFactionAt(chunkPs, FactionRoutpost,
						Bukkit.getPlayer(plugin.getConfig().getString("owner-ign")));
			}
		}
	}

	private Set<PS> getChunksInWorld() {
		int maxX = Integer.parseInt(plugin.getConfig().getString("world.maxX"));
		int minX = Integer.parseInt(plugin.getConfig().getString("world.minX"));
		int maxZ = Integer.parseInt(plugin.getConfig().getString("world.maxZ"));
		int minZ = Integer.parseInt(plugin.getConfig().getString("world.minZ"));

		Set<PS> chunksInCuboid = new HashSet<>();
		for (int x = minX; x <= maxX; x += 16) {
			for (int z = minZ; z <= maxZ; z += 16) {
				Location loc = new Location(plugin.getServer().getWorld("routpost"), x, 120.0D, z);
				chunksInCuboid.add(PS.valueOf(loc));
			}
		}
		return chunksInCuboid;
	}

	private void unclaimRoutpostWorld() {
		Set<PS> chunksInCuboid = getChunksInWorld();
		Faction FactionRoutpost = FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag"));
		for (PS chunkPs : chunksInCuboid) {
			if (!BoardColl.get().getFactionAt(chunkPs).getId().equals(FactionRoutpost.getId())) {
				BoardColl.get().removeAt(chunkPs);
			}
		}
	}
	// ENDREGION: PRIVATE METHODS
}
