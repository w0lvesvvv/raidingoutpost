package me.wolves.raidingoutpost.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;

import net.citizensnpcs.api.CitizensAPI;

public class SchematicUtil {

	public static void pasteBaseInterior(String name, int X, int Y, int Z) {
		File file = new File("plugins/WorldEdit/schematics/" + name + ".schematic");
		Vector point = new Vector(X, Y, Z);
		Schematic schem = null;
		try {
			schem = FaweAPI.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BukkitWorld bukkitWorld = new BukkitWorld(Bukkit.getWorld("routpost"));
		if (schem != null) {
			schem.paste((World) bukkitWorld, point, false, true, null);
		}
	}

	public static void pasteBaseBuffer(String name, int X, int Y, int Z) {
		File file1 = new File("plugins/WorldEdit/schematics/" + name + ".schematic");
		Vector point1 = new Vector(X, Y, Z);
		Schematic schem = null;
		try {
			schem = FaweAPI.load(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BukkitWorld bukkitWorld = new BukkitWorld(Bukkit.getWorld(("routpost")));
		if (schem != null) {
			schem.paste((World) bukkitWorld, point1, false, true, null);
		}
	}

	public static void teleportAllPlayersOut(Cuboid cuboid, Location location) {
		if (cuboid == null) {
			Bukkit.getServer().getLogger().log(Level.WARNING,
					"[RaidingOutpost] Cube does not exist! Please set this cube.");
			return;
		}
		for (Player player : Bukkit.getServer().getWorld("routpost").getPlayers()) {
			if (!CitizensAPI.getNPCRegistry().isNPC(player)) {
				player.teleport(location);
			}
		}
	}

	public static void teleportAllPlayerOutWorld(Location location) {
		for (Player player : Bukkit.getServer().getWorld("routpost").getPlayers()) {
			if (!CitizensAPI.getNPCRegistry().isNPC(player)) {
				player.sendMessage(Utils.chat("&b[Routpost] &fWorld is been reloaded."));
				player.teleport(location);
			}
		}
	}

}
