package yield.output.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import yield.core.EventListener;
import yield.core.Main;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Forwards events to an UDP socket.
 */
public class UDPNetworkSocket implements EventListener<String> {

	private DatagramSocket socket;
	private InetAddress group;
	private int port;

	public UDPNetworkSocket(Main main, ObjectNode config)
			throws SocketException, UnknownHostException {
		port = config.get("port").intValue();
		String target = config.get("host").textValue();

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
			throw new RuntimeException(e1);
		}
	}
}
