package ws.slide.minecraft.bukkit.servermessenger;

import java.io.IOException;
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
	private final NetworkManager network_manager;

	public NetworkServerConnection(Socket socket, String name) throws IOException {
		this.socket = socket;
		this.name = name;
		this.network_manager = new NetworkManager(MinecraftServer.getServer().getLogger(), this.socket, this.name, this, null);
		this.network_manager.e = 0;
	}

	@Override
	public boolean a() {
		return true;
	}

	public void sendPacket(Packet packet) throws Throwable {
        ServerMessengerPlugin.getInstance().getLogger().info("Queueing packet!");
		this.network_manager.queue(packet);
	}

	public void a(Packet2Handshake packet2handshake) {
        ServerMessengerPlugin.getInstance().getLogger().info("Handshake <-> " + packet2handshake.c);
	}

	public void a(Packet0KeepAlive packet0keepalive) {
        ServerMessengerPlugin.getInstance().getLogger().info("Keep Alive");
		
	}

	public void a(Packet250CustomPayload packet250custompayload) {
        ServerMessengerPlugin.getInstance().getLogger().info("Custom Payload");
		
	}
}
