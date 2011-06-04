package me.tomys.Activity;

import me.tomys.Activity.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.*;

import com.nijikokun.register.payment.Method;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Activity extends JavaPlugin{

    public PluginDescriptionFile info = null;
    public PluginManager pm = this.getServer().getPluginManager();
	
	private final ActivityPlayerListener playerListener = new ActivityPlayerListener(this);
	private final ActivityBlockListener blockListener = new ActivityBlockListener(this);
	private final ActivityEntityListener entityListener = new ActivityEntityListener(this);
	Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler permissionHandler;
	
	public double chatmul;
	public double blockplacemul;
	public double blockbreakmul;
	public double damagemul;
	
	public Server server;
	public Player[] players;
	
	public Method Method = null;
	
	Timer timer;

    private static final File myfile= new File("plugins" + File.separator + "Activity" + File.separator + "config.yml");
    Configuration config = load();
	
	public void onEnable(){ 
		log.info("[[Activity]] has been enabled.");

        try {
                setupConfig();
        } catch (SecurityException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        
		setupPermissions();
		
		if (config.getProperty("enable.chat").equals(1))
			pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
		if (config.getProperty("enable.damage").equals(1))
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
		if (config.getProperty("enable.blockplace").equals(1))
			pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
		if (config.getProperty("enable.blockbreak").equals(1))
			pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
		if (config.getProperty("enable.movement").equals(1))
			pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Monitor, this);
		

        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new server(this), Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, new server(this), Priority.Monitor, this);
		
		timer();
		server=this.getServer();
	}
	
	public void onDisable(){ 
		timer.cancel();
		timer.purge();
		log.info("[[Activity]] has been disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (commandLabel.equalsIgnoreCase("activity")){
			return displayActivity(sender);
		}
		if (commandLabel.equalsIgnoreCase("setmultiplier")){
			return setMultiplier(sender, args);
		}
		if (commandLabel.equalsIgnoreCase("setmul")){
			return setMultiplier(sender, args);
		}
		if (commandLabel.equalsIgnoreCase("setinterval")){
			return setInterval(sender, args);
		}
		return false;
	}

	private boolean setInterval(CommandSender sender, String[] args) {
		if(!permissionHandler.has((Player)sender, "activity.setinteval")){
			sender.sendMessage("You do not have sufficient permissions to use this command.");
			return true;
		}
		return saveInterval(args[0]);
	}

	private boolean displayActivity(CommandSender sender) {
		if(permissionHandler.has((Player)sender, "activity.active")){
			sender.sendMessage("Your Activity is "+String.valueOf(calcActivity((Player)sender))+".");
		}
		return true;
	}

	private double calcActivity(Player player) {
		String name = player.getName();
		double total = 0;
		if(config.getProperty("players."+name+".chat")!=null)
			total += Double.parseDouble(String.valueOf(config.getProperty("players."+name+".chat"))+".0")*Double.parseDouble(String.valueOf(config.getProperty("multipliers.chat"))+".0");
		if(config.getProperty("players."+name+".blockplace")!=null)
			total += Double.parseDouble(String.valueOf(config.getProperty("players."+name+".blockplace"))+".0")*Double.parseDouble(String.valueOf(config.getProperty("multipliers.blockplace"))+".0");
		if(config.getProperty("players."+name+".blockbreak")!=null)
			total += Double.parseDouble(String.valueOf(config.getProperty("players."+name+".blockbreak"))+".0")*Double.parseDouble(String.valueOf(config.getProperty("multipliers.blockbreak"))+".0");
		if(config.getProperty("players."+name+".damage")!=null)
			total += Double.parseDouble(String.valueOf(config.getProperty("players."+name+".damage"))+".0")*Double.parseDouble(String.valueOf(config.getProperty("multipliers.damage"))+".0");
		if(config.getProperty("players."+name+".movement")!=null)
			total += Double.parseDouble(String.valueOf(config.getProperty("players."+name+".damage"))+".0")*Double.parseDouble(String.valueOf(config.getProperty("multipliers.damage"))+".0");
		return total;
	}

	private boolean setMultiplier(CommandSender sender, String[] args) {
		if(!permissionHandler.has((Player)sender, "activity.setmul")){
			sender.sendMessage("You do not have sufficient permissions to use this command.");
			return true;
		}
		if(args.length>1){
			return saveMultiplier(args[0],args[1]);
		}
		return false;
	}
	
	private boolean saveMultiplier(String type, String value) {
		if(type.equalsIgnoreCase("chat")){
			config.setProperty("multipliers.chat", Integer.parseInt(value));
			chatmul=Double.parseDouble(value);
		}
		if(type.equalsIgnoreCase("blockplace")){
			config.setProperty("multipliers.blockplace", Integer.parseInt(value));
			blockplacemul=Double.parseDouble(value);
		}
		if(type.equalsIgnoreCase("blockbreak")){
			config.setProperty("multipliers.blockbreak", Integer.parseInt(value));
			blockbreakmul=Double.parseDouble(value);
		}
		if(type.equalsIgnoreCase("damage")){
			config.setProperty("multipliers.damage", Integer.parseInt(value));
			damagemul=Double.parseDouble(value);
		}
		if(type.equalsIgnoreCase("total")){
			config.setProperty("multipliers.total", Integer.parseInt(value));
			damagemul=Double.parseDouble(value);
		}
		config.save();
		return true;
	}
	
	private boolean saveInterval(String interval) {
		config.setProperty("interval", interval);
		config.save();
		return true;
	}

	private void setupPermissions() {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (permissionHandler == null) {
			if (permissionsPlugin != null) {
				permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				log.info("[[Activity]]: Permissions detected.");
			} else {
				log.info("[[Activity]]: Permission system not detected, Activity disabled.");
			}
		}
	}

	private void setupConfig() throws SecurityException, IOException {
		System.out.println("[[Activity]]: Setting up config file");
		if(!new File("plugins" + File.separator + "Activity").exists()){
			new File("plugins" + File.separator + "Activity").mkdir();
		}
		if(!new File("plugins" + File.separator + "Activity" + File.separator + "config.yml").exists()){
			Handler handler = new FileHandler("plugins" + File.separator + "Activity" + File.separator + "config.yml");
			handler.setFormatter(new SimpleFormatter());
			config = load();
			config.setProperty("multipliers", 1);
			config.setProperty("multipliers.chat", 1);
			config.setProperty("multipliers.blockplace", 1);
			config.setProperty("multipliers.blockbreak", 1);
			config.setProperty("multipliers.damage", 1);
			config.setProperty("multipliers.movement", 1);
			config.setProperty("multipliers.total", 1);
			config.setProperty("enable", 1);
			config.setProperty("enable.chat", 1);
			config.setProperty("enable.blockplace", 1);
			config.setProperty("enable.blockbreak", 1);
			config.setProperty("enable.damage", 1);
			config.setProperty("enable.movement", 0);
			config.setProperty("interval",600000);
			config.setProperty("players", 1);
			config.save();
			if(!handler.isLoggable(null)){
				System.out.println("[[Activity]]: Config file set up and readable!");
			}else{
				System.out.println("[[Activity]]: Config file failed to create!");
			}
		}else{
			System.out.println("[[Activity]]: Config file found!");
		}
	}

	public Configuration load() {
		try {
			Configuration PluginPropConfig = new Configuration(myfile);
			PluginPropConfig.load();
			return PluginPropConfig;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
	
	public void reportActivity(String type, Player player){
		if(player!=null){
			String name = player.getName();
			if(config.getProperty("players."+name)==null)
				config.setProperty("players."+name, 1);
			if(config.getProperty("players."+name+"."+type)==null)
				config.setProperty("players."+name+"."+type, 0);
			config.setProperty("players."+name+"."+type, Integer.valueOf(String.valueOf(config.getProperty("players."+name+"."+type)))+1);
			config.save();
		}
	}
	
	public void reportActivity(String type, Player player, int amount){
		if(player!=null){
			String name = player.getName();
			if(config.getProperty("players."+name)==null)
				config.setProperty("players."+name, 1);
			if(config.getProperty("players."+name+"."+type)==null)
				config.setProperty("players."+name+"."+type, 0);
			config.setProperty("players."+name+"."+type, Integer.valueOf(String.valueOf(config.getProperty("players."+name+"."+type)))+amount);
			config.save();
		}
	}
	
	private void timer(){
	    timer = new Timer();
	    int interval = Integer.parseInt(String.valueOf(config.getProperty("interval")));
	    timer.schedule(new timerAction(), interval, interval);
	}
	
	class timerAction extends TimerTask{
		public void run() {
			players=server.getOnlinePlayers();
			for(int x=0;x<players.length;x++){
				if(permissionHandler.has(players[x], "activity.active")){
					double theactivity=calcActivity(players[x]);
					config.setProperty("players."+players[x].getName()+".chat", 0);
					config.setProperty("players."+players[x].getName()+".blockplace", 0);
					config.setProperty("players."+players[x].getName()+".blockbreak", 0);
					config.setProperty("players."+players[x].getName()+".movement", 0);
					config.setProperty("players."+players[x].getName()+".damage", 0);
					double themoney=theactivity*Double.parseDouble(String.valueOf(config.getProperty("multipliers.total")));
					players[x].sendMessage("Your Activity is "+String.valueOf(theactivity)+" and "+Method.format(themoney)+(themoney==Double.parseDouble("1.0")?" has":" have")+" been added to your account.");
					Method.getAccount(players[x].getName()).add(themoney);
					config.save();
				}
			}
		}
	}
}
