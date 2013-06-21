package ws.slide.minecraft.bukkit.servermessenger;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.server.v1_5_R3.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;

import com.google.common.collect.ImmutableSet;

public class NetworkServer implements PluginMessageRecipient {
	private String name;
	private String host = "127.0.0.1";
	private int port = 62323;
	private boolean connect = true;
	private Socket socket = null;
	private NetworkServerConnection connection;
    private final Set<String> channels = new HashSet<String>();
    private int failed_connection_attempt_count = 0;

    public NetworkServer(String name, String host, int port) {
    	this.name = name;
    	this.host = host;
    	this.port = port;
    }
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean getConnect() {
		return this.connect;
	}

	public void setConnect(boolean connect) {
		this.connect = connect;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setConnection(NetworkServerConnection connection) {
		this.connection = connection;
	}

	public NetworkServerConnection getConnection() {
		return this.connection;
	}

	public void addChannel(String channel) {
		if (channels.add(channel)) {
		}
	}
	
	public void removeChannel(String channel) {
		if (channels.remove(channel)) {
		}
	}

	public Set<String> getListeningPluginChannels() {
		return ImmutableSet.copyOf(channels);
	}

	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        ServerMessenger.validatePluginMessage(ServerMessengerPlugin.getMessenger(), source, channel, message);

//        for (String achannel : channels)
//        	ServerMessengerPlugin.getInstance().getLogger().info("Channel:" + achannel);

//        if (channels.contains(channel)) {
//        	ServerMessengerPlugin.getInstance().getLogger().info("Constructing packet for plugin message");

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.tag = channel;
			packet.length = message.length;
			packet.data = message;
			try {
				connection.sendPacket(packet);
			} catch (Throwable e) {
				e.printStackTrace();
			}
//        }
	}

	public boolean isConnected() {
		if (this.connection != null)
			return this.connection.a();

		return false;
	}

	public int getFailedConnectionAttemptCount() {
		return failed_connection_attempt_count;
	}

	public void setFailedConnectionAttemptCount(int failed_connection_attempt_count) {
		this.failed_connection_attempt_count = failed_connection_attempt_count;
	}

	public void incrementFailedConnectionAttemptCount() {
		this.failed_connection_attempt_count++;
	}
}
