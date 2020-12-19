package io.github.mooeypoo.chatmonitor;

import java.util.regex.Pattern;
import java.util.List;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Listener;

public class ChatMonitor extends JavaPlugin implements Listener {

	@Override
    public void onEnable() {
		// Get config
		saveDefaultConfig();
		updateDefaults();
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, (this));
        // TODO Insert logic to be performed when the plugin is enabled
		getLogger().info("ChatMonitor is enabled.");
    }
    
    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
		getLogger().info("ChatMonitor is disabled.");
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
    	String firstMatch = null;

    	Player p = event.getPlayer();
    	String msgFromPlayer = event.getMessage();
    	if (p.hasPermission("chatmonitor.ignore")) {
    		// Skip if the user's permission allows ignoring what they say
    		return;
    	}
    	
    	// If we found a match for a bad word, deal with it:
    	firstMatch = matchBadWords(msgFromPlayer);
    	if (firstMatch != null) {
        	event.setCancelled(true);
        	p.sendMessage(String.format(getConfig().getString("message"), firstMatch));
    		getLogger().warning(
    			"**SWEAR JAR: "+ firstMatch +"** MESSAGE PREVENTED. ORIGINAL: [" +
    				p.getDisplayName() + "] " + msgFromPlayer
    		);
    		runCommands(p.getName(), firstMatch);	
    	}
    }

    // TODO: Add support for commands, like /tell
//    @Override
//    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//    	if (sender.hasPermission("chatmonitor.ignore")) {
//    		return true;
//    	}
//
//    	if (cmd.getName().equalsIgnoreCase("tell")) {
//    		String msg = String.join(" ", args);
//    		getLogger().info("debugging: tell cmd message" + msg);
//    		String firstMatch = matchBadWords(msg);
//        	if (firstMatch != null) {
//            	sender.sendMessage(String.format(getConfig().getString("message"), firstMatch));
//        		getLogger().warning(
//      				"**SWEAR JAR: "+ firstMatch +"** MESSAGE PREVENTED. ORIGINAL: [" + 
//      					sender.getName() + 
//        				"] /" + cmd.getName() + " " + msg
//        		);
//    			runCommands(sender.getName(), firstMatch);
//        		return false;
//        	}
//    	}
//    	return true;
//    }
    
    public void updateDefaults() {
    	getConfig().addDefault("message", "Bad word intercepted (%s). Message stopped.");
        getConfig().options().copyDefaults();
        saveConfig();
    }
    
    private void runCommands(String playerName, String wordCaught) {
    	List<String> cmds = getConfig().getStringList("runcommands");
//    	if (cmds.isEmpty()) {
//    		getLogger().info("No commands to invoke. Skipping.");
//    		return;
//    	}
    	for (String c : cmds){
    		// Replace magic words:
    		String replacedCommand = c.replace("%player%", playerName);
    		replacedCommand = replacedCommand.replace("%word%", wordCaught);
    		final String runnableCommand = replacedCommand;

    		// Log and execute:
    		getLogger().info("Invoking command: " + runnableCommand);
    		
    		getServer().getScheduler().callSyncMethod(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand));    		
//    		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand);
    	}
    }

    private String matchBadWords(String str) {
    	Matcher matcher;
    	String firstMatch = null;

    	List<String> badwords = getConfig().getStringList("badwords");
		// Check if the word is a bad word
		for (String word : badwords){
    		Pattern pattern = Pattern.compile(word);
    		matcher = pattern.matcher(str);
    		if (matcher.find()) {
    			firstMatch = matcher.group();
    			break;
    		}
    	}
		
		return firstMatch;
    }

}
