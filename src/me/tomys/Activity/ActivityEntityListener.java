package me.tomys.Activity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ActivityEntityListener extends EntityListener{
	public static Activity plugin;
	public ActivityEntityListener(Activity instance) { 
        plugin = instance;
    }
	public void onEntityDamage(EntityDamageEvent event){
		if (event.getCause()==EntityDamageEvent.DamageCause.ENTITY_ATTACK){
			EntityDamageByEntityEvent entEvent = (EntityDamageByEntityEvent)event;
			Entity damager = entEvent.getDamager();
			if (damager instanceof Player){
				if (Activity.permissionHandler.has((Player)damager, "activity.active"))
					if (event.getDamage()!=0)
						plugin.reportActivity("damage", (Player)damager, event.getDamage());
			}
		}
	}
}
