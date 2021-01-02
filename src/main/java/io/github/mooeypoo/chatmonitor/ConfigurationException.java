package io.github.mooeypoo.chatmonitor;

public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;
	private String filename = null;
	private Exception exception = null;

	public ConfigurationException(String filename, String errorMessage) {
        super(errorMessage);
        
        this.filename = filename;
    }

	public ConfigurationException(String filename, String errorMessage, Exception ex) {
        super(errorMessage);
        
        this.filename = filename;
        this.exception = ex;
    }
	
	public String getConfigFileName() {
		return this.filename;
	}
	
	public StackTraceElement[] getStackTrace() {
		if (this.exception != null) {
			return this.exception.getStackTrace();
		}
		return null;
	}
}
