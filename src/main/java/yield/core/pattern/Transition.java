package yield.core.pattern;

import yield.core.Criterion;

/**
 * State transition for pattern recognition.
 */
public abstract class Transition<Event> extends Criterion<Event> {
	public final String from;
	public final String to;

	public Transition(String from, String to) {
		this.from = from;
		this.to = to;
	}
}
