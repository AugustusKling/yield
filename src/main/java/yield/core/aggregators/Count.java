package yield.core.aggregators;

import java.util.Iterator;

import yield.core.Aggregator;

/**
 * Counts number of events.
 * 
 * @param <In>
 *            Any type.
 */
public class Count<In> extends Aggregator<In, Integer> {

	@Override
	protected void aggregate(Iterable<In> events) {
		int count = 0;
		Iterator<In> iterator = events.iterator();
		while (iterator.hasNext()) {
			count = count + 1;
			iterator.next();
		}
		getQueue().feed(count);
	}

}
