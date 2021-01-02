package io.github.mooeypoo.chatmonitor;

import java.util.Set;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault.DefaultBoolean;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;
import space.arim.dazzleconf.annote.ConfDefault.DefaultStrings;
import space.arim.dazzleconf.annote.ConfKey;

public interface GroupConfigInterface {
	/*
	 * Config file fields
	 * ---
	 * message: String
	 * preventsend: Boolean
	 * broadcast: Boolean
	 * includecommands: List<String>
	 * words: List<string>
	 */
	
	@ConfKey("message")
	@ConfComments({
			"Message displayed to the user (or broadcast in general chat)",
			"if a word in the list matches user text.",
			"You can use magic words that will be replaced before the message",
			"is sent to the user:",
			"- %player% the player whose message triggered the match",
			"- %word% the word that was caught",
			"- %matchrule% the rule (in the list below) that was triggered on the matched word"
	})
	@DefaultString("This word (\"%word%\") is not allowed on this server.")
	String message();
	
	@ConfKey("preventsend")
	@ConfComments("Stop the user message from appearing in the main chat if there was a word match.")
	@DefaultBoolean(false)
	Boolean preventSend();
	
	@ConfKey("broadcast")
	@ConfComments("Send the response message to the entire chat. If false, the message is only visible to the user whose text matched the word from the list.")
	@DefaultBoolean(false)
	Boolean broadcast();
	
	@ConfKey("includecommands")
	@ConfComments("A list of commands the plugin should look into when searching for word matches. Do not include the slash (Example: - tell)")
	@DefaultStrings({"tell"})
	Set<String> includeCommands();

	@ConfKey("words")
	@ConfComments("A list of words to trigger the behavior of this group. This is a regular expression list. If words are added plainly, they will match anywhere in the string. Example: 'foo' will match 'food' and 'blafoo'. For comlpex behavior, like limiting word boundaries and alternative spellings, see regular expression documentation, and visit the ChatMonitor wiki for example lists.")
	@DefaultStrings({})
	Set<String> words();

	@ConfKey("runcommands")
	@ConfComments("A list of raw commands to execute after a word is matched.")
	@DefaultStrings({})
	Set<String> runCommands();
}






