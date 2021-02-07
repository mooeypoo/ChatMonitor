package io.github.mooeypoo.chatmonitor.configs;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ConfigManager {
    private Path dataFolder;
	private String prefix;
	private ConfigLoader<PluginConfigInterface> mainConfig = null;
	private HashMap<String, ConfigLoader<GroupConfigInterface>> configs = new HashMap<String, ConfigLoader<GroupConfigInterface>>();

	public ConfigManager(Path dataFolder, String prefix) throws ConfigurationException {
        this.dataFolder = dataFolder;
        this.prefix = prefix;
        this.reload();
    }

	public void reload() throws ConfigurationException {
		// Main config
		if (this.mainConfig == null) {
			this.mainConfig = ConfigLoader.create(this.dataFolder, "config.yml", PluginConfigInterface.class);
		}
		this.mainConfig.reloadConfig();
		
		// Group configs
		this.configs.clear();
		for (String groupName : this.mainConfig.getConfigData().groups()) {
			ConfigLoader<GroupConfigInterface> groupConfig = ConfigLoader.create(
				dataFolder,
				this.prefix + "_" + groupName + ".yml",
				GroupConfigInterface.class
			);
			// TODO: try/catch
			groupConfig.reloadConfig();
			this.configs.put(groupName, groupConfig);
		}
		
		// TODO: try/catch
		this.mainConfig.reloadConfig();
	}
	
	public ConfigLoader<PluginConfigInterface> getMainConfig() {
		return this.mainConfig;
	}

	public HashMap<String, ConfigLoader<GroupConfigInterface>> getGroupConfigs() {
		return this.configs;
	}
	
	public Set<String> getGroupNames() {
		return this.configs.keySet();
	}
	
	public GroupConfigInterface getGroupConfigData(String groupName) throws ConfigurationException {
		ConfigLoader<GroupConfigInterface> conf = this.configs.get(groupName);
		if (conf == null) {
			return null;
		}
		return conf.getConfigData();
	}
	
	public Set<String> getGroupWords(String groupName) throws ConfigurationException {
		GroupConfigInterface groupConfigData = this.getGroupConfigData(groupName);
		if (groupConfigData == null) {
			return Collections.<String>emptySet();
		}
		
		return groupConfigData.words();
	}
}
