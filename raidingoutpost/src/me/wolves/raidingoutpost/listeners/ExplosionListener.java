package me.wolves.raidingoutpost.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;

import me.wolves.raidingoutpost.RaidingOutpostPlugin;
import me.wolves.raidingoutpost.entity.RaidingOutpost;
import me.wolves.raidingoutpost.util.Cuboid;
import me.wolves.raidingoutpost.util.SchematicUtil;
import me.wolves.raidingoutpost.util.Utils;

public class ExplosionListener implements Listener {

	private RaidingOutpostPlugin routpostPlugin;
	private RaidingOutpost routpost;
	private Cuboid cubo;
	// BASE COORDS
	private int maxX;
	private int minX;
	private int maxY;
	private int minY;
	private int maxZ;
	private int minZ;
	// BUFFER COORDS
	private int bufferMaxX;
	private int bufferMinX;
	private int bufferMaxZ;
	private int bufferMinZ;

	public ExplosionListener(RaidingOutpostPlugin plugin, RaidingOutpost routpost) {
		this.routpostPlugin = plugin;
		this.routpost = routpost;

		if (plugin.getConfig().getString("base.maxX").length() > 0
				&& plugin.getConfig().getString("buffer.maxX").length() > 0) {
			maxX = Integer.parseInt(plugin.getConfig().getString("base.maxX"));
			minX = Integer.parseInt(plugin.getConfig().getString("base.minX"));
			maxY = Integer.parseInt(plugin.getConfig().getString("base.maxY"));
			minY = Integer.parseInt(plugin.getConfig().getString("base.minY"));
			maxZ = Integer.parseInt(plugin.getConfig().getString("base.maxZ"));
			minZ = Integer.parseInt(plugin.getConfig().getString("base.minZ"));

			bufferMaxX = Integer.parseInt(plugin.getConfig().getString("buffer.maxX"));
			bufferMinX = Integer.parseInt(plugin.getConfig().getString("buffer.minX"));
			bufferMaxZ = Integer.parseInt(plugin.getConfig().getString("buffer.maxZ"));
			bufferMinZ = Integer.parseInt(plugin.getConfig().getString("buffer.minZ"));

			cubo = new Cuboid(minX, maxX, minY, maxY, minZ, maxZ,
					Bukkit.getServer().getWorld(routpostPlugin.getConfig().getString("routpost.worldName")));

			Bukkit.getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {

		if (!(event.getEntity() instanceof TNTPrimed))
			return;

		Location sourceLoc = ((TNTPrimed) event.getEntity()).getSourceLoc();
		if (sourceLoc == null)
			return;

		Faction factionBreaching = BoardColl.get().getFactionAt(PS.valueOf(sourceLoc));
		Faction factionBreached = FactionColl.get().get(routpost.getOutpostOwnerID());

		if (!checkFactionIsClaimed(factionBreaching))
			return;

		if (!checkExplosionAtCube(event.getLocation()))
			return;

		if (factionBreaching.getId() != routpost.getOutpostOwnerID()) {
			if (!routpost.recentlyCapped) {

				routpost.recentlyCapped = true;

				// Envio mensaje
				Bukkit.broadcastMessage(Utils
						.chat(routpostPlugin.getConfig().getString("routpost.capMessage") + factionBreaching.getName()));

				routpostPlugin.getConfig().set("routpost.factionOwnerDontModify", factionBreaching.getId());
				routpostPlugin.saveConfig();

				Faction factionRoutpost = FactionColl.get()
						.getByName(Utils.chat(routpostPlugin.getConfig().getString("routpost.worldName")));

				routpost.setOutpostOwnerId(factionBreaching.getId());

				setRelations(factionRoutpost, factionBreached, factionBreaching);

				restartRoutpostBaseWhenBreached();

			} else {
				event.setCancelled(true);
			}
		}
	}

	public void onEntityExplodeWhenCapped(EntityExplodeEvent event) {

		if (!(event.getEntity() instanceof TNTPrimed))
			return;

		Location sourceLoc = ((TNTPrimed) event.getEntity()).getSourceLoc();
		if (sourceLoc == null)
			return;

		if (!checkExplosionAtBuffer(event.getLocation()))
			return;

		if (!routpost.recentlyCapped)
			return;

		event.setCancelled(true);
	}

	public boolean checkFactionIsClaimed(Faction factionBreaching) {
		if (factionBreaching.isNormal()) {
			return true;
		}

		return false;
	}

	public boolean checkExplosionAtCube(Location explosionLocation) {
		if (explosionLocation.getWorld() != Bukkit.getServer()
				.getWorld(routpostPlugin.getConfig().getString("routpost.worldName")))
			return false;
		if (explosionLocation.getBlockX() < minX)
			return false;
		if (explosionLocation.getBlockX() > maxX)
			return false;
		if (explosionLocation.getBlockY() < minY)
			return false;
		if (explosionLocation.getBlockY() > maxY)
			return false;
		if (explosionLocation.getBlockZ() < minZ)
			return false;
		if (explosionLocation.getBlockZ() > maxZ)
			return false;

		return true;
	}

	public boolean checkExplosionAtBuffer(Location explosionLocation) {
		if (explosionLocation.getWorld() != Bukkit.getServer()
				.getWorld(routpostPlugin.getConfig().getString("routpost.worldName")))
			return false;
		if (explosionLocation.getBlockX() < bufferMinX)
			return false;
		if (explosionLocation.getBlockX() > bufferMaxX)
			return false;
		if (explosionLocation.getBlockZ() < bufferMinZ)
			return false;
		if (explosionLocation.getBlockZ() > bufferMaxZ)
			return false;

		return true;
	}

	public void setRelations(Faction factionRoutpost, Faction factionBreached, Faction factionBreaching) {
		// Quito las antiguas relaciones
		if (factionBreached != factionRoutpost) {
			factionBreached.setRelationWish(factionRoutpost, Rel.NEUTRAL);
			factionRoutpost.setRelationWish(factionBreached, Rel.NEUTRAL);
		}

		// Pongo las nuevas relaciones
		factionBreaching.setRelationWish(factionRoutpost, Rel.TRUCE);
		factionRoutpost.setRelationWish(factionBreaching, Rel.TRUCE);
	}

	public void restartRoutpostBaseWhenBreached() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(routpostPlugin, new Runnable() {
			@Override
			public void run() {
				// Teletransporto a todos fuera
				SchematicUtil.teleportAllPlayersOut(cubo,
						new Location(Bukkit.getWorld(routpostPlugin.getConfig().getString("teleportSpawn.worldName")),
								Double.parseDouble(routpostPlugin.getConfig().getString("teleportSpawn.X")),
								Double.parseDouble(routpostPlugin.getConfig().getString("teleportSpawn.Y")),
								Double.parseDouble(routpostPlugin.getConfig().getString("teleportSpawn.Z"))));

				// Guardo el momento en el que lo he capturado
				routpost.cappedAtMillis = System.currentTimeMillis();
				routpost.lastPrizeMillis = System.currentTimeMillis();
				routpost.nextPrizeMillis = System.currentTimeMillis() + 60 * 60 * 1000;

				routpostPlugin.getConfig().set("routpost.cappedAtMillisDontModify",
						Long.toString(routpost.getCappedAtMillis()));
				routpostPlugin.getConfig().set("routpost.lastPrizeMillisDontModify",
						Long.toString(routpost.getLastPrizeMillis()));
				routpostPlugin.getConfig().set("routpost.nextPrizeMillisDontModify",
						Long.toString(routpost.getNextPrizeMillis()));
				routpostPlugin.saveConfig();

				// Pego las schematicas
				Bukkit.getScheduler().scheduleSyncDelayedTask(routpostPlugin, new Runnable() {
					@Override
					public void run() {
						SchematicUtil.pasteBaseBuffer(routpostPlugin.getConfig().getString("schematics.buffer1.name"),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer1.X")),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer1.Y")),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer1.Z")));

						if (routpostPlugin.getConfig().getString("schematics.buffer2.name").length() > 0) {
							SchematicUtil.pasteBaseBuffer(
									routpostPlugin.getConfig().getString("schematics.buffer2.name"),
									Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer2.X")),
									Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer2.Y")),
									Integer.parseInt(routpostPlugin.getConfig().getString("schematics.buffer2.Z")));
						}

						SchematicUtil.pasteBaseInterior(routpostPlugin.getConfig().getString("schematics.base.name"),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.base.X")),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.base.Y")),
								Integer.parseInt(routpostPlugin.getConfig().getString("schematics.base.Z")));

						Bukkit.getScheduler().scheduleSyncDelayedTask(routpostPlugin, new Runnable() {
							@Override
							public void run() {
								Location hologramLocation = new Location(
										Bukkit.getWorld(routpostPlugin.getConfig().getString("routpost.worldName")),
										Integer.parseInt(routpostPlugin.getConfig().getString("routpost.chestCoords.X"))
												+ 0.5,
										Integer.parseInt(routpostPlugin.getConfig().getString("routpost.chestCoords.Y"))
												- 0.5,
										Integer.parseInt(routpostPlugin.getConfig().getString("routpost.chestCoords.Z"))
												+ 0.5);
								ArmorStand hologram = (ArmorStand) hologramLocation.getWorld()
										.spawnEntity(hologramLocation, EntityType.ARMOR_STAND);

								hologram.setGravity(false);
								hologram.setCustomName(Utils.chat("&bPrize will be available in XX:XX"));
								hologram.setCustomNameVisible(true);
								hologram.setVisible(false);
							}
						}, 50L);

						// Habilito la captura
						routpost.recentlyCapped = false;
					}
				}, 100L);
			}
		}, 200L);
	}
}
