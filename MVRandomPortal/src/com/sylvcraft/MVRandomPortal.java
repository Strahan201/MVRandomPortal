package com.sylvcraft;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.sylvcraft.commands.MVRP;
import com.sylvcraft.events.PortalUse;

public class MVRandomPortal extends JavaPlugin {
  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(new PortalUse(this), this);
    getCommand("mvrp").setExecutor(new MVRP(this));
    saveDefaultConfig();
  }

  public boolean isValidPortal(String portal) {
    MultiversePortals mvPortals = (MultiversePortals) getServer().getPluginManager().getPlugin("Multiverse-Portals");
    for (MVPortal mvportal : mvPortals.getPortalManager().getAllPortals()) {
      if (mvportal.getName().equalsIgnoreCase(portal)) return true;
    }
    return false;
  }
  
  public boolean isValidPortal(String portal, String subportal) {
    if (!isValidPortal(portal)) return false;
    if (!getPortals(portal).contains(subportal)) return false;
    return true;
  }
  
  public List<String> getPortals() {
    List<String> ret = new ArrayList<>();
    ConfigurationSection cfg = getConfig().getConfigurationSection("random_portals");
    if (cfg == null) return ret;
    
    return new ArrayList<String>(cfg.getKeys(false));
  }
  
  public List<String> getPortals(String portal) {
    List<String> ret = new ArrayList<>();
    ConfigurationSection cfg = getConfig().getConfigurationSection("random_portals." + portal);
    if (cfg == null) return ret;
    
    return cfg.getStringList("destinations");
  }
  
  public List<String> getMultiversePortals() {
    List<String> ret = new ArrayList<>();
    MultiversePortals mvPortals = (MultiversePortals) getServer().getPluginManager().getPlugin("Multiverse-Portals");
    for (MVPortal portal : mvPortals.getPortalManager().getAllPortals()) ret.add(portal.getName());
    return ret;
  }
  
  public void msg(String msgCode, CommandSender sender) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	msgTransmit(getConfig().getString("messages." + msgCode), sender);
  }

  public void msg(String msgCode, CommandSender sender, Map<String, String> data) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	String tmp = getConfig().getString("messages." + msgCode, msgCode);
  	for (Map.Entry<String, String> mapData : data.entrySet()) {
  	  tmp = tmp.replace(mapData.getKey(), mapData.getValue());
  	}
  	msgTransmit(tmp, sender);
  }
  
  public void msgTransmit(String msg, CommandSender sender) {
  	for (String m : (msg + " ").split("%br%")) {
  		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
  	}
  }
}