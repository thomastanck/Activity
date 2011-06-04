package me.tomys.Activity;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ActivityBlockListener extends BlockListener{
	public static Activity plugin;
	public ActivityBlockListener(Activity instance) { 
        plugin = instance;
    }
	public void onBlockPlace (BlockPlaceEvent event){
		if (Activity.permissionHandler.has(event.getPlayer(), "activity.active"))
		plugin.reportActivity("blockplace",event.getPlayer());
	}
	public void onBlockBreak (BlockBreakEvent event){
		if (Activity.permissionHandler.has(event.getPlayer(), "activity.active"))
		plugin.reportActivity("blockbreak",event.getPlayer());
	}
}
