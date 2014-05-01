package yield.test;

import yield.core.Aggregator;
import yield.core.EventQueue;
import yield.core.Filter;
import yield.core.Producer;
import yield.core.Query;
import yield.core.Window;
import yield.core.windows.FixedSize;
import yield.output.Printer;

public class Test {

	public static void main(String[] args) {
		EventQueue<LogEvent> inOne = new EventQueue<>();
		inOne.feed(new LogEvent("holla"));
		inOne.bind(new Printer<LogEvent>("p1"));
		inOne.bind(new Printer<LogEvent>("p2"));
		inOne.feed(new LogEvent("holla2"));

		Filter<LogEvent> filter = new Filter<LogEvent>() {

			@Override
			protected boolean matches(LogEvent e) {
				return e.toString().contains("a");
			}

		};
		EventQueue<LogEvent> filtered = filter.getQueue();
		inOne.bind(filter);
		filtered.bind(new Printer<LogEvent>("filtered"));

		Producer<Window<LogEvent>> twoMostRecentProducer = new Producer<Window<LogEvent>>() {
			@Override
			public Window<LogEvent> make() {
				return new FixedSize<>(2);
			}

		};
		Window<LogEvent> twoMostRecent = twoMostRecentProducer.make();
		filtered.bind(twoMostRecent);
		Producer<Aggregator<LogEvent, LogEvent>> concatProducer = new Producer<Aggregator<LogEvent, LogEvent>>() {

			@Override
			public Aggregator<LogEvent, LogEvent> make() {
				return new Aggregator<LogEvent, LogEvent>() {

					@Override
					protected void aggregate(Iterable<LogEvent> events) {
						StringBuilder compound = new StringBuilder();
						for (LogEvent e : events) {
							compound.append(e.toString());
						}
						queue.feed(new LogEvent(compound.toString()));
					}
				};
			}
		};
		Aggregator<LogEvent, LogEvent> agg = concatProducer.make();
		twoMostRecent.bind(agg);
		agg.getQueue().bind(new Printer<LogEvent>("agg"));

		inOne.feed(new LogEvent("out"));
		inOne.feed(new LogEvent("aout"));
		inOne.feed(new LogEvent("out"));
		inOne.feed(new LogEvent("sdsfaout"));
		inOne.feed(new LogEvent("aggtest"));

		Counter<LogEvent> counter = new Counter<LogEvent>();
		new Query<>(counter).filter(new Filter<LogEvent>() {

			@Override
			protected boolean matches(LogEvent e) {
				return e.message.length() < 3;
			}
		}).within(twoMostRecentProducer, concatProducer).getQueue()
				.bind(new Printer<LogEvent>("query result A: "));
		new Query<>(counter).filter(new Filter<LogEvent>() {

			@Override
			protected boolean matches(LogEvent e) {
				return e.message.length() > 3;
			}
		}).within(twoMostRecentProducer, concatProducer).getQueue()
				.bind(new Printer<LogEvent>("query result B: "));
		counter.run(10, new Counter.Producer<yield.test.LogEvent>() {

			@Override
			public LogEvent produce(int value) {
				StringBuilder compound = new StringBuilder();
				for (int i = 0; i < value; i++) {
					compound.append(i);
				}
				return new LogEvent(compound.toString());
			}
		});
	}
}
