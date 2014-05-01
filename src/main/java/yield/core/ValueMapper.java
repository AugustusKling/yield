package yield.core;

public interface ValueMapper<In, Out> {
	Out map(In value);
}
