package yield.config.function;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.ParameterMap;
import yield.config.ParameterMap.Param;
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

	private enum Parameters implements Param {
		/**
		 * Path to watch.
		 */
		path {
			@Override
			public Object getDefault() {
				throw new UnsupportedOperationException();
			}
		},

		/**
		 * Skip over existing data when watching files for additions.
		 */
		skip {
			@Override
			public Boolean getDefault() {
				return true;
			}
		},

		/**
		 * Only read the file one, the terminate this input. Only for files.
		 */
		once {
			@Override
			public Boolean getDefault() {
				return false;
			}
		}
	}

	private class WatchOptions {
		/**
		 * Path to watch.
		 */
		private String path;

		/**
		 * Skip over existing data when watching files for additions.
		 */
		private boolean skip = (boolean) Parameters.skip.getDefault();

		/**
		 * Only read the file one, the terminate this input. Only for files.
		 */
		private boolean once = (boolean) Parameters.once.getDefault();
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ParameterMap<Parameters> parameters = parseArguments(args,
				Parameters.class);
		WatchOptions options = new WatchOptions();
		if (parameters.containsKey(Parameters.path)) {
			options.path = parameters.getString(Parameters.path);
		} else {
			throw new IllegalArgumentException(
					"Watch target not given. Path to file or directory needs to be provided.");
		}
		options.skip = parameters.getBoolean(Parameters.skip);
		options.once = parameters.getBoolean(Parameters.once);

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
