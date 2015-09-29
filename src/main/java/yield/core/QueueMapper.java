package yield.core;

import javax.annotation.Nonnull;

public abstract class QueueMapper<In, Out> extends BaseControlQueueProvider
		implements SourceProvider<Out>, EventListener<In> {
	@Override
	public void feed(In e) {
		getQueue().feed(map(e));
	}

	protected abstract Out map(In e);

	@Override
	@Nonnull
	public abstract EventQueue<Out> getQueue();

	@Override
	@Nonnull
	public EventType getInputType() {
		return EventType.ALL;
	}
}
