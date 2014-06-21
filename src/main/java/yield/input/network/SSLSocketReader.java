package yield.input.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Awaits data from a SSL connection and converts it to a {@link String} event.
 */
public class SSLSocketReader extends Thread {
	private Socket socket;
	private SSLServerSocket serverSocket;

	public SSLSocketReader(SSLServerSocket serverSocket, Socket socket) {
		this.serverSocket = serverSocket;
		this.socket = socket;
		this.setDaemon(true);
	}

	@Override
	public void run() {
		try (InputStream is = socket.getInputStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				serverSocket.feedBoundQueues(line);
			}
			System.out.println("Closed");
		} catch (IOException e) {
			serverSocket.getControlQueue()
					.feed(new ConnectionFailed(
							"Connection to event sender lost.", e));
		}
	}
}
