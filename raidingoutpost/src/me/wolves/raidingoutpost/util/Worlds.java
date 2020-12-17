package me.wolves.raidingoutpost.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;

import me.wolves.raidingoutpost.RaidingOutpostPlugin;

public class Worlds {

	public void importWorlds(RaidingOutpostPlugin plugin) {
		boolean errors = false;
		File backupDir = new File(plugin.getDataFolder(), "backups");
		byte b;
		int i;
		File[] arrayOfFile;
		for (i = (arrayOfFile = backupDir.listFiles()).length, b = 0; b < i;) {
			File source = arrayOfFile[b];
			if (source.isDirectory()) {
				File target = new File(plugin.getServer().getWorldContainer(), source.getName());
				if (target.exists() && target.isDirectory() && !delete(target)) {
					Utils.logConsoleSevere("Failed to reset world \"" + source.getName() + "\" - could not delete old "
							+ "world folder.");
					errors = true;
				} else {
					try {
						copyDir(source, target);
					} catch (IOException e) {
						e.printStackTrace();
						Utils.logConsoleSevere("Failed to reset world \"" + source.getName()
								+ "\" - could not import the " + "world from backup.");
						errors = true;
					}
					Utils.logConsoleInfo(
							"Import of world \"" + source.getName() + "\" " + (errors ? "failed!" : "succeeded!"));
					errors = false;
				}
			}
			b++;
		}
	}

	public void deleteWorlds(RaidingOutpostPlugin plugin) {
		boolean worldsListed = false;
		for (String worldName : plugin.getConfig().getStringList("random-seed.worlds")) {
			if (!worldsListed)
				worldsListed = true;
			File target = new File(plugin.getServer().getWorldContainer(), worldName);
			if (!target.exists()) {
				Utils.logConsoleSevere("Could not load world \"" + worldName + "\" with a random seed: no such world "
						+ "exists in the server directory!");
				return;
			}
			if (target.isDirectory()) {
				if (!delete(target)) {
					Utils.logConsoleSevere(
							"Failed to delete world \"" + worldName + "\", perhaps the folder is locked?");
					continue;
				}
				Utils.logConsoleInfo("Successfully loaded a random seed for world \"" + worldName + "\"!");
			}
		}
		if (!worldsListed)
			Utils.logConsoleWarning(
					"The random seed option is enabled but no worlds are listed to be deleted and regenerated with random seeds.");
	}

	public static boolean delete(File file) {
		if (file.isDirectory()) {
			byte b;
			int i;
			File[] arrayOfFile;
			for (i = (arrayOfFile = file.listFiles()).length, b = 0; b < i;) {
				File subfile = arrayOfFile[b];
				if (!delete(subfile))
					return false;
				b++;
			}
		}
		if (!file.delete())
			return false;
		return true;
	}
	
	public static void copyDir(File source, File target) throws IOException {
		if (source.isDirectory()) {
			if (!target.exists())
				target.mkdir();
			String[] files = source.list();
			byte b;
			int i;
			String[] arrayOfString1;
			for (i = (arrayOfString1 = files).length, b = 0; b < i;) {
				String file = arrayOfString1[b];
				File srcFile = new File(source, file);
				File destFile = new File(target, file);
				copyDir(srcFile, destFile);
				b++;
			}
		} else {
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0)
				out.write(buffer, 0, length);
			in.close();
			out.close();
		}
	}
	
	public static void restartRoutpost() {

		Utils.executeCommand("mv delete routpost");
		Utils.executeCommand("mv confirm");

		try {
			Worlds.copyDir(new File("routpostBackup"), new File("routpost"));

			Utils.executeCommand("mv import routpost normal");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bukkit.broadcastMessage(Utils.chat("&b[Routpost] &7World has been restarted."));
	}

}
