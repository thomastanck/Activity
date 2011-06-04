package me.tomys.Activity;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ActivityPlayerListener extends PlayerListener{
	public static Activity plugin;
	public ActivityPlayerListener(Activity instance) { 
        plugin = instance;
    }
	public void onPlayerChat (PlayerChatEvent event){
		if (Activity.permissionHandler.has(event.getPlayer(), "activity.active"))
		plugin.reportActivity("chat",event.getPlayer());
	}
	public void onPlayerMove (PlayerMoveEvent event){
		if (Activity.permissionHandler.has(event.getPlayer(), "activity.active"))
		plugin.reportActivity("movement",event.getPlayer());
	}
}
