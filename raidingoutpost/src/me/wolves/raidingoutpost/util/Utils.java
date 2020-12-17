package me.wolves.raidingoutpost.util;

import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	public static String chat(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static List<String> chat(List<String> s) {

		List<String> message = new ArrayList<>();

		for (String line : s) {
			message.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		return message;
	}

	public static ItemStack createItem(int materialId, int amount, boolean enchanted, String displayName,
			String... loreString) {

		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(materialId), amount);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.chat(displayName));
		List<String> lore = new ArrayList<String>();
		for (String s : loreString) {
			lore.add(Utils.chat(s));
		}
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.values());
		if (enchanted) {
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack createItem(int materialId, int amount, boolean enchanted, String displayName,
			List<String> loreString) {

		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(materialId), amount);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.chat(displayName));
		List<String> lore = new ArrayList<String>();
		for (String s : loreString) {
			lore.add(Utils.chat(s));
		}
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.values());
		if (enchanted) {
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack createItemByte(int materialId, int byteId, int amount, boolean enchanted,
			String displayName, List<String> loreString) {

		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(materialId), amount, (short) byteId);
		;

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.chat(displayName));
		List<String> lore = new ArrayList<String>();
		for (String s : loreString) {
			lore.add(Utils.chat(s));
		}
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.values());
		if (enchanted) {
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		item.setItemMeta(meta);

		return item;
	}

	public static List<Block> getNearbyBlocks(Location location, int radius) {
		List<Block> blocks = new ArrayList<Block>();
		for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
			for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
				for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
					blocks.add(location.getWorld().getBlockAt(x, y, z));
				}
			}
		}
		return blocks;
	}

	public static long CalculateSecondsUntil(int Hours, int Minutes, int Seconds) {
		ZonedDateTime nowZoned = ZonedDateTime.now();
		Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone())
				.toInstant();
		Duration duration = Duration.between(midnight, Instant.now());
		long seconds = duration.getSeconds();

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Hours);
		calendar.set(Calendar.MINUTE, Minutes);
		calendar.set(Calendar.SECOND, Seconds);
		calendar.set(Calendar.MILLISECOND, 0);
		Instant restart = calendar.toInstant();
		Duration restartDuration = Duration.between(midnight, restart);
		long restartSeconds = restartDuration.getSeconds();

		long finalSeconds = restartSeconds - seconds;
		
		return finalSeconds;
	}

	@SuppressWarnings("deprecation")
	public static long CalculateSecondsUntil(long miliseconds) {
		ZonedDateTime nowZoned = ZonedDateTime.now();
		Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone())
				.toInstant();
		Duration duration = Duration.between(midnight, Instant.now());
		long seconds = duration.getSeconds();

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, getDate(miliseconds).getHours());
		calendar.set(Calendar.MINUTE, getDate(miliseconds).getMinutes());
		calendar.set(Calendar.SECOND, getDate(miliseconds).getSeconds());
		calendar.set(Calendar.MILLISECOND, 0);
		Instant restart = calendar.toInstant();
		Duration restartDuration = Duration.between(midnight, restart);
		long restartSeconds = restartDuration.getSeconds();

		long finalSeconds = restartSeconds - seconds;
		
		return finalSeconds;
	}
	
	public static Date getDate(long milli) {
		Date currentTime = new Date(milli);
		return currentTime;
	}

	public static void tellConsole(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public static void executeCommand(String message) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
	}

	public static void logConsoleInfo(String message) {
		Bukkit.getLogger().info(message);
	}

	public static void logConsoleWarning(String message) {
		Bukkit.getLogger().warning(message);
	}

	public static void logConsoleSevere(String message) {
		Bukkit.getLogger().severe(message);
	}

}
