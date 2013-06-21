package ws.slide.minecraft.bukkit.servermessenger;

public interface PluginMessageListener extends org.bukkit.plugin.messaging.PluginMessageListener {
	void onPluginMessageReceived(String channel, NetworkServer player, byte[] message);
}
