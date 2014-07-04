package yield.config.function;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;

import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.input.directory.DirectoryEvent;
import yield.input.directory.DirectoryWatcher;
import yield.input.shipper.ShipperFile;
import yield.json.JsonEvent;

/**
 * Watches files or directories for modifications.
 * <p>
 * Yields either {@link DirectoryEvent}s for directories or {@link String}s for
 * files.
 */
public class Watch extends FunctionConfig {
	private String resultType;

	private enum Parameters {
		/**
		 * Path to watch.
		 */
		path,

		/**
		 * Skip over existing data when watching files for additions.
		 */
		skip,

		/**
		 * Only read the file one, the terminate this input. Only for files.
		 */
		once
	}

	private class WatchOptions {
		/**
		 * Path to watch.
		 */
		private String path;

		/**
		 * Skip over existing data when watching files for additions.
		 */
		private boolean skip = true;

		/**
		 * Only read the file one, the terminate this input. Only for files.
		 */
		private boolean once = false;
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Map<Parameters, String> parameters = parseArguments(args,
				Parameters.class);
		WatchOptions options = new WatchOptions();
		if (parameters.containsKey(Parameters.path)) {
			options.path = parameters.get(Parameters.path);
		} else {
			throw new IllegalArgumentException(
					"Watch target not given. Path to file or directory needs to be provided.");
		}
		if (parameters.containsKey(Parameters.skip)) {
			options.skip = "true".equals(Parsers.or(
					Scanners.string("true").source(),
					Scanners.string("false").source()).parse(
					parameters.get(Parameters.skip)));
		}
		if (parameters.containsKey(Parameters.once)) {
			options.once = "true".equals(Parsers.or(
					Scanners.string("true").source(),
					Scanners.string("false").source()).parse(
					parameters.get(Parameters.once)));
		}

		Path watchable = Paths.get(options.path);
		if (watchable.toFile().isDirectory() || options.path.endsWith("/")) {
			this.resultType = JsonEvent.class.getName();
			try {
				DirectoryWatcher watcher = new DirectoryWatcher(watchable);
				MappedQueue<DirectoryEvent, JsonEvent> toJSON = new MappedQueue<>(
						new ValueMapper<DirectoryEvent, JsonEvent>() {

							@Override
							public JsonEvent map(DirectoryEvent value) {
								JsonEvent event = new JsonEvent();
								event.put("type", value.type.toString());
								event.put("affectedPath", value.affectedPath
										.toAbsolutePath().toString());
								return event;
							}
						});
				watcher.getQueue().bind(toJSON);
				return wrapResultingYielder(toJSON.getQueue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			this.resultType = String.class.getName();
			ShipperFile watcher = new ShipperFile(watchable.toAbsolutePath());
			watcher.read(options.once, options.skip);
			return wrapResultingYielder(watcher.getQueue());
		}
	}

	@Override
	protected String getResultEventType() {
		return this.resultType;
	}
}
