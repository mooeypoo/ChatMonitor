package io.github.mooeypoo.chatmonitor;

import java.util.List;

public class WordAction {
	private String matchedRule;
	private String originalWord;
	private List<String> commands;
	private String message;
	private String group;
	private Boolean preventSend;
	private Boolean broadcast;
	
	public WordAction(
			String matchedRule,
			String originalWord,
			String message,
			Boolean preventSend,
			Boolean broadcast,
			List<String> commands,
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

	public List<String> getCommands() {
		return this.commands;
	}
	
	public String getGroup() {
		return this.group;
	}
	
	public Boolean isPreventSend() {
		return this.preventSend;
	}
	
	public Boolean isBroadcast() {
		return this.broadcast;
	}

	public Boolean isEmpty() {
		return this.matchedRule == null;
	}
}
