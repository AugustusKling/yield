package yield.output.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventType;
import yield.input.ListenerExceutionFailed;

/**
 * Forwards events to an UDP socket.
 */
public class UDPNetworkSocket extends BaseControlQueueProvider implements
		EventListener<String> {

	private DatagramSocket socket;
	private InetAddress group;
	private int port;

	public UDPNetworkSocket(String target, int port) throws SocketException,
			UnknownHostException {
		this.port = port;

		socket = new DatagramSocket();

		group = InetAddress.getByName(target);
	}

	@Override
	public void feed(String e) {
		byte[] buf = e.getBytes(StandardCharsets.UTF_8);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
		try {
			socket.send(packet);
		} catch (IOException e1) {
			this.getControlQueue().feed(new ListenerExceutionFailed<>(e, e1));
		}
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return new EventType(String.class);
	}
}
