package ws.slide.minecraft.bukkit.servermessenger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public class ServerMessengerPlugin extends JavaPlugin {
	private static ServerMessengerPlugin instance;
	private String host = "127.0.0.1";
	private int port = 62323;
	private boolean allow_incoming = true;

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

	@Override
	public void onEnable() {
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
					if (!server.isConnected() && server.getFailedConnectionAttemptCount() < 5) {
						ServerMessengerPlugin.getInstance().getLogger().info("Reconnecting to " + server.getName());

						NetworkServerConnector network_server_connector = new NetworkServerConnector(server);
						network_server_connector.run();

						server.incrementFailedConnectionAttemptCount();
					}
				}
			}
		}, 20 * 10, 20 * 10);
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
				String host = getConfig().getString("servers." + server_name + ".host");
				int port = getConfig().getInt("servers." + server_name + ".port");

				NetworkServer network_server = new NetworkServer(server_name, host, port);
				this.servers.add(network_server);
			}
		}
		else
			getLogger().severe("No servers configured!");
/*
	public void loadTeams() {
		if (getConfig().contains("teams")) {
			getLogger().info("Loading teams");
			Map<String, Object> team_names = getConfig().getConfigurationSection("teams").getValues(false);
			for (String team_name : team_names.keySet()) {
				String block_info = getConfig().getString("teams." + team_name + ".block");
				ChatColor color = ChatColor.valueOf(getConfig().getString("teams." + team_name + ".color").toUpperCase());

				Team team = new Team(team_name, block_info, color);
				team_manager.addTeam(team_name, team);
			}
		}
		else
			getLogger().severe("No teams configured!");
	}
 */
	}
}
