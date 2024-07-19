package cat.meowkotuk606.anticrash;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import cat.meowkotuk606.anticrash.CatAntiCrash.CatLevel;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

public class Utils {
	protected Boolean removeOp(Player player) {
		if (CatAntiCrash.config.getBoolean("defend.op.enabled")) {
			boolean a = CatAntiCrash.config.getBoolean("defend.op.whitelist.admins") ? !CatAntiCrash.config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!CatAntiCrash.config.getStringList("defend.op.whitelist.players").contains(player.getName()) && a) {
				if (player.isOp()) {
					player.setOp(false);
					CatAntiCrash.log(CatAntiCrash.langLog.removeOP.formatted(player.getName()), CatLevel.warn);
					CatAntiCrash.send(player, CatAntiCrash.config.getString("remove.op"));
					return true;
				}
			}
		}
		return false;
	}

	protected Boolean removeGameMode(Player player) {
		if (CatAntiCrash.config.getBoolean("defend.gamemode.enabled")) {
			boolean a = CatAntiCrash.config.getBoolean("defend.gamemode.whitelist.admins") ? !CatAntiCrash.config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!CatAntiCrash.config.getStringList("defend.gamemode.whitelist.players").contains(player.getName()) && a) {
				Set<GameMode> blacklist = CatAntiCrash.config.getStringList("defend.gamemode.blacklist").stream().map(mode -> {
					try {
						return GameMode.valueOf(mode.toUpperCase());
					} catch (IllegalArgumentException e) {
						CatAntiCrash.log(CatAntiCrash.langLog.undefinedGM.formatted(mode), CatLevel.warn);
						return null;
					}
				}).filter(mode -> mode != null).collect(Collectors.toSet());
				if (blacklist.contains(player.getGameMode())) {
					GameMode gm = player.getGameMode();
					player.setGameMode(GameMode.valueOf(CatAntiCrash.config.getString("defend.gamemode.replace").toUpperCase()));
					CatAntiCrash.log(CatAntiCrash.langLog.removeGM.formatted(player.getName()), CatLevel.warn);
					CatAntiCrash.send(player, CatAntiCrash.config.getString("remove.gamemode").replace("%gamemode%", gm.toString()));
					return true;
				}
			}
		}
		return false;
	}

	protected Boolean removeGroups(Player player) {
		if (CatAntiCrash.config.getBoolean("defend.groups.enabled")) {
			boolean a = CatAntiCrash.config.getBoolean("defend.groups.whitelist.admins") ? !CatAntiCrash.config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!CatAntiCrash.config.getStringList("defend.groups.whitelist.players").contains(player.getName()) && a) {
				Set<Group> l = CatAntiCrash.getGroups(player);
				if (l == null) {
					CatAntiCrash.log(CatAntiCrash.langLog.errorCheckGroup.formatted(player.getName()), CatLevel.error);
					return true;
				}
				if (!l.isEmpty()) {
					l.stream().forEach(g -> {
						try {
							User u = CatAntiCrash.lp.getUserManager().loadUser(player.getUniqueId()).get();
							u.data().remove(Node.builder("group." + g.getName()).build());
							CatAntiCrash.lp.getUserManager().saveUser(u);
						} catch (Exception e) {
							e.printStackTrace();
						}
						CatAntiCrash.log(CatAntiCrash.langLog.removeGroup.formatted(g.getName(), player.getName()), CatLevel.warn);
						CatAntiCrash.send(player, CatAntiCrash.config.getString("remove.groups").replace("%group%", g.getName()));
					});
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	protected Boolean removePerms(Player player) {
		if (CatAntiCrash.config.getBoolean("defend.permissions.enabled")) {
			boolean a = CatAntiCrash.config.getBoolean("defend.permissions.whitelist.admins") ? !CatAntiCrash.config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!CatAntiCrash.config.getStringList("defend.permissions.whitelist.players").contains(player.getName()) && a) {
				User user = CatAntiCrash.lp.getUserManager().loadUser(player.getUniqueId()).join();
				Collection<Node> un = user.getNodes();
				AtomicBoolean b = new AtomicBoolean(false);
				un.stream().filter(node -> CatAntiCrash.isBlock(node)).forEach(node -> {
					try {
						User u = CatAntiCrash.lp.getUserManager().loadUser(player.getUniqueId()).get();
						u.data().remove(node);
						CatAntiCrash.lp.getUserManager().saveUser(u);
					} catch (Exception e) {
						e.printStackTrace();
					}
					CatAntiCrash.log(CatAntiCrash.langLog.removePermGroup.formatted(node.getKey(), player.getName()), CatLevel.warn);
					CatAntiCrash.send(player, CatAntiCrash.config.getString("remove.permissions").replace("%permission%", node.toString()));
					b.set(true);
				});
				return b.get();
			}
		}
		return false;
	}
}
