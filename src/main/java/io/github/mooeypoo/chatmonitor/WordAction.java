package io.github.mooeypoo.chatmonitor;

import java.util.List;

public class WordAction {
	private String word;
	private List<String> commands;
	private String message;
	private String group;
	private Boolean preventSend;
	private Boolean broadcast;
	
	public WordAction(
			String word,
			String message,
			Boolean preventSend,
			Boolean broadcast,
			List<String> commands,
			String group
	) {
		this.word = word;
		this.message = message;
		this.commands = commands;
		this.preventSend = preventSend;
		this.broadcast = broadcast;
		this.group = group;
	}
	
	public String getWord() {
		return this.word;
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
		return this.word == null;
	}
}
