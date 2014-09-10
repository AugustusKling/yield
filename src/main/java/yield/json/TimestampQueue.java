package yield.json;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yield.config.function.where.Expr;
import yield.core.ValueMapper;

public class TimestampQueue implements ValueMapper<JsonEvent, JsonEvent> {
	private static final Logger logger = LogManager
			.getLogger(TimestampQueue.class);

	private Expr inputValue;
	private SimpleDateFormat pattern;

	/**
	 * Default values for those date parts that are not defined by the pattern
	 * in use.
	 */
	private Calendar base;

	private boolean iso8601Pattern = false;

	private boolean epochMilliseconds = false;

	public TimestampQueue(String pattern, Locale locale, Calendar base,
			Expr inputValue) {
		this.base = base;
		if ("=ISO8601".equals(pattern)) {
			iso8601Pattern = true;
		} else if ("=EPOCH-MILLISECONDS".equals(pattern)) {
			epochMilliseconds = true;
		} else {
			this.pattern = new SimpleDateFormat(pattern, locale);
			this.pattern.setLenient(false);
		}
		this.inputValue = inputValue;
	}

	@Override
	public JsonEvent map(JsonEvent value) {
		Object input = inputValue.apply(value).getValue();

		// Unix timestamp.
		if (epochMilliseconds) {
			long epoch;
			if (input instanceof String) {
				epoch = Long.parseLong((String) input);
			} else if (input instanceof Number) {
				epoch = ((Number) input).longValue();
			} else {
				logger.error("Failed to parse date.");
				return value;
			}
			Calendar parsed = Calendar.getInstance();
			parsed.setTimeInMillis(epoch);
			JsonEvent ret = new JsonEvent(value);
			ret.getObject().put("timestamp", parsed.getTimeInMillis());
			return ret;
		}

		if (input instanceof Number) {
			input = input.toString();
		}
		if (input instanceof String) {
			if (iso8601Pattern) {
				Calendar parsed = DatatypeConverter
						.parseDateTime((String) input);
				JsonEvent ret = new JsonEvent(value);
				ret.getObject().put("timestamp", parsed.getTimeInMillis());
				return ret;
			} else {
				ParsePosition pos = new ParsePosition(0);
				Date date;
				Calendar parseBase;
				synchronized (pattern) {
					parseBase = Calendar.getInstance();
					parseBase.setLenient(false);
					pattern.setCalendar(parseBase);
					date = pattern.parse((String) input, pos);
				}
				if (pos.getErrorIndex() == -1
						&& pos.getIndex() == ((String) input).length()) {
					// Parsing succeeded.

					// Apply defaults for values that have not been read by
					// parsing.
					Calendar defaults;
					if (base != null) {
						defaults = base;
					} else {
						defaults = Calendar.getInstance();
					}
					String textPattern = pattern.toPattern();
					if (!textPattern.contains("y")) {
						parseBase.set(Calendar.YEAR,
								defaults.get(Calendar.YEAR));
					}
					if (!textPattern.contains("M")) {
						parseBase.set(Calendar.MONTH,
								defaults.get(Calendar.MONTH));
					}
					if (!textPattern.contains("D")
							&& !textPattern.contains("d")) {
						parseBase.set(Calendar.DAY_OF_MONTH,
								defaults.get(Calendar.DAY_OF_MONTH));
					}
					if (!textPattern.toUpperCase().contains("H")
							&& !textPattern.toUpperCase().contains("K")) {
						parseBase.set(Calendar.HOUR_OF_DAY,
								defaults.get(Calendar.HOUR_OF_DAY));
					}
					if (!textPattern.contains("m")) {
						parseBase.set(Calendar.MINUTE,
								defaults.get(Calendar.MINUTE));
					}
					if (!textPattern.contains("s")) {
						parseBase.set(Calendar.SECOND,
								defaults.get(Calendar.SECOND));
					}
					if (!textPattern.contains("S")) {
						parseBase.set(Calendar.MILLISECOND,
								defaults.get(Calendar.MILLISECOND));
					}
					if (!textPattern.toUpperCase().contains("Z")
							&& textPattern.contains("X")) {
						parseBase.set(Calendar.ZONE_OFFSET,
								defaults.get(Calendar.ZONE_OFFSET));
					}
					date.setTime(parseBase.getTimeInMillis());

					JsonEvent ret = new JsonEvent(value);
					ret.getObject().put("timestamp", date.getTime());
					return ret;
				} else {
					logger.warn(pattern.format(new Date()));
					// Date parsing failed.
					logger.error("Failed to parse date from " + input);
				}
			}
		} else {
			logger.error("Failed to parse date.");
		}
		return value;
	}
}
