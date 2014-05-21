package yield.core;

import javax.annotation.Nonnull;

/**
 * Queue that only forwards matching events.
 */
public abstract class Filter<LogEvent> extends BaseControlQueueProvider
		implements EventListener<LogEvent>, SourceProvider<LogEvent> {
	@Nonnull
	private final EventQueue<LogEvent> queue;

	public Filter() {
		this.queue = new EventQueue<LogEvent>();
	}

	@Override
	@Nonnull
	public EventQueue<LogEvent> getQueue() {
		return queue;
	}

	@Override
	public void feed(LogEvent e) {
		if (matches(e)) {
			queue.feed(e);
		}
	}

	/**
	 * Decides whether to forward or discard incoming event.
	 * 
	 * @param e
	 *            Incoming event.
	 * @return When {@code true} forward, when {@code false} discards.
	 */
	protected abstract boolean matches(LogEvent e);
}
