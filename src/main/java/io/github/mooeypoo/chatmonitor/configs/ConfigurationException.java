package io.github.mooeypoo.chatmonitor.configs;

public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String filename;
	private final Exception exception;

	public ConfigurationException(String filename, String errorMessage) {
        super(errorMessage);
        
        this.filename = filename;
		this.exception = null;
    }

	public ConfigurationException(String filename, String errorMessage, Exception ex) {
        super(errorMessage);
        
        this.filename = filename;
        this.exception = ex;
    }
	
	public String getConfigFileName() {
		return this.filename;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		if (this.exception != null) {
			return this.exception.getStackTrace();
		}
		return new StackTraceElement[0];
	}
}
