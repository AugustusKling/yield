package yield.config;

/**
 * Pair of function alias and its implementation.
 */
public class FunctionDefinition {
	/**
	 * Name of function.
	 */
	public final String functionName;

	/**
	 * Implementation of function.
	 */
	public final FunctionConfig functionConfig;

	public FunctionDefinition(String functionName, FunctionConfig functionConfig) {
		this.functionName = functionName;
		this.functionConfig = functionConfig;
	}
}
