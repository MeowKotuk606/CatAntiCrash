package cat.meowkotuk606.anticrash;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class r1_20_add implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerSignOpen(PlayerSignOpenEvent event) {
		if (CatAntiCrash.isVerify(event.getPlayer())) {
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