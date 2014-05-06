package yield.config;

import yield.core.Window;
import yield.core.windows.AgingOut;
import yield.core.windows.All;
import yield.core.windows.FixedSize;

/**
 * Base {@link FunctionConfig} for those functions which combine multiple events
 * into one.
 * 
 * @param <EventType>
 *            Type of single event in data to aggregate.
 */
public abstract class AggregateFunctionConfig<EventType> extends FunctionConfig {
	/**
	 * @return Strategy to decide which events are part of the aggregate.
	 */
	protected Window<EventType> getWindow(String windowDescription) {
		final Window<EventType> window;
		if (windowDescription.isEmpty()) {
			// All events ever seen.
			window = new All<EventType>().make();
		} else {
			String[] parts = windowDescription.split(" ", 3);
			if (parts.length != 3) {
				throw new IllegalArgumentException(
						"Require pattern, for example: within 5 sec");
			} else if (parts[2].equals("sec")) {
				// Sliding window over time.
				window = new AgingOut<EventType>(
						Long.parseLong(parts[1]) * 1000);
			} else if (parts[2].equals("elements")) {
				// Fixed number of most recent events.
				window = new FixedSize<EventType>(Integer.parseInt(parts[1]));
			} else {
				throw new IllegalArgumentException("Cannot parse.");
			}
		}
		return window;
	}
}
