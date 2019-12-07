package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.StringUtil;

import com.sylvcraft.MVRandomPortal;

public class MVRP implements TabExecutor {
  MVRandomPortal plugin;
  
  public MVRP(MVRandomPortal instance) {
    plugin = instance;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("mvrp.admin")) return null;
    List<String> portals = new ArrayList<>();
    
    switch (args.length) {
    case 1:
      return StringUtil.copyPartialMatches(args[0], new ArrayList<String>(Arrays.asList("add","del","list","toggle")), new ArrayList<String>());

    case 2:
      switch (args[0].toLowerCase()) {
      case "list":
      case "del":
        portals = plugin.getPortals();
        break;

      default:
        portals = plugin.getMultiversePortals();
        break;
      }
      if (args[0].equalsIgnoreCase("toggle")) portals.add(0, "*");
      return StringUtil.copyPartialMatches(args[1], portals, new ArrayList<String>());
      
    case 3:
      switch (args[0].toLowerCase()) {
      case "toggle":
      case "list":
        return new ArrayList<String>(Arrays.asList(""));
        
      case "del":
        portals = plugin.getPortals(args[1]);
        if (portals.size() == 0) return new ArrayList<String>(Arrays.asList(""));
        portals.add(0, "*");
        return StringUtil.copyPartialMatches(args[2], portals, new ArrayList<String>());

      default:
        return StringUtil.copyPartialMatches(args[2], plugin.getMultiversePortals(), new ArrayList<String>());
      }
      
    default:
      return new ArrayList<String>(Arrays.asList(""));
    }
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      if (args.length == 0) {
        showHelp(sender);
        return true;
      }

      Map<String, String> data = new HashMap<String, String>();
      switch (args[0].toLowerCase()) {
      case "add":
        if (args.length < 3) {
          showHelp(sender);
          return true;
        }
        
        data.put("%portal%", args[1]);
        data.put("%newportal%", args[2]);
        plugin.msg("portal-add-" + (addPortal(args[1], args[2])?"success":"fail"), sender, data);
        break;
        
      case "del":
        if (args.length < 3) {
          showHelp(sender);
          return true;
        }
        
        delPortal(sender, args[1], args[2]);
        break;
        
      case "list":
        List<String> portals = (args.length == 1)?plugin.getPortals():plugin.getPortals(args[1]);
        if (portals.size() == 0) {
          plugin.msg("list-none", sender);
          return true;
        }
        
        listPortals(sender, portals);
        break;
        
      case "toggle":
        if (args.length < 2) {
          showHelp(sender);
          return true;
        }
        
        Boolean result = toggleActive(args[1]);
        if (result == null) {
          plugin.msg("invalid", sender);
          return true;
        }
        
        data.put("%portal%", args[1].equals("*")?"all portals":args[1]);
        data.put("%result%", result?"enabled":"disabled");
        plugin.msg("portal-toggle", sender, data);
        break;
      }

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  Boolean toggleActive(String portal) {
    if (portal.equals("*")) {
      boolean newStatus = !plugin.getConfig().getBoolean("global.enabled", true);
      plugin.getConfig().set("global.enabled", newStatus);
      plugin.saveConfig();
      return newStatus;
    }
    
    ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("random_portals." + portal);
    if (cfg == null) return null;
    
    boolean newStatus = !cfg.getBoolean("enabled", true);
    cfg.set("enabled", newStatus);
    plugin.saveConfig();
    return newStatus;
  }
  
  void listPortals(CommandSender sender, List<String> portals) {
    Map<String, String> data = new HashMap<String, String>();
    plugin.msg("list-header", sender);
    for (String portal : portals) {
      data.put("%portal%", portal);
      plugin.msg("list-data", sender, data);
    }
    plugin.msg("list-footer", sender);
  }
  
  boolean addPortal(String portal, String addingPortal) {
    if (!plugin.isValidPortal(portal)) return false;
    if (!plugin.isValidPortal(addingPortal)) return false;
    
    List<String> portals = plugin.getPortals(portal);
    portals.add(addingPortal);
    plugin.getConfig().set("random_portals." + portal + ".destinations", portals);
    plugin.saveConfig();
    return true;
  }
  
  void delPortal(CommandSender sender, String portal, String portalToRemove) {
    Map<String, String> data = new HashMap<String, String>();
    data.put("%portal%", portal);
    data.put("%delportal%", portalToRemove);
    
    if (!plugin.isValidPortal(portal)) {
      plugin.msg("invalid", sender, data);
      return;
    }
    
    if (portalToRemove.equals("*")) {
      plugin.getConfig().set("random_portals." + portal, null);
      plugin.saveConfig();
      plugin.msg("portal-del-all", sender, data);
      return;
    }
    
    if (!plugin.isValidPortal(portal, portalToRemove)) {
      plugin.msg("invalid", sender, data);
      return;
    }

    switch (removePortal(portal, portalToRemove)) {
    case 0:
      plugin.msg("portal-del-fail", sender, data);
      break;
    case 1:
      plugin.msg("portal-del-success", sender, data);
      break;
    case 2:
      plugin.msg("portal-del-all", sender, data);
      break;
    }
  }
  
  int removePortal(String portal, String portalToRemove) {
    ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("random_portals." + portal);
    if (cfg == null) return 0;
  
    List<String> portals = cfg.getStringList("destinations");
    if (!portals.contains(portalToRemove)) return 0;
    
    if (portals.size() == 1) {
      plugin.getConfig().set("random_portals." + portal, null);
      plugin.saveConfig();
      return 2;
    } else {
      portals.remove(portalToRemove);
      cfg.set("destinations", portals);
      plugin.saveConfig();
      return 1;
    }
  }

  void showHelp(CommandSender sender) {
    int displayed = 0;
    if (sender.hasPermission("MVRandomPortal.xxxx")) { plugin.msg("xxxx", sender); displayed++; }
    if (displayed == 0) plugin.msg("access-denied", sender);
  }
}



































