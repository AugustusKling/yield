package yield.core.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventQueue;
import yield.core.SourceProvider;

/**
 * State machine base pattern match for events in succession.
 * 
 * States get automatically added when transitions are added.
 * 
 * @param <Event>
 *            Type of events.
 */
public class Pattern<Event> extends BaseControlQueueProvider implements
		EventListener<Event>, SourceProvider<String> {
	/**
	 * Output which publishes all state changes.
	 */
	private final EventQueue<String> stateChanges = new EventQueue<>();

	/**
	 * Possible transitions by current state.
	 */
	private final Map<String, List<Transition<Event>>> transitions = new HashMap<>();

	/**
	 * Current state.
	 */
	private String state;

	public Pattern(String initialState) {
		this.state = initialState;
	}

	/**
	 * All state changes.
	 */
	@Override
	public EventQueue<String> getQueue() {
		return stateChanges;
	}

	@Override
	public void feed(Event e) {
		for (Transition<Event> transition : getAllowedTransitions()) {
			if (transition.matches(e)) {
				this.state = transition.to;
				stateChanges.feed(this.state);
				break;
			}
		}
	}

	/**
	 * Adds transition and its states.
	 */
	public void addTransition(Transition<Event> transition) {
		List<Transition<Event>> fromSameState;
		if (transitions.containsKey(transition.from)) {
			fromSameState = transitions.get(transition.from);
		} else {
			fromSameState = new ArrayList<>();
			transitions.put(transition.from, fromSameState);
		}
		fromSameState.add(transition);
	}

	/**
	 * @return Possible transitions from current state.
	 */
	private List<Transition<Event>> getAllowedTransitions() {
		if (transitions.containsKey(state)) {
			return transitions.get(state);
		} else {
			return Collections.emptyList();
		}
	}
}
