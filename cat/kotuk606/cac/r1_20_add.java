package cat.kotuk606.cac;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class r1_20_add implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerSignOpen(PlayerSignOpenEvent event) {
		Player p = event.getPlayer();
		Boolean ver = CatAntiCrash.getVerify().get(p);
		Boolean ver1 = CatAntiCrash.getVerifyDS().get(p);
		if (ver1 != null && ver1 == true) {
			event.setCancelled(true);
		}
		if (ver != null && ver == true) {
			event.setCancelled(true);
		}
	}

	public int getDuration() {
		return PotionEffect.INFINITE_DURATION;
	}

	public PotionEffect getDarkness() {
		return new PotionEffect(PotionEffectType.DARKNESS, getDuration(), 1);
	}

	public PotionEffectType getDarknessType() {
		return PotionEffectType.DARKNESS;
	}
}
