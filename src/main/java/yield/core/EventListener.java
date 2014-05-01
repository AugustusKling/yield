package yield.core;

public interface EventListener<Event> {
	void feed(Event e);
}
