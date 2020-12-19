package io.github.mooeypoo.chatmonitor;
/*
* Copyright (C) 2012
*
* Permission is hereby granted, free of charge, to any person obtaining a copy 
* of this software and associated documentation files (the "Software"), to deal 
* in the Software without restriction, including without limitation the rights 
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
* copies of the Software, and to permit persons to whom the Software is 
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
* SOFTWARE.
*/
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigAccessor {
    private JavaPlugin plugin;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
	private String name;

    public ConfigAccessor(JavaPlugin plugin, String name, String prefix) {
        this.plugin = plugin;
        this.name = prefix + "_" + name + ".yml";
        this.reloadConfig();
    }

    public void reloadConfig() {
        if (this.customConfigFile == null) {
            this.customConfigFile = new File(this.plugin.getDataFolder(), this.name);
        }
        this.customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public FileConfiguration getConfig() {
        if (this.customConfig == null) {
            reloadConfig();
        }
        return this.customConfig;
    }

    public void saveConfig() {
        if (this.customConfig == null || this.customConfigFile == null) {
            return;
        }
        try {
        	this.customConfig.save(customConfigFile);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
}