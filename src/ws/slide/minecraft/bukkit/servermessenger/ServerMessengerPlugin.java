package ws.slide.minecraft.bukkit.servermessenger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerMessengerPlugin extends JavaPlugin {
	private static ServerMessengerPlugin instance;

	public static ServerMessengerPlugin getInstance() {
		if (instance == null)
			instance = new ServerMessengerPlugin();

		return instance;
	}

	public static ServerMessenger getMessenger() {
		return ServerMessengerPlugin.getInstance().messenger;
	}

	private Set<NetworkServer> servers;
	private NetworkServerListenerThread network_server_listener_thread;
	private ServerMessenger messenger;
	private String host = "127.0.0.1";
	private int port = 62323;
	private boolean allow_incoming = true;

	public ServerMessengerPlugin() {
		ServerMessengerPlugin.instance = this;
		this.messenger = new ServerMessenger();
		this.servers = new HashSet<NetworkServer>();
	}

	public void setServers(Set<NetworkServer> servers) {
		this.servers = servers;
	}

	public Set<NetworkServer> getServers() {
		return servers;
	}

	public NetworkServer getServerByHost(String host) {
		for (NetworkServer server : servers)
			if (server.getHost().equals(host))
				return server;

		return null;
	}

	public NetworkServer getServer(String name) {
		for (NetworkServer server : servers)
			if (server.getName().equals(name))
				return server;

		return null;
	}

	public NetworkServerListenerThread getNetworkServerListenerThread() {
		return this.network_server_listener_thread;
	}

	public String getServerName() {
		return Bukkit.getServerName();
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public void onEnable() {
		getLogger().info("Starting up on server " + getServerName());
		loadServers();

		this.host = getConfig().getString("host", "127.0.0.1");
		this.port = getConfig().getInt("port", 62323);
		this.allow_incoming = getConfig().getBoolean("allow_incoming", true);

		if (this.allow_incoming) {
			getLogger().info("Listening on port " + this.host + ":" + this.port);
			try {
				this.network_server_listener_thread = new NetworkServerListenerThread(InetAddress.getByName(this.host), this.port);
				this.network_server_listener_thread.start();
			} catch (IOException e) { e.printStackTrace(); }
		}

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for (NetworkServer server : servers) {
					if (server.getConnect() && server.getConnection() == null && server.getFailedConnectionAttemptCount() < 5) {
						ServerMessengerPlugin.getInstance().getLogger().info("Reconnecting to " + server.getName());

						NetworkServerConnector network_server_connector = new NetworkServerConnector(server);
						network_server_connector.run();

						server.incrementFailedConnectionAttemptCount();
					}
				}
			}
		}, 20 * 10, 20 * 10);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				network_server_listener_thread.a();
			}
		}, 1, 1);
	}

	@Override
	public void onDisable() {
		if (this.network_server_listener_thread != null)
			this.network_server_listener_thread.stopListening();
	}

	private void loadServers() {
		if (getConfig().contains("servers")) {
			getLogger().info("Loading servers");
			Map<String, Object> server_names = getConfig().getConfigurationSection("servers").getValues(false);
			for (String server_name : server_names.keySet()) {
				String host = getConfig().getString("servers." + server_name + ".host", "127.0.0.1");
				int port = getConfig().getInt("servers." + server_name + ".port", 62323);
				boolean connect = getConfig().getBoolean("servers." + server_name + ".connect", true);

				NetworkServer network_server = new NetworkServer(server_name, host, port);
				network_server.setConnect(connect);
				this.servers.add(network_server);
			}
		}
		else
			getLogger().severe("No servers configured!");
	}
}
