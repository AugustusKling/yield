package yield.input.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import yield.core.EventSource;

/**
 * Reads from standard input (or user's console), yields each line as event.
 */
public class Stdin extends EventSource<String> {
	public Stdin() {
		new Thread() {
			@Override
			public void run() {
				Reader reader = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader(reader);
				String line;
				try {
					while ((line = br.readLine()) != null) {
						feedBoundQueues(line);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			};
		}.start();
	}
}
