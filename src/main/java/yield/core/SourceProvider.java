package yield.core;

public interface SourceProvider<LogEvent> {
	Yielder<LogEvent> getQueue();
}
