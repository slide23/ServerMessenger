package ws.slide.minecraft.bukkit.servermessenger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import net.minecraft.server.v1_5_R3.Connection;
import net.minecraft.server.v1_5_R3.MinecraftServer;
import net.minecraft.server.v1_5_R3.NetworkManager;
import net.minecraft.server.v1_5_R3.Packet;
import net.minecraft.server.v1_5_R3.Packet0KeepAlive;
import net.minecraft.server.v1_5_R3.Packet250CustomPayload;
import net.minecraft.server.v1_5_R3.Packet2Handshake;

public class NetworkServerConnection extends Connection {
	private final Socket socket;
	private final String name;
	private final NetworkServer server;
	private final NetworkManager network_manager;
	public boolean disconnected = false;

	public NetworkServerConnection(Socket socket, String name, NetworkServer server) throws IOException {
		this.socket = socket;
		this.name = name;
		this.server = server;
		this.network_manager = new NetworkManager(MinecraftServer.getServer().getLogger(), this.socket, this.name, this, null);
		this.network_manager.e = 0;
	}

	public NetworkServer getServer() {
		return this.server;
	}

	public NetworkManager getNetworkManager() {
		return this.network_manager;
	}

	@Override
	public boolean a() {
		return true;
	}

	public void c() {
//		Bukkit.getLogger().info("Called c which is calling b");
		try {
//			this.sendPacket(new Packet0KeepAlive(23));
		} catch (Throwable e) { e.printStackTrace(); }

		this.network_manager.b();
	}

	public void disconnect(String error) {
		
	}

	public void onUnhandledPacket(Packet packet) {
		ServerMessengerPlugin.getInstance().getLogger().info("Unhandled packet " + packet.a());
	}

	public void sendPacket(Packet packet) throws Throwable {
		ServerMessengerPlugin.getInstance().getLogger().info("Queueing packet!");
		this.network_manager.queue(packet);
	}

	public void a(Packet2Handshake packet2handshake) {
		ServerMessengerPlugin.getInstance().getLogger().info("Handshake <-> " + packet2handshake.f());

		NetworkServer server = ServerMessengerPlugin.getInstance().getServer(packet2handshake.f());
		if (server == null) {
			ServerMessengerPlugin.getInstance().getLogger().info("Invalid server attempting to connect: " + packet2handshake.f());
			return;
		}

		ServerMessengerPlugin.getInstance().getLogger().info("Server Connected: " + packet2handshake.f());
		server.setConnection(this);
	}

	public void a(Packet0KeepAlive packet0keepalive) {
		ServerMessengerPlugin.getInstance().getLogger().info("Keep Alive");
	}

	public void a(Packet250CustomPayload packet250custompayload) {
		ServerMessengerPlugin.getInstance().getLogger().info("Custom Payload in channel " + packet250custompayload.tag + "");
		ServerMessengerPlugin.getInstance().getLogger().info("Server " + this.server.getName() + " send data in channel " + packet250custompayload.tag);
		ServerMessengerPlugin.getMessenger().dispatchIncomingMessage(this.server, packet250custompayload.tag, packet250custompayload.data);
		if (packet250custompayload.tag.equals("REGISTER")) {
			ServerMessengerPlugin.getInstance().getLogger().info("Server " + this.server.getName() + " registering for channel " + packet250custompayload.tag);
			try {
				String channels = new String(packet250custompayload.data, "UTF8");
				for (String channel : channels.split("\0")) {
					getServer().addChannel(channel);
				}
			} catch (UnsupportedEncodingException ex) {
				throw new AssertionError(ex);
			}
		} else if (packet250custompayload.tag.equals("UNREGISTER")) {
			ServerMessengerPlugin.getInstance().getLogger().info("Server " + this.server.getName() + " unregistering for channel " + packet250custompayload.tag);
			try {
				String channels = new String(packet250custompayload.data, "UTF8");
				for (String channel : channels.split("\0")) {
					getServer().removeChannel(channel);
				}
			} catch (UnsupportedEncodingException ex) {
				throw new AssertionError(ex);
			}
		} else {
		}
	}
}
