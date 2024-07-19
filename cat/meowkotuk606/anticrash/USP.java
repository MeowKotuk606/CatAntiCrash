package cat.meowkotuk606.anticrash;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;

public class USP {
	private ServerProtectorAPI a = ((ServerProtectorManager) Bukkit.getPluginManager().getPlugin("UltimateServerProtector")).getPluginAPI();

	public boolean logged(Player p) { return a.isAuthorised(p); }
}
