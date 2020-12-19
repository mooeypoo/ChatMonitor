package io.github.mooeypoo.chatmonitor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Listener;

public class ChatMonitor extends JavaPlugin implements Listener {
	private WordManager wordmanager; 

	@Override
    public void onEnable() {
		// Get config
		saveDefaultConfig();
		updateDefaults();
		
		// Initialize word list
		getLogger().info("Initializing word lists...");
		this.wordmanager = new WordManager(this, getConfig());

		// Connect events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, (this));
        // TODO Insert logic to be performed when the plugin is enabled
		getLogger().info("ChatMonitor is enabled.");
    }
    
    @Override
    public void onDisable() {
		getLogger().info("ChatMonitor is disabled.");
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
    	Player p = event.getPlayer();
    	if (p.hasPermission("chatmonitor.ignore")) {
    		// Skip if the user's permission allows ignoring what they say
    		return;
    	}

    	String msgFromPlayer = event.getMessage();
    	WordAction action = this.wordmanager.process(msgFromPlayer);
    	
    	if (action == null || action.isEmpty()) {
    		return;
    	}

		String response = action.getMessage().replace("%player%", p.getName());
		response = response.replace("%word%", action.getWord());

    	event.setCancelled(true);
		p.sendMessage(ChatColor.RED + response);
		getLogger().warning(
		"**SWEAR JAR ("+ action.getWord() +")** [GROUP: " + action.getGroup() + "] MESSAGE PREVENTED. \n" +
				"-> MESSAGE SENT TO USER: " + response + "\n" +
				"-> ATTEMPTED USER MESSAGE: [" + p.getDisplayName() + "] " + msgFromPlayer
		);
		this.runCommands(p.getName(), action);
    }
    
    public void updateDefaults() {
    	getConfig().addDefault("defaultmessage", "Bad word intercepted (%s). Message stopped.");
        getConfig().options().copyDefaults();
        saveConfig();
    }
    
    private void runCommands(String playerName, WordAction action) {
    	for (String cmd : action.getCommands()) {
    		// Replace magic words:
    		String replacedCommand = cmd.replace("%player%", playerName);
    		replacedCommand = replacedCommand.replace("%word%", action.getWord());
    		final String runnableCommand = replacedCommand;

    		// Log and execute:
    		getLogger().info("Invoking command: " + runnableCommand);
    		getServer().getScheduler().callSyncMethod(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand));    		
    	}
    }
}
