package cat.meowkotuk606.anticrash;

import org.bukkit.entity.Player;

import cat.meowkotuk606.anticrash.Objects.VerifyStatus;

public class CatAntiCrashAPI {
	public static VerifyStatus getStatus(Player player) { return CatAntiCrash.v.getOrDefault(player, new VerifyStatus()).copy(); }

	public static boolean isVerify(Player p) { return !CatAntiCrash.v.getOrDefault(p, new VerifyStatus()).inCheck(); }
}
