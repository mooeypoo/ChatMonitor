package io.github.mooeypoo.chatmonitor;

import java.util.List;

public class WordAction {
	private String word;
	private List<String> commands;
	private String message;
	private String group;
	
	public WordAction(String word, String message, List<String> commands, String group) {
		this.word = word;
		this.message = message;
		this.commands = commands;
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

	public Boolean isEmpty() {
		return this.word == null;
	}
}
