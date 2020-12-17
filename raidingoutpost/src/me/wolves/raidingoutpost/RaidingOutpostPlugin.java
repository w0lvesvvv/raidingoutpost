package me.wolves.raidingoutpost;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;

import me.wolves.raidingoutpost.commands.RaidingOutpostLoad;
import me.wolves.raidingoutpost.entity.RaidingOutpost;
import me.wolves.raidingoutpost.listeners.ChestListener;
import me.wolves.raidingoutpost.listeners.ExplosionListener;
import me.wolves.raidingoutpost.listeners.FactionsListener;
import me.wolves.raidingoutpost.util.Utils;

public class RaidingOutpostPlugin extends JavaPlugin {

	private static RaidingOutpostPlugin plugin;
	private RaidingOutpost routpost;

	public RaidingOutpostPlugin() {
		plugin = this;
	}

	public static RaidingOutpostPlugin get() {
		return plugin;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		Utils.tellConsole(Utils.chat("&B[RaidingOutpost] Plugin is been launched..."));

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				routpost = new RaidingOutpost();
				getRaidingOutpost();

				if (plugin.getConfig().getString("routpost.chestCoords.X").length() > 0) {
					routpost.chestLocation = new Location(
							Bukkit.getWorld(plugin.getConfig().getString("routpost.worldName")),
							Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.X")),
							Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.Y")),
							Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.Z")));
				}

				if (plugin.getConfig().getString("routpost.nextPrizeMillisDontModify").length() > 0) {
					routpost.nextPrizeMillis = Long
							.parseLong(plugin.getConfig().getString("routpost.nextPrizeMillisDontModify"));
				}

				if (plugin.getConfig().getString("routpost.cappedAtMillisDontModify").length() > 0) {
					routpost.cappedAtMillis = Long
							.parseLong(plugin.getConfig().getString("routpost.cappedAtMillisDontModify"));
				}

				if (plugin.getConfig().getString("routpost.lastPrizeMillisDontModify").length() > 0) {
					routpost.lastPrizeMillis = Long
							.parseLong(plugin.getConfig().getString("routpost.lastPrizeMillisDontModify"));
				}

				routpost.nextUnclaim = System.currentTimeMillis() + (6*60*60*1000);
				
				new RaidingOutpostLoad(plugin, routpost);
				new ExplosionListener(plugin, routpost);
				new FactionsListener(plugin, routpost);
				new ChestListener(plugin, routpost);

				ExecuteAtHourEvery24H(15, 00, 00, 24 * 60 * 60 * 20); // Cada 24h
				ExecuteEvery6H(6*60*60*20); // Cada 6h
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						CalculateHologram();
					}
				}, 10 * 20);
			}
		}, 20 * 20);

	}

	public Faction getRaidingOutpost() {
		String tag = Utils.chat(plugin.getConfig().getString("routpost.tag"));
		Faction faction = FactionColl.get().getByName(tag);
		if (faction == null || faction == FactionColl.get().getNone()) {
			faction = FactionColl.get().create();
			faction.setDescription(plugin.getConfig().getString("routpost.description"));
		}
		faction.setName(tag);
		faction.setPowerBoost(1000D);
		String factionOwnerID = plugin.getConfig().getString("routpost.factionOwnerDontModify");
		if (factionOwnerID != null) {
			if (factionOwnerID.length() > 0) {
				routpost.setOutpostOwnerId(factionOwnerID);
			} else {
				routpost.setOutpostOwnerId(
						FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag")).getId());
			}
		} else {
			routpost.setOutpostOwnerId(
					FactionColl.get().getByName(plugin.getConfig().getString("routpost.tag")).getId());
		}

		return faction;
	}

	public void CalculateHologram() {

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				//Calculo el tiempo para el proximo unclaim
				routpost.millisUntilUnclaim = Utils.CalculateSecondsUntil(routpost.getNextUnclaim())*1000;
				
				//Calculo tiempo para el holograma
				if (plugin.getConfig().getString("routpost.chestCoords.X").length() > 0) {
					Collection<Entity> listEntities = Bukkit
							.getWorld(plugin.getConfig().getString("routpost.worldName")).getNearbyEntities(
									new Location(Bukkit.getWorld(plugin.getConfig().getString("routpost.worldName")),
											Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.X")),
											Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.Y")),
											Double.parseDouble(plugin.getConfig().getString("routpost.chestCoords.Z"))),
									1, 1, 1);

					if (!routpost.getOutpostOwnerTag().equals(plugin.getConfig().getString("routpost.tag"))) {
						if (listEntities != null) {
							for (Entity entity : listEntities) {
								if (entity.getType() == EntityType.ARMOR_STAND) {
									ArmorStand hologram = (ArmorStand) entity;

									ZonedDateTime nowZoned = ZonedDateTime.now();
									Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone())
											.toInstant();
									Duration duration = Duration.between(midnight, Instant.now());
									long seconds = duration.getSeconds();

									Calendar calendar = Calendar.getInstance();
									calendar.set(Calendar.HOUR_OF_DAY,
											Utils.getDate(routpost.getNextPrizeMillis()).getHours());
									calendar.set(Calendar.MINUTE,
											Utils.getDate(routpost.getNextPrizeMillis()).getMinutes());
									calendar.set(Calendar.SECOND,
											Utils.getDate(routpost.getNextPrizeMillis()).getSeconds());
									calendar.set(Calendar.MILLISECOND, 0);
									Instant restart = calendar.toInstant();
									Duration restartDuration = Duration.between(midnight, restart);
									long restartSeconds = restartDuration.getSeconds();

									long finalSeconds = restartSeconds - seconds;

									if (!routpost.getOutpostOwnerTag()
											.equals(plugin.getConfig().getString("routpost.tag"))) {
										if (System.currentTimeMillis() < routpost.getNextPrizeMillis()) {
											hologram.setCustomName(Utils.chat("&bPrize will be available in "
													+ Utils.getDate(finalSeconds * 1000).getMinutes() + ":"
													+ Utils.getDate(finalSeconds * 1000).getSeconds()));
											routpost.prizeAvailableMessage = true;
										} else {
											hologram.setCustomName(Utils.chat("&bReclaim the prize now!"));
											if (routpost.prizeAvailableMessage) {
												Bukkit.broadcastMessage(
														Utils.chat("&bThere is a prize available at routpost!"));
												routpost.prizeAvailableMessage = false;
											}
										}

									} else {
										hologram.setCustomName(Utils.chat("&bPrize will be available in XX:XX"));
									}
								}
							}
						}
					}
				}
			}
		}, 0, 20);
	}

	@SuppressWarnings("deprecation")
	public void ExecuteAtHourEvery24H(int Hour, int Minute, int Second, long GameTick) {
		ZonedDateTime nowZoned = ZonedDateTime.now();
		Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
		Duration duration = Duration.between(midnight, Instant.now());
		long seconds = duration.getSeconds();

		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.HOUR_OF_DAY) > Hour) {
			calendar.add(Calendar.DATE, 1);
		} else if (calendar.get(Calendar.HOUR_OF_DAY) == Hour) {
			if (calendar.get(Calendar.MINUTE) > Minute) {
				calendar.add(Calendar.DATE, 1);
			} else if (calendar.get(Calendar.MINUTE) == Minute) {
				if (calendar.get(Calendar.SECOND) >= Second) {
					calendar.add(Calendar.DATE, 1);
				}
			}
		}
		calendar.set(Calendar.HOUR_OF_DAY, Hour);
		calendar.set(Calendar.MINUTE, Minute);
		calendar.set(Calendar.SECOND, Second);
		calendar.set(Calendar.MILLISECOND, 0);
		Instant restart = calendar.toInstant();
		Duration restartDuration = Duration.between(midnight, restart);
		long restartSeconds = restartDuration.getSeconds();

		long finalSeconds = restartSeconds - seconds;

		Utils.tellConsole(Utils.chat("&b[Routpost] World restart programmed at "
				+ Utils.getDate(System.currentTimeMillis() + (finalSeconds * 1000)).getHours() + ":"
				+ Utils.getDate(System.currentTimeMillis() + (finalSeconds * 1000)).getMinutes()));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				Utils.executeCommand("routpost restartWorld");
				Bukkit.broadcastMessage(Utils.chat("&b[Routpost] The routpost world has been restarted."));
			}
		}, finalSeconds * 20, GameTick);
	}

	public void ExecuteEvery6H (long GameTick) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				unclaimRoutpostWorld();
				Bukkit.broadcastMessage(Utils.chat("&b[Routpost] &fRoutpost world has been unclaimed."));
				routpost.nextUnclaim = System.currentTimeMillis() + (6*60*60*1000);
			}
		}, 6*60*60*20, GameTick);
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
}
