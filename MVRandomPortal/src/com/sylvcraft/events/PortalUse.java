package com.sylvcraft.events;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.sylvcraft.MVRandomPortal;

public class PortalUse implements Listener {
  Random rnd = new Random();
  MVRandomPortal plugin;

  public PortalUse(MVRandomPortal plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler
  public void onPortal(MVPortalEvent e) {
    if (!plugin.getConfig().getBoolean("global.enabled", true)) return;
    
    String cfgRoot = "random_portals." + e.getSendingPortal().getName();
    ConfigurationSection cfg = plugin.getConfig().getConfigurationSection(cfgRoot);
    if (cfg == null) return;
    
    if (!plugin.getConfig().getBoolean(cfgRoot + ".enabled", true)) return;
    
    List<String> destinations = plugin.getPortals(e.getSendingPortal().getName());
    if (destinations.size() == 0) return;
    
    int destination = rnd.nextInt(destinations.size());
    MultiversePortals mvPortals = (MultiversePortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
    if (mvPortals == null) return;
    
    MVPortal p = mvPortals.getPortalManager().getPortal(destinations.get(destination));
    if (p == null) return;
    
    e.setCancelled(true);
    e.getTeleportee().teleport(p.getDestination().getLocation(e.getTeleportee()));
  }

}
