package ws.slide.minecraft.bukkit.servermessenger;

import java.lang.reflect.*;
import java.net.Socket;

import net.minecraft.server.v1_5_R3.Packet2Handshake;

public class NetworkServerConnector extends Thread {
	private NetworkServer server;
	private Socket socket;
	private NetworkServerConnection network_server_connection;

	public NetworkServerConnector(NetworkServer server) {
		this.server = server;
	}

    public void run() {
    	try {
			this.socket = new Socket(server.getHost(), server.getPort());
			this.network_server_connection = new NetworkServerConnection(this.socket, server.getName());

			Packet2Handshake packet2handshake = new Packet2Handshake();

			Field afield = Packet2Handshake.class.getDeclaredField("a");
			afield.setAccessible(true);
			afield.setInt(packet2handshake, 0);

			Field bfield = Packet2Handshake.class.getDeclaredField("b");
			bfield.setAccessible(true);
			bfield.set(packet2handshake, server.getName());

			packet2handshake.c = server.getHost();
			packet2handshake.d = server.getPort();

			this.network_server_connection.sendPacket(packet2handshake);
		} catch (Exception e) {
			server.incrementFailedConnectionAttemptCount();

			e.printStackTrace();
		} catch (Throwable e) {
			server.incrementFailedConnectionAttemptCount();

			e.printStackTrace();
		}
    }
}
