package ws.slide.minecraft.bukkit.servermessenger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.*;
import java.net.Socket;

import net.minecraft.server.v1_5_R3.Packet250CustomPayload;
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
			this.network_server_connection = new NetworkServerConnection(this.socket, server.getName(), this.server);
			this.server.setConnection(this.network_server_connection);

			Packet2Handshake packet2handshake = new Packet2Handshake();

			Field afield = Packet2Handshake.class.getDeclaredField("a");
			afield.setAccessible(true);
			afield.setInt(packet2handshake, 0);

			Field bfield = Packet2Handshake.class.getDeclaredField("b");
			bfield.setAccessible(true);
			bfield.set(packet2handshake, ServerMessengerPlugin.getInstance().getServerName());

			packet2handshake.c = ServerMessengerPlugin.getInstance().getHost();
			packet2handshake.d = ServerMessengerPlugin.getInstance().getPort();

			ServerMessengerPlugin.getInstance().getLogger().info(ServerMessengerPlugin.getInstance().getServerName() + " " + ServerMessengerPlugin.getInstance().getHost() + ":" + ServerMessengerPlugin.getInstance().getPort());

			this.network_server_connection.sendPacket(packet2handshake);
/*
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
			dataoutputstream.writeUTF("CHAT");
			this.network_server_connection.sendPacket(new Packet250CustomPayload("REGISTER", bytearrayoutputstream.toByteArray()));
*/
			ServerMessengerPlugin.getInstance().getNetworkServerListenerThread().addConnection(network_server_connection);
		} catch (Exception e) {
			server.incrementFailedConnectionAttemptCount();
			server.setConnection(null);

			e.printStackTrace();
		} catch (Throwable e) {
			server.incrementFailedConnectionAttemptCount();
			server.setConnection(null);

			e.printStackTrace();
		}
    }
}
