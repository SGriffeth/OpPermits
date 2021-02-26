package name.griffeth.sebastian.OpPermits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World; 
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import name.griffeth.sebastian.OpPermits.Files.DataManager;
import net.md_5.bungee.api.ChatColor;

public class OpPermitsListener extends JavaPlugin implements Listener {
	    
	//Hash map to determine whether a player is a permanent OP 
	protected final static Map<UUID,Boolean> PERM_OP = new HashMap<UUID,Boolean>();
	//PLAYERS a map that keeps track of all the players that have joined the server
	protected final static Map<String,List<String>> PLAYERS = new HashMap<String,List<String>>(); 
	//A player with the top admin permission has the most control
	protected final static Map<UUID,Boolean> TOP_ADMIN = new HashMap<UUID,Boolean>();
	//Messages a offline player will receive when he logs in
	protected final static Map<UUID,List<String>> MESSAGES = new HashMap<UUID,List<String>>();
	/* 
	 * If the world is on op mode (the value is true or null) players can issue any command (Unless a top admin has taken their permission to use that command) as a op,
	 * if not they can only issue commands that a top admin has granted them.
	 */
	protected final static Map<World,Boolean> COMMANDS = new HashMap<World,Boolean>(); 
	//
	protected final static Map<UUID,List<String>> PERMS = new HashMap<UUID,List<String>>(); 
	
	protected final static String NAME = "OpPermits";
	protected final static String ERROR_PREFIX =ChatColor.BLACK + "[" + ChatColor.UNDERLINE + "" + ChatColor.DARK_RED + "ERROR" + ChatColor.BLACK + "]" + ChatColor.WHITE + " ";
	protected final String PLUGIN_PREFIX = ChatColor.BLUE + "[" + ChatColor.GRAY + getName() + ChatColor.BLUE + "]" + ChatColor.WHITE + " ";
	protected final static String IMPORTANT_PREFIX = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "[IMPORTANT]" + ChatColor.WHITE + " ";
	protected final static String TOP_ADMIN_PREFIX = ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + "TOP_ADMIN" + ChatColor.DARK_RED + "]" + ChatColor.WHITE + " ";
	protected final static String PERM_OP_PREFIX = ChatColor.BLUE + "[" + ChatColor.GREEN + "PERM_OP" + ChatColor.BLUE + "]" + ChatColor.WHITE + " ";
	protected final static String OP_PREFIX = ChatColor.AQUA + "[" + ChatColor.WHITE + "OP" + ChatColor.AQUA + "]" + ChatColor.WHITE + " ";
	protected final String NO_PERMISSION = PLUGIN_PREFIX + "Sorry you " + ChatColor.LIGHT_PURPLE + "dont have permission to do this";
	
	protected DataManager data;
	
	@Override
	public void onEnable() { 
		//Plugin is being enabled (server is starting)
		this.data = new DataManager(this);
		this.getServer().getPluginManager().registerEvents(this, this);
		PERM_OP.put(null, null);
		PLAYERS.put(null, null);
		TOP_ADMIN.put(null, null);
		MESSAGES.put(null, null);
		loadMaps();
		this.getLogger().info(getName() + " Has been enabled");
	} 
	
	@Override
	public void onDisable() {
		//Plugin is being disabled (server is shuting down)
		saveMaps();
	}
	
	protected void loadMaps() {
		/* 
		Load all hashmaps
		 */
		if(data.getConfig().contains("perm_op")) {
			data.getConfig().getConfigurationSection("perm_op").getKeys(false).forEach(key -> {
				Boolean value = (Boolean) data.getConfig().get("perm_op." + key);
				PERM_OP.put(UUID.fromString(key), value);
			});
		}
		if(data.getConfig().contains("top_admin")) {
			data.getConfig().getConfigurationSection("top_admin").getKeys(false).forEach(key -> {
				Boolean value = (Boolean) (data.getConfig().get("top_admin." + key));
				TOP_ADMIN.put(UUID.fromString(key), value);
			});
		}
		if(data.getConfig().contains("players")) {
			data.getConfig().getConfigurationSection("players").getKeys(false).forEach(key -> {
				List<String> value = (List<String>) (data.getConfig().get("players." + key));
				PLAYERS.put(key, value);
			});
		}
		if(data.getConfig().contains("messages")) {
			data.getConfig().getConfigurationSection("messages").getKeys(false).forEach(key -> {
				List<String> value = (List<String>) (data.getConfig().get("messages." + key));
				MESSAGES.put(UUID.fromString(key), value);
			});
		}
	}
	
	protected void saveMaps() {
		/*
		Save all hashmaps
		 */
		if(!PERM_OP.isEmpty()) {
			for(Map.Entry<UUID, Boolean> entry : PERM_OP.entrySet()) {
				if(entry != null) {
					if(entry.getKey() != null && entry.getValue() != null)
						data.getConfig().set("perm_op." + entry.getKey(), entry.getValue());
				}
			}
		}
		if(!TOP_ADMIN.isEmpty()) {
			for(Map.Entry<UUID, Boolean> entry : TOP_ADMIN.entrySet()) {
				if(entry != null) {
					if(entry.getKey() != null && entry.getValue() != null)
						data.getConfig().set("top_admin." + entry.getKey(), entry.getValue());
				}
			}
		}
		if(!PLAYERS.isEmpty()) {
			for(Map.Entry<String, List<String>> entry : PLAYERS.entrySet()) {
				if(entry != null) {
					if(entry.getKey() != null && entry.getValue() != null) {
						data.getConfig().set("players." + entry.getKey(), (List<String>) entry.getValue());
					}
				}
			}
		}
		if(!MESSAGES.isEmpty()) {
			for(Map.Entry<UUID, List<String>> entry : MESSAGES.entrySet()) {
				if(entry != null) {
					if(entry.getKey() != null && entry.getValue() != null) {
						data.getConfig().set("messages." + entry.getKey(), (List<String>) entry.getValue());
					}
				}
			}
		}
		data.saveConfig();
	}
	
	protected List<String> getCommands(){
		//Returns a list of the plugins commands
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("op_permits");
		commands.add("op");
		commands.add("save");
		commands.add("load");
		commands.add("data");
		return commands;
	}
	
	protected boolean invert(Boolean b) {
		if(b == true) {
			return false;
		}else {
			return true;
		}
	}
	
	protected boolean invert(boolean b) {
		if(b == true) {
			return false;
		}else {
			return true;
		}
	}
	
	protected String enumerate(int i) {
		return ChatColor.WHITE + "" + i + ChatColor.DARK_BLUE + "." + ChatColor.WHITE + " ";
	}
	
	protected static OfflinePlayer getOfflinePlayer(String name) {
		OfflinePlayer[] offliners = Bukkit.getOfflinePlayers();
		for(OfflinePlayer offliner : offliners) {
			if(offliner.getName().equals(name)) return offliner;
		}
		return null;
		/*OfflinePlayer p = null;
		for(OfflinePlayer entry : Bukkit.getOfflinePlayers()) {
			Bukkit.getLogger().info(entry.getName() + " is next");
			if(entry.getName() == name) {
				return entry;
			}
		}
		return p;*/
		/*OfflinePlayer player = null;
		Iterator<UUID> it = getPlayers().iterator();
		while(it.hasNext()) {
			UUID next = it.next();
			OfflinePlayer p = Bukkit.getOfflinePlayer(next);
			if(p == null) return player;
			if(p.getName() == name) {
				player = p;
			}
		}
		return player;*/
	}
	
	protected List<String> getPlayers() {
		if(PLAYERS.get("PlayerList") == null) return new ArrayList<String>();
		return PLAYERS.get("PlayerList");
	}
	
	protected boolean hasJoined(Player player) {
		if(PLAYERS.get("PlayerList") == null) return false;
		if(PLAYERS.get("PlayerList").contains(player.getUniqueId().toString())) {
			return true;
		}else {
			return false;
		}
	}
	
	protected void setPermOp(Player player,Boolean op) {
		PERM_OP.put(player.getUniqueId(), op);
		player.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "You are now permanent OP = " + op);
	}
	
	protected void setPermOp(OfflinePlayer player,Boolean op) {
		PERM_OP.put(player.getUniqueId(), op);
		sendMessage(player,PLUGIN_PREFIX + ChatColor.GREEN + "You are now permanent OP = " + op);
	}
	
	protected boolean isPermOp(Player player) {
		boolean b = false;
		if(PERM_OP.get(player.getUniqueId()) == null) return b;
		
		if(PERM_OP.get(player.getUniqueId()) == true) b = true;
		
		return b;
	}
	
	protected boolean isPermOp(OfflinePlayer player) {
		boolean b = false;
		if(PERM_OP.get(player.getUniqueId()) == null) return b;
		
		if(PERM_OP.get(player.getUniqueId()) == true) b = true;
		
		return b;
	}
	
	protected boolean isTaken() {
		boolean b = false;
		for (Map.Entry<UUID, Boolean> entry : TOP_ADMIN.entrySet()) {
			if(entry != null) {
				UUID key = entry.getKey();
				if(key != null) {
					if(isTopAdmin(key)) {
						b = true;
					}
				}
			}
		}
		return b;
	}
	
	protected boolean isTopAdmin(UUID player) {
		boolean b = false;
		if(TOP_ADMIN.get(player) != null) {
			if(TOP_ADMIN.get(player) == true) {
				b = true;
			}
		}
		return b;
	}
	
	protected boolean isTopAdmin(Player player) {
		boolean b = false;
		if(TOP_ADMIN.get(player.getUniqueId()) != null) {
			if(TOP_ADMIN.get(player.getUniqueId()) == true) {
				b = true;
			}
		}
		return b;
	}
	
	protected boolean isTopAdmin(OfflinePlayer player) {
		boolean b = false;
		if(TOP_ADMIN.get(player.getUniqueId()) != null) {
			if(TOP_ADMIN.get(player.getUniqueId()) == true) {
				b = true;
			}
		}
		return b;
	}
	
	protected void setTopAdmin(Player player,Boolean top) {
		TOP_ADMIN.put(player.getUniqueId(), top);
		player.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "You are now top admin = " + top);
	}
	
	protected void setTopAdmin(OfflinePlayer player,Boolean top) {
		TOP_ADMIN.put(player.getUniqueId(), top);
		sendMessage(player,PLUGIN_PREFIX + ChatColor.GREEN + "You are now top admin = " + top);
	}
	
	protected List<String> getMessages(Player player) {
		List<String> list = new ArrayList<String>();
		list = MESSAGES.get(player.getUniqueId());
		if(list == null) return new ArrayList<String>();
		return list;
	}
	
	protected List<String> getMessages(OfflinePlayer player) {
		List<String> list = new ArrayList<String>();
		list = MESSAGES.get(player.getUniqueId());
		if(list == null) return new ArrayList<String>();
		return list;
	}

	protected void sendMessages(Player player) {
		Iterator<String> list = getMessages(player).iterator();
		while(list.hasNext()) {
			player.sendMessage(list.next());
		}
	}
	
	protected void sendMessage(OfflinePlayer player,String message) {
		List<String> list = getMessages(player);
		list.add(message);
		MESSAGES.put(player.getUniqueId(), list);
	}
	
	protected void say(Player player,String message) {
		String name = player.getName();
		Bukkit.getOnlinePlayers().forEach(p ->{
			if(isTopAdmin(player)) {
				p.sendMessage(TOP_ADMIN_PREFIX + "<" + name + ">" + message);
				return;
			}
			if(isPermOp(player)) {
				p.sendMessage(PERM_OP_PREFIX + "<" + name + ">" + message);
				return;
			}
			if(player.isOp()) {
				p.sendMessage(OP_PREFIX + "<" + name + ">" + message);
				return;
			}
			p.sendMessage("<" + name + ">" + message);
		});
	}
	
	protected static boolean isOpMode(World world) {
		if(COMMANDS.get(world) == null) {
			return true;
		}else if(COMMANDS.get(world) == true) {
			return true;
		}else if(COMMANDS.get(world) == false) {
			return false;
		}
		return true;
	}
	
	protected static List<String> getCommands(Player player) {
		if(PERMS.get(player.getUniqueId()) == null) return new ArrayList<String>();
		return PERMS.get(player.getUniqueId());
	}
	
	protected static List<String> getCommands(UUID player) {
		if(PERMS.get(player) == null) return new ArrayList<String>();
		return PERMS.get(player);
	}
	
	protected static List<String> getCommands(String player) {
		if(PERMS.get(getOfflinePlayer(player).getUniqueId()) == null) return new ArrayList<String>();
		return PERMS.get(getOfflinePlayer(player).getUniqueId());
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
		if(!(sender instanceof Player)) return true;
		Player p = (Player) sender;
		World w = p.getWorld();
		int length = args.length;
		//Switch on the label e.g /help 1 the label is help
		switch(label) {
		case "command":
			if(isTopAdmin(p)) {
				switch(length) {
				case 1:
					List<String> list = getCommands(args[0]);
					int count = 0;
					Iterator<String> it = list.iterator();
					p.sendMessage(args[0] + " has the following commands :");
					while(it.hasNext()) {
						count++;
						p.sendMessage(enumerate(count) + it.next());
					}
					break;
				case 2:
					List<String> list2 = getCommands(args[0]);
					list2.add(args[1]);
					PERMS.put(getOfflinePlayer(args[0]).getUniqueId(), list2);
					p.sendMessage(getOfflinePlayer(args[0]).getName() + " now has the following commands :");
					Iterator<String> it2 = list2.iterator();
					int count2 = 0;
					while(it2.hasNext()) {
						count2++;
						p.sendMessage(enumerate(count2) + it2.next());
					}
					break;
					default:
						//zero whether
						p.sendMessage("Do " + ChatColor.LIGHT_PURPLE + "/command <PlayerName>" + ChatColor.WHITE + " to get that player's list of commands" + "\n" +  "Do " + ChatColor.LIGHT_PURPLE + "/command <PlayerName> <Command>" + ChatColor.WHITE + " to give/take a command to/from a player");
						break;
				}
			}else {
				p.sendMessage(NO_PERMISSION);
			}
			return true;
		case "data":
			/*
			 * Give the player information about the hash maps
			 */
			if(!p.isOp()) {
				p.sendMessage(NO_PERMISSION); 
				break;
			}
			p.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "Looking for data...");
			for(Map.Entry<UUID, Boolean> entry : TOP_ADMIN.entrySet()) {
				if(entry != null) {
					UUID key = entry.getKey();
					if(key == null || entry.getValue() == null) {
						p.sendMessage("There are no top admin's");
					}else {
						if(Bukkit.getPlayer(key) != null) {
							p.sendMessage(PLUGIN_PREFIX + Bukkit.getPlayer(key).getName() + " is a top admin : " + entry.getValue());
						}else {
							if(Bukkit.getOfflinePlayer(key) != null) {
								p.sendMessage(PLUGIN_PREFIX + Bukkit.getOfflinePlayer(key).getName() + " is a top admin : " + entry.getValue());
							}
						}
					}
				}
			}
			for(Map.Entry<UUID, Boolean> entry : PERM_OP.entrySet()) {
				if(entry != null) {
					UUID key = entry.getKey();
					if(key == null || entry.getValue() == null) {
						p.sendMessage("There are no perm op's");
					}else {
						if(Bukkit.getPlayer(key) != null) {
							p.sendMessage(PLUGIN_PREFIX + Bukkit.getPlayer(key).getName() + " is a perm op : " + entry.getValue());
						}else {
							if(Bukkit.getOfflinePlayer(key) != null) {
								p.sendMessage(PLUGIN_PREFIX + Bukkit.getOfflinePlayer(key).getName() + " is a perm op : " + entry.getValue());
							}
						}
					}
				}
			}
			for(Map.Entry<UUID, List<String>> entry : MESSAGES.entrySet()) {
				if(entry != null) {
					UUID key = entry.getKey();
					if(key == null || entry.getValue() == null) {
						p.sendMessage("There are no messages for any players");
					}else {
						if(Bukkit.getPlayer(key) != null) {
							p.sendMessage(PLUGIN_PREFIX + Bukkit.getPlayer(key).getName() + " has the following messages : " + entry.getValue());
						}else {
							if(Bukkit.getOfflinePlayer(key) != null) {
								p.sendMessage(PLUGIN_PREFIX + Bukkit.getOfflinePlayer(key).getName() + " has the following messages : " + entry.getValue());
							}
						}
					}
				}
			}
			if(getPlayers().isEmpty()) {p.sendMessage("No one has joined yet");break;}
			int count = 0;
			Iterator<String> ite = getPlayers().iterator();
			p.sendMessage(PLUGIN_PREFIX + "Players that have joined :");
			while(ite.hasNext()) {
				UUID next = UUID.fromString(ite.next());
				count += 1;
				if(Bukkit.getPlayer(next) != null) {
					p.sendMessage(enumerate(count) + Bukkit.getPlayer(next).getName());
				}else {
					if(Bukkit.getOfflinePlayer(next) != null) {
						p.sendMessage(enumerate(count) + Bukkit.getOfflinePlayer(next).getName());
					}
				}
			}
			return true;
		case "load":
			if(!p.isOp()) {
				p.sendMessage(NO_PERMISSION);
				break;
			}
			if(length >= 1) {

			}else {
				loadMaps();
				p.sendMessage(ChatColor.GREEN + "Maps loaded from " + getName() + "/" + data.YML_FILE);
			} 
			return true;
		case "op_mode":
			switch(length) {
			case 0:
				//Change op mode for the world the player is in
				COMMANDS.put(w, invert(isOpMode(p.getWorld())));
				p.sendMessage(p.getWorld().getName() + "'s" + ChatColor.AQUA + " op mode is now = " + isOpMode(p.getWorld()));
				break;
			case 1:
				//Change the op mode for a world
				COMMANDS.put(Bukkit.getWorld(args[0]), invert(isOpMode(Bukkit.getWorld(args[0]))));
				p.sendMessage(args[0] + "'s " + ChatColor.AQUA + "op mode is now = " + isOpMode(Bukkit.getWorld(args[0])));
				break;
				default:
					p.sendMessage(PLUGIN_PREFIX + "Do " + ChatColor.LIGHT_PURPLE + "/command" + ChatColor.WHITE + " to change this world's op mode "
						+ ChatColor.LIGHT_PURPLE + "(Op mode determines whether op's can use any command they want)"
						+ "\n" + ChatColor.WHITE + "Do " + ChatColor.LIGHT_PURPLE + "/command <WorldName>" + ChatColor.WHITE + 
						" to change the op mode of a specific world");
					break;
			}
			return true;
		case "op_permits":
			//Switch on the amount of command arguments e.g /help 2 has 1 argument
			switch(length) {
			case 1:
				Iterator<String> it = getCommands().iterator();
				//Switch on the first argument 
				switch(args[0]) {
				case "help":
					//Show the player all the available commands
					int count2 = 0;
					p.sendMessage(PLUGIN_PREFIX + "Commands : ");
					while(it.hasNext()) {
						String next = it.next();
						//The variable count keeps track of the amount of commands we have iterated through
						count2 += 1;
						//If count < than the amount of Commands we append "," to the end of the sentence
						if(count2 < getCommands().size()) {
							p.sendMessage(enumerate(count2) + next + ",");
						}else {
							p.sendMessage(enumerate(count2) + next + "");
						}
					}
					break;
				}
				break;
			}
			return true;
		case "save":
			if(!p.isOp()) {
				p.sendMessage(NO_PERMISSION);
				break;
			}
			if(length >= 1) {

			}else {
				saveMaps();
				p.sendMessage(PLUGIN_PREFIX + "Data saved in " + getName() + "/" + data.YML_FILE);
			}
			return true;
		default:
			return true;
		}
		return true;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!hasJoined(p)) {
			//The player is playing for the first time so we add him to the list
			e.setJoinMessage(ChatColor.LIGHT_PURPLE + "" + p.getName() + "" + ChatColor.YELLOW + " joined for the first time!");
			Bukkit.getLogger().info(PLUGIN_PREFIX + IMPORTANT_PREFIX + p.getName() + " joined for the first time!");
			List<String> players = getPlayers();
			players.add(p.getUniqueId().toString());
			PLAYERS.put("PlayerList", players);
			return;
		}
		sendMessages(p);
	}
	
	@EventHandler 
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		e.setCancelled(true);
		say(p," " + e.getMessage());
	}
	 
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String name = p.getName();
		String cmd = e.getMessage().substring(1);
		String[] args = cmd.split(" ");
		String message = args[0];
		if(message.startsWith(getName().toLowerCase() + ":")) {
			String suffix = message.substring(getName().length() + 1);
			Bukkit.dispatchCommand(p, suffix);
		}
		switch(message) {
		case "op": 
			switch(args.length) {
			case 3:
				switch(args[2]) { 
				case "perm":
					if(isTopAdmin(p)) { 
						Player t = Bukkit.getPlayer(args[1]);
						if(t != null) {
							//This takes the players perm OP if he's a perm OP, and gives him perm OP if he's not already.
							setPermOp(t,invert(isPermOp(t)));
							p.sendMessage(t.getName() + " Is now a perm OP = " + isPermOp(t));
						}else {
							OfflinePlayer target = getOfflinePlayer(args[1]);
							if(target != null) {
								//This takes the players perm OP if he's a perm OP, and gives him perm OP if he's not already.
								setPermOp(target,invert(isPermOp(target)));
								p.sendMessage(target.getName() + " Is now a perm OP = " + isPermOp(target));
							}
						}
					} 
					break;
				case "top":
					if(p.isOp()) {
						if(!isTaken()) {
							setTopAdmin(p,true);
							break;
						}
					}
					if(isTopAdmin(p)) { 
						Player t = Bukkit.getPlayer(args[1]);
						if(t != null) {
							//This takes the players perm OP if he's a perm OP, and gives him perm OP if he's not already.
							setTopAdmin(t,invert(isTopAdmin(t)));
							p.sendMessage(t.getName() + " Is now a top admin = " + isTopAdmin(t));
						}else {
							OfflinePlayer target = getOfflinePlayer(args[1]);
							if(target != null) {
								//This takes the players perm OP if he's a perm OP, and gives him perm OP if he's not already.
								setTopAdmin(target,invert(isTopAdmin(target)));
								p.sendMessage(target.getName() + " Is now a top admin = " + isTopAdmin(target));
							}
						}
					}
					break;
				}
				break;
			case 1:
				if(isTopAdmin(p)) {
					setPermOp(p,true);
				}
				if(isPermOp(p)) {
					p.setOp(true);
					p.sendMessage(PLUGIN_PREFIX + " Your permanent OP enables you to op yourself!");
				}
				break;
			}
			break;
		case "msg":
			if(!p.isOp()) {
				p.sendMessage(NO_PERMISSION);
				break;
			}
			
			break;
			default:
				break;
		}
		if(!isOpMode(p.getWorld())) {
			if(PERMS.get(p.getUniqueId()) != null) {
				if(!PERMS.get(p.getUniqueId()).contains(message) && !isTopAdmin(p)) {
					/*
					 * Op mode is off,the player does not have permission to use the command,he is not a top admin,
					 * therefore we don't let him use the command
					 */
					p.sendMessage(NO_PERMISSION);
					e.setCancelled(true);
				}
			}
		}
	}
}
