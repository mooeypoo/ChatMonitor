package io.github.mooeypoo.chatmonitor;

import java.util.Set;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;
import space.arim.dazzleconf.annote.ConfDefault.DefaultStrings;

public interface PluginConfigInterface {
	/*
	 * Config file fileds
	 * ---
	 * defaultmessage: String
	 * groups: List<String>
	 */
	@ConfKey("defaultmessage")
	@ConfComments("Default message to display to the user (or broadcast in general chat) if a word in any of the group lists matches user text. This message will be used if any of the groups uses an empty message.")
	@DefaultString("This word (\"%word%\") is not allowed on this server.")
	String defaultMessage();

	@ConfKey("groups")
	@ConfComments("A list of word-groups to use. Each group requires a separate config file; the config files will be auto-generated on first-run of this list if they don't already exist, and can be then edited to add specific behavior and a list of match words.")
	@DefaultStrings({"list"})
	Set<String> groups();
}
