package yield.filter.combinator;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventQueue;
import yield.core.SourceProvider;

/**
 * Merges multiple events into their concatenation. Subsequent events are merged
 * as long as the provided regular expression holds.
 */
public class RegExCombinator extends BaseControlQueueProvider implements
		SourceProvider<String>, EventListener<String> {
	/**
	 * Merger.
	 */
	private StringBuilder collection = null;
	private Pattern p;
	@Nonnull
	private final EventQueue<String> queue = new EventQueue<>();

	/**
	 * Further events to be merged are awaited to a maximum of {@link #maxIdle}
	 * millis. That means an event is delayed for {@code #maxIdle} millis after
	 * its last component has been received unless another (unrelated) event
	 * arrives.
	 */
	private int maxIdle = 500;
	private Timer t = new Timer(true);
	private TimerTask publisher;

	/**
	 * @param mergeCriterion
	 *            Regular expression. When an event matches it, it gets merged
	 *            with the previous event (possibly merger).
	 */
	public RegExCombinator(String mergeCriterion) {
		p = Pattern.compile(mergeCriterion, Pattern.DOTALL
				| Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
	}

	@Override
	public void feed(String e) {
		// Reset timer to limit merging.
		if (publisher != null) {
			publisher.cancel();
		}

		if (collection != null && p.matcher(e).matches()) {
			// Merge.
			collection.append(System.lineSeparator());
			collection.append(e);
		} else {
			// Yield former merger and keep event as merge candidate.
			if (collection != null) {
				queue.feed(collection.toString());
			}
			collection = new StringBuilder(e);
		}

		// Set timer to publish merger as event in case no more events come in
		// in due future.
		publisher = new TimerTask() {
			@Override
			public void run() {
				queue.feed(collection.toString());
				collection = new StringBuilder();
			}
		};
		t.schedule(publisher, maxIdle);
	}

	/**
	 * @return Merged events.
	 */
	@Override
	@Nonnull
	public EventQueue<String> getQueue() {
		return queue;
	}
}
