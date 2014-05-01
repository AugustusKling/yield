package yield.json;

import yield.core.EventListener;
import yield.core.EventSource;
import yield.core.Main;
import yield.core.MappedQueue;
import yield.core.ValueMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Converts {@link String} to JSON, than extracts matched fragments using a
 * regular expression. The extracted fragments are than available as own
 * properties within the yielded JSON event.
 * 
 * @see http://logstash.net/docs/1.4.0/filters/grok
 */
public class GrokingQueue extends EventSource<JsonEvent> implements
		EventListener<String> {

	private MappedQueue<String, JsonEvent> jsonParser;
	private MappedQueue<JsonEvent, JsonEvent> results;

	public GrokingQueue(Main main, ObjectNode config) {
		String path = config.get("path").textValue();
		String pattern = config.get("pattern").textValue();

		// Convert String to JSON
		jsonParser = new MappedQueue<>(new ValueMapper<String, JsonEvent>() {
			@Override
			public JsonEvent map(String value) {
				return new JsonEvent(value);
			}
		});
		// Extract fragments.
		ValueMapper<JsonEvent, JsonEvent> mapper = new JsonGrok(path, pattern);

		results = new MappedQueue<>(mapper);
		jsonParser.getQueue().bind(results);
	}

	@Override
	public void bind(EventListener<JsonEvent> printer) {
		results.getQueue().bind(printer);
	}

	@Override
	public void feed(String e) {
		jsonParser.feed(e);
	}
}
