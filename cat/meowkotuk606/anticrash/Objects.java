package cat.meowkotuk606.anticrash;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

public class Objects {
	public static class VerifyStatus {
		public Code codeVerify = new Code();
		public Button buttonVerify = new Button();
		public Password passwordVerify = new Password();
		public Pair<BossBar, BukkitTask> bossbarCode = new Pair<>(Bukkit.createBossBar(CatAntiCrash.config.getString("bossbar.code.title").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.code.time"))),
			BarColor.valueOf(CatAntiCrash.config.getString("bossbar.code.color")), BarStyle.valueOf(CatAntiCrash.config.getString("bossbar.code.type"))), null);
		public Pair<BossBar, BukkitTask> bossbarButton = new Pair<>(Bukkit.createBossBar(CatAntiCrash.config.getString("bossbar.button.title").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.button.time"))),
			BarColor.valueOf(CatAntiCrash.config.getString("bossbar.button.color")), BarStyle.valueOf(CatAntiCrash.config.getString("bossbar.button.type"))), null);
		public Pair<BossBar, BukkitTask> bossbarPassword = new Pair<>(Bukkit.createBossBar(CatAntiCrash.config.getString("bossbar.password.title").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.password.time"))),
			BarColor.valueOf(CatAntiCrash.config.getString("bossbar.password.color")), BarStyle.valueOf(CatAntiCrash.config.getString("bossbar.password.type"))), null);
		public BukkitTask tsk = null;
		public int checkInt = 0;

		public boolean inCheck() { return codeVerify.inCheck || buttonVerify.inCheck || passwordVerify.inCheck; }

		public boolean isCompleted() { return codeVerify.completed || buttonVerify.completed || passwordVerify.completed; }

		public VerifyStatus copy() {
			VerifyStatus copy = new VerifyStatus();
			Code code = new Code();
			code.completed = this.codeVerify.completed;
			code.inCheck = this.codeVerify.inCheck;
			Button button = new Button();
			button.completed = this.buttonVerify.completed;
			button.inCheck = this.buttonVerify.inCheck;
			Password passw = new Password();
			passw.completed = this.passwordVerify.completed;
			passw.inCheck = this.passwordVerify.inCheck;
			copy.codeVerify = code;
			copy.buttonVerify = button;
			copy.passwordVerify = passw;
			copy.checkInt = checkInt;
			return copy;
		}
	}

	public static class Int { public int v = 0; }

	public static class Pair<T, E> {
		public T v1;
		public E v2;

		public Pair(T v1, E v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
	}

	public static class Password extends Verify { public String pass = ""; }

	public static class Code extends Verify { public String code = ""; }

	public static class Button extends Verify {}

	public static abstract class Verify {
		public boolean inCheck;
		public boolean completed;
	}
}
