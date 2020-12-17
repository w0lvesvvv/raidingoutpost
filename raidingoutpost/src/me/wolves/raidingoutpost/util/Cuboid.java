package me.wolves.raidingoutpost.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Cuboid {
	
	  public int xMin;
	  
	  public int xMax;
	  
	  public int yMin;
	  
	  public int yMax;
	  
	  public int zMin;
	  
	  public int zMax;
	  
	  public World world;
	  
	  public Cuboid(Location point1, Location point2) {
	    this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
	    this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
	    this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
	    this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
	    this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
	    this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
	    this.world = point1.getWorld();
	  }
	  
	  public Cuboid(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, World world) {
	    this.xMin = xMin;
	    this.xMax = xMax;
	    this.yMin = yMin;
	    this.yMax = yMax;
	    this.zMin = zMin;
	    this.zMax = zMax;
	    this.world = world;
	  }
	  
	  @SuppressWarnings("deprecation")
	public static Cuboid deserialize(JavaPlugin p, String s) {
	    World world;
	    int xMax, yMax, zMax;
	    if (s == null)
	      return null; 
	    String s1 = s.split("@")[0];
	    try {
	      world = p.getServer().getWorld(s.split("@")[1]);
	    } catch (ArrayIndexOutOfBoundsException e) {
	      world = p.getServer().getWorlds().get(0);
	    } 
	    String s2 = s1.split(":")[0];
	    String[] sR = s2.split(",");
	    int xMin = (new Integer(sR[0])).intValue();
	    int yMin = (new Integer(sR[2])).intValue();
	    int zMin = (new Integer(sR[1])).intValue();
	    if (s1.contains(":")) {
	      String s3 = s1.split(":")[1];
	      String[] sR1 = s3.split(",");
	      xMax = (new Integer(sR1[0])).intValue();
	      yMax = (new Integer(sR1[2])).intValue();
	      zMax = (new Integer(sR1[1])).intValue();
	    } else {
	      xMax = xMin;
	      yMax = yMin;
	      zMax = zMin;
	    } 
	    return new Cuboid(xMin, xMax, yMin, yMax, zMin, zMax, world);
	  }
	  
	  public boolean contains(Location loc) {
	    if (loc.getWorld() != this.world)
	      return false; 
	    if (loc.getBlockX() < this.xMin)
	      return false; 
	    if (loc.getBlockX() > this.xMax)
	      return false; 
	    if (loc.getBlockY() < this.yMin)
	      return false; 
	    if (loc.getBlockY() > this.yMax)
	      return false; 
	    if (loc.getBlockZ() < this.zMin)
	      return false; 
	    if (loc.getBlockZ() > this.zMax)
	      return false; 
	    return true;
	  }
	  
	  public String serialize() {
	    return "" + this.xMin + "," + this.zMin + "," + this.yMin + ":" + this.xMax + "," + this.zMax + "," + this.yMax + "@" + this.world.getName();
	  }
	  
	  public String toString() {
	    return serialize();
	  }
	  
	  public int getXWidth() {
	    return this.xMax - this.xMin;
	  }
	  
	  public int getZWidth() {
	    return this.zMax - this.zMin;
	  }
	  
	  public int getHeight() {
	    return this.yMax - this.yMin;
	  }
	  
	  public int getArea() {
	    return getHeight() * getXWidth() * getZWidth();
	  }
	}
