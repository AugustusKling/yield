package yield.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Filters log events within given bounds.
 */
@Plugin(name = "LevelRangeFilter", category = "Core", elementType = "filter", printObject = true)
public class LevelRangeFilter extends AbstractFilter {
	private final Level minLevel;
	private final Level maxLevel;

	private LevelRangeFilter(final Level minLevel, final Level maxLevel,
			final Result onMatch, final Result onMismatch) {
		super(onMatch, onMismatch);
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	@Override
	public Result filter(final Logger logger, final Level level,
			final Marker marker, final String msg, final Object... params) {
		return filter(level);
	}

	@Override
	public Result filter(final Logger logger, final Level level,
			final Marker marker, final Object msg, final Throwable t) {
		return filter(level);
	}

	@Override
	public Result filter(final Logger logger, final Level level,
			final Marker marker, final Message msg, final Throwable t) {
		return filter(level);
	}

	@Override
	public Result filter(final LogEvent event) {
		return filter(event.getLevel());
	}

	private Result filter(final Level level) {
		if (maxLevel.intLevel() <= level.intLevel()
				&& minLevel.intLevel() >= level.intLevel()) {
			return onMatch;
		} else {
			return onMismatch;
		}
	}

	/**
	 * @param minLevel
	 *            Minimum log Level.
	 * @param maxLevel
	 *            Maximum log level.
	 * @param onMatch
	 *            Action to take on a match.
	 * @param onMismatch
	 *            Action to take on a mismatch.
	 * @return The created filter.
	 */
	@PluginFactory
	public static LevelRangeFilter createFilter(
			@PluginAttribute(value = "minLevel", defaultString = "TRACE") final Level minLevel,
			@PluginAttribute(value = "maxLevel", defaultString = "FATAL") final Level maxLevel,
			@PluginAttribute(value = "onMatch", defaultString = "NEUTRAL") final Result onMatch,
			@PluginAttribute(value = "onMismatch", defaultString = "DENY") final Result onMismatch) {
		return new LevelRangeFilter(minLevel, maxLevel, onMatch, onMismatch);
	}
}
