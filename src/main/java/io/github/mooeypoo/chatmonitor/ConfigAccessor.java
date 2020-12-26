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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        if (!customConfigFile.exists()) {
        	// Copy the defaults from the internal file default_words_config.yml
        	this.copyIntoFile(this.customConfigFile);
        }

        this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
    }
    
    private Boolean copyIntoFile(File outputFile) {
    	try {
    	    InputStream source = this.plugin.getResource("default_words_config.yml");
    	    if (source == null) {
    	    	return false;
    	    }

    	    OutputStream target = new FileOutputStream(outputFile);
    		this.plugin.getLogger().info("Initializing config from local resource 'default_words_config.yml' into '" + this.name + "'");
    	    byte[] buffer = new byte[8 * 1024];
    	    int bytesRead;
    	    while ((bytesRead = source.read(buffer)) != -1) {
    	    	target.write(buffer, 0, bytesRead);
    	    }
    	    target.close();
    	    return true;
    	} catch (IOException e) {
    		this.plugin.getLogger().info("IOException for config '" + this.name + "'. Using default fallback.");
    		return false;
    	}
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