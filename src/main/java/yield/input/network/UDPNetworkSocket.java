package yield.input.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

import yield.core.EventQueue;

/**
 * Creates an UDP listener and yields all received datagrams.
 */
public class UDPNetworkSocket extends EventQueue<String> {
	public UDPNetworkSocket(final String host, final int port) {
		super(String.class);

		new Thread() {
			@Override
			public void run() {
				try (MulticastSocket socket = new MulticastSocket(port)) {
					InetAddress group = InetAddress.getByName(host);
					socket.joinGroup(group);
					while (true) {
						byte[] buf = new byte[8192];
						DatagramPacket packet = new DatagramPacket(buf,
								buf.length);
						socket.receive(packet);
						feed(new String(packet.getData(),
								StandardCharsets.UTF_8));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			};
		}.start();
	}
}
