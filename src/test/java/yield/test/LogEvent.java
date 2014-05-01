package yield.test;

public class LogEvent {
	String message;

	public LogEvent(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + message;
	}
}
