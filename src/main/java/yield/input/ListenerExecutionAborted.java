package yield.input;

/**
 * Throw if a listener aborted to process an event because it was requested to
 * abort. For example when the listener is interrupted due to
 * {@link Thread#interrupt()}.
 */
public class ListenerExecutionAborted implements ControlEvent {

}
