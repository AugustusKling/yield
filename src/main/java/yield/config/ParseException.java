package yield.config;

/**
 * Failure to parse or process line in configuration.
 */
public class ParseException extends RuntimeException {
	private static final long serialVersionUID = 926391238556586051L;

	public ParseException(String message, ConfigLine configLine) {
		super(configLine + ": " + message);
	}

	public ParseException(String message, ConfigLine configLine,
			RuntimeException e) {
		super(configLine + ": " + message, e);
	}

}
