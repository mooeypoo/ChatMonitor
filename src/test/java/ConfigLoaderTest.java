import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import io.github.mooeypoo.chatmonitor.ConfigLoader;
import io.github.mooeypoo.chatmonitor.ConfigurationException;
import io.github.mooeypoo.chatmonitor.GroupConfigInterface;
import io.github.mooeypoo.chatmonitor.PluginConfigInterface;

public class ConfigLoaderTest {
	@After
	public void tearDown() throws IOException {
		// Remove temp folder
		String path = Paths.get("src", "test", "resources", "temp").normalize().toString();
		System.out.println("Deleting contents of folder '" + path + "'");
		FileUtils.cleanDirectory(new File(path));
	}

	@Test
	public void testMalformedYaml() throws IOException {
		// Mock filesystem
		String fileName = "malformed.yml";
		Path path = Paths.get("src", "test", "resources", "temp", fileName);

		this.makeFileWithContent(path, "name: foo: bar: baz");
		ConfigLoader<PluginConfigInterface> config = ConfigLoader.create(
				Paths.get("src", "test", "resources", "temp"),
				fileName, PluginConfigInterface.class
		);
		
		try {
			config.reloadConfig();
			fail("Expected a ConfigurationException to be thrown");
		} catch (ConfigurationException e) {
			assertEquals(e.getConfigFileName(), "malformed.yml");
			assertEquals(e.getMessage(), "The yaml syntax of this file is malformed. Using defaults, instead.");
		}
		
		this.deleteFile(path);
	}

	@Test
	public void testBadConfigurationKeyvals() throws IOException {
		// Mock filesystem
		String fileName = "badconfig.yml";
		Path path = Paths.get("src", "test", "resources", "temp", fileName);

		this.makeFileWithContent(path, "preventsend: stringy");
		ConfigLoader<GroupConfigInterface> config = ConfigLoader.create(
				Paths.get("src", "test", "resources", "temp"),
				fileName, GroupConfigInterface.class
		);
		
		try {
			config.reloadConfig();
			fail("Expected a ConfigurationException to be thrown");
		} catch (ConfigurationException e) {
			assertEquals(e.getConfigFileName(), "badconfig.yml");
			assertEquals(e.getMessage(), "The keys and values used in this file are malformed. Using defaults, instead.");
		}
		
		this.deleteFile(path);
	}

	private void makeFileWithContent(Path path, String content) throws IOException {
		File newFile = null;
		try {
	    	String fullPath = path.normalize().toString();
	    	System.out.println("PATH --> " + fullPath);
	        newFile = new File(fullPath);
	        if (newFile.createNewFile()) {
	          System.out.println("\n>> File created: " + newFile.getName());
	        } else {
	          System.out.println("\n>> File already exists.");
	        }
		} catch (IOException e) {
		    System.out.println("An error occurred.");
		    e.printStackTrace();
		}
		if (newFile != null) {
	        FileWriter writer = new FileWriter(newFile);
	        writer.write(content);
	        writer.close();
		}
	}
	
	private void deleteFile(Path path) {
		String fullPath = path.normalize().toString();
    	System.out.println("DELETE PATH --> " + fullPath);
        File newFile = new File(fullPath);
        
        newFile.delete();
	}
}
