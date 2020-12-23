package io.github.mooeypoo.chatmonitor;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
    	WordAction action = this.wordmanager.processAllWords(msgFromPlayer);
    	
    	if (!this.processResponse(action, p, msgFromPlayer)) {
	    	event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
    	if (event.getPlayer().hasPermission("chatmonitor.ignore")) {
    		return;
    	}
    	// split up to get the elements of the command
    	String[] cmdElements = event.getMessage().split(" ");
    	String cmdName = cmdElements[0].substring(1);
    	
    	// See if we should even look at this for the command
    	if (!this.wordmanager.getRelevantCommands().contains(cmdName)) {
    		return;
    	}

    	WordAction action = this.wordmanager.processWordsInCommand(cmdName, event.getMessage());
    	
    	if (!this.processResponse(action, event.getPlayer(), event.getMessage())) {
	    	event.setCancelled(true);
    	}
    }
    
    /**
     * Process the resulting action given to send the player a message, process
     * the log message and activate commands.
     *
     * @param action The resulting WordAction object representing the details of
     *               the word that matched.
     * @param player The player triggering the text or command
     * @param msg    The message sent by the user
     * @return       Whether or not the event should continue to process or be aborted
     */
    public Boolean processResponse(WordAction action, Player player, String msg) {
    	if (action == null || action.isEmpty()) {
    		return true;
    	}
    	
    	Boolean shouldAllowEvent = !action.isPreventSend();
		String response = action.getMessage().replace("%player%", player.getName());
		response = response.replace("%word%", action.getWord());
		ArrayList<String> logMessage = new ArrayList<String>();
		logMessage.add("*WORD MATCH: "+ action.getWord() +"[type: " + action.getGroup() + "]*");
		if (action.isPreventSend()) {
			logMessage.add("MESSAGE MUTED.");
			logMessage.add("\n-> Attempted message: " + player.getName() + "> " + msg);
			shouldAllowEvent = false;
		}
		if (!response.isEmpty()) {
			if (action.isBroadcast()) {
				Bukkit.broadcastMessage(ChatColor.RED + response);
				logMessage.add("\n-> Broadcast message> " + response);
			} else {
				player.sendMessage(ChatColor.RED + response);
				logMessage.add("\n-> Sent to user> " + response);
			}
		}
		getLogger().warning(String.join(" ",logMessage));
		this.runCommands(player, action);
		return shouldAllowEvent;
    }

    /**
     * Update the main config defaults.
     */
    public void updateDefaults() {
    	getConfig().addDefault("defaultmessage", "Bad word intercepted (%s). Message stopped.");
        getConfig().options().copyDefaults();
        saveConfig();
    }
    
    /**
     * Based on the word that matched, see if the group requires followup actions
     * and trigger them on a deferred thread.
     *
     * @param playerName The name of the player
     * @param action The details of the matched word
     */
    private void runCommands(Player player, WordAction action) {
    	for (String cmd : action.getCommands()) {
    		// Replace magic words:
    		String replacedCommand = cmd.replace("%player%", player.getName());
    		replacedCommand = replacedCommand.replace("%word%", action.getWord());
    		final String runnableCommand = replacedCommand;

    		// Log and execute:
    		getLogger().info("Invoking command: " + runnableCommand);
    		try {
	    		getServer().getScheduler().callSyncMethod(this, new Callable<Boolean>() {
	    			public Boolean call() {
	        			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand);
	        			return false;
	    			}
	    		}).get();
    		} catch (ExecutionException e) {
    			getLogger().warning("ExecutionException for command \"" + cmd + "\"");
    		} catch (InterruptedException e) {
    			getLogger().warning("InterruptedException for command \"" + cmd + "\"");
    		}
//    		getServer().getScheduler().callSyncMethod(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand));    		
    	}
    }
}
