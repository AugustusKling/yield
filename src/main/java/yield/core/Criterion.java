package yield.core;

public abstract class Criterion<LogEvent> {
	public abstract boolean matches(LogEvent e);
}
