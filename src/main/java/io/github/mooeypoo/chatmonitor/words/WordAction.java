package io.github.mooeypoo.chatmonitor.words;

import java.util.Set;

public class WordAction {
	private String matchedRule;
	private String originalWord;
	private Set<String> commands;
	private String message;
	private String group;
	private boolean preventSend;
	private boolean broadcast;
	
	public WordAction(
			String matchedRule,
			String originalWord,
			String message,
			boolean preventSend,
			boolean broadcast,
			Set<String> commands,
			String group
	) {
		this.matchedRule = matchedRule;
		this.originalWord = originalWord;
		this.message = message;
		this.commands = commands;
		this.preventSend = preventSend;
		this.broadcast = broadcast;
		this.group = group;
	}
	
	public String getMatchedRule() {
		return this.matchedRule;
	}
	
	public String getOriginalWord() {
		return this.originalWord;
	}
	
	public String getMessage() {
		return this.message;
	}

	public Set<String> getCommands() {
		return this.commands;
	}
	
	public String getGroup() {
		return this.group;
	}
	
	public boolean isPreventSend() {
		return this.preventSend;
	}
	
	public boolean isBroadcast() {
		return this.broadcast;
	}

	public boolean isEmpty() {
		return this.matchedRule == null;
	}
}
