package ws.slide.minecraft.bukkit.servermessenger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkServerListenerThread extends Thread {
	@SuppressWarnings("unused")
	private final InetAddress inet_address;
	private final int port;
	private final ServerSocket server_socket;
	private boolean accepting = true;
    private final List<NetworkServerConnection> connections = Collections.synchronizedList(new ArrayList<NetworkServerConnection>());

	public NetworkServerListenerThread(InetAddress inet_address, int port) throws IOException {
		this.port = port;
		this.server_socket = new ServerSocket(this.port, 0, inet_address);
		this.inet_address = (inet_address == null) ? (server_socket.getInetAddress()) : (inet_address);
	}

    public void run() {
        while (this.accepting) {
            try {
                Socket socket = this.server_socket.accept();
                ServerMessengerPlugin.getInstance().getLogger().info("Incoming connection!");
                NetworkServerConnection network_server_connection = new NetworkServerConnection(socket, "Connection #S");

                this.addConnection(network_server_connection);
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }

        ServerMessengerPlugin.getInstance().getLogger().info("Closing listening thread");
    }

    public void addConnection(NetworkServerConnection network_server_connection) {
    	this.connections.add(network_server_connection);
    }

    public void stopListening() {
    	this.accepting = false;
    	try {
			this.server_socket.close();
		} catch (IOException e) { e.printStackTrace(); }
    }
}
