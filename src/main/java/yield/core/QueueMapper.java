package yield.core;

import javax.annotation.Nonnull;

public abstract class QueueMapper<In, Out> implements SourceProvider<Out>,
		EventListener<In> {
	@Nonnull
	private final EventQueue<Out> queue = new EventQueue<Out>();

	@Override
	public void feed(In e) {
		queue.feed(map(e));
	}

	protected abstract Out map(In e);

	@Override
	@Nonnull
	public EventQueue<Out> getQueue() {
		return queue;
	}

}
