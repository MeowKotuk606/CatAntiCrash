package cat.meowkotuk606.anticrash;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerItemMendEvent;

public class r1_13 implements Listener {
	public r1_13(CatAntiCrash main) { Bukkit.getPluginManager().registerEvents(this, main); }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandSend(PlayerCommandSendEvent event) {
		Player p = event.getPlayer();
		event.getCommands().removeIf(cmd -> {
			if (cmd.equals("verify") && CatAntiCrash.config.getConfigurationSection("admins").getKeys(false).contains(p.getName())) { return false; }
			if (!p.hasPermission("catanticrash.bypass.commands")) {
				if (CatAntiCrash.isBlockCmd(cmd, !CatAntiCrashAPI.isVerify(p)) || CatAntiCrash.config.getBoolean("commands.blacklist.block_all")) { return true; }
			} else return false;
			return false;
		});
	}

	public boolean onBlockDispense(BlockDispenseEvent event, Block block, World world, Location location) {
		if (world != null) {
			int maxHeight = world.getMaxHeight() - 1;
			if (block.getBlockData() instanceof Dispenser) {
				Dispenser dispenser = (Dispenser) block.getBlockData();
				BlockFace face = dispenser.getFacing();
				double y = location.getY();
				if ((y >= maxHeight && face == BlockFace.UP) || (y <= 1.0D && face == BlockFace.DOWN)) {
					event.setCancelled(true);
					CatAntiCrash.exploitPunish(null);
				}
			}
		}
		return true;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemMenu(PlayerItemMendEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }
}
