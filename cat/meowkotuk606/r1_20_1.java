package cat.meowkotuk606.anticrash;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;

public class r1_20_1 implements Listener {
	public r1_20_1(CatAntiCrash main) { Bukkit.getPluginManager().registerEvents(this, main); }
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerSignOpen(PlayerSignOpenEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }
}
