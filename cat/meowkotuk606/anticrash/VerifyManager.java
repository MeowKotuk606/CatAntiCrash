package cat.meowkotuk606.anticrash;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonObject;

import cat.meowkotuk606.anticrash.CatAntiCrash.CatLevel;
import cat.meowkotuk606.anticrash.Objects.Int;
import cat.meowkotuk606.anticrash.Objects.VerifyStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class VerifyManager {
	private CatAntiCrash main;

	public VerifyManager(CatAntiCrash cac) { main = cac; }

	protected void check(Player player) {
		if (CatAntiCrash.v.containsKey(player)) { return; }
		sendLines(player);
		CatAntiCrash.v.put(player, new VerifyStatus());
		if (CatAntiCrash.checkInt.get(0).equals("password")) {
			CatAntiCrash.manage.verifyPassword(player);
		} else if (CatAntiCrash.checkInt.get(0).equals("button")) {
			CatAntiCrash.manage.verifyButton(player);
		} else if (CatAntiCrash.checkInt.get(0).equals("code")) {
			CatAntiCrash.manage.verifyCode(player);
		} else CatAntiCrash.manage.success(player);
	}

	protected void task(Player player, String str) {
		if (CatAntiCrash.v.get(player).tsk != null && !CatAntiCrash.v.get(player).tsk.isCancelled()) { CatAntiCrash.v.get(player).tsk.cancel(); }
		CatAntiCrash.v.get(player).tsk = Bukkit.getScheduler().runTaskTimer(main, () -> {
			player.setGameMode(GameMode.SURVIVAL);
			if (CatAntiCrash.config.getBoolean("effects.verify." + str + ".enabled")) {
				Set<PotionEffect> set = CatAntiCrash.config.getStringList("effects.verify." + str + ".list").stream().map(effect -> {
					String[] e = effect.split(":");
					return new PotionEffect(PotionEffectType.getByName(e[0].toUpperCase()), e[2].equals("INFINITE") ? (CatAntiCrash.r == null ? 60 : CatAntiCrash.r.getDuration()) : Integer.parseInt(e[2]), Integer.parseInt(e[1]));
				}).collect(Collectors.toSet());
				player.addPotionEffects(set);
			}
			if (CatAntiCrash.config.getBoolean("titles.verify." + str + ".enabled")) {
				player.sendTitle(CatAntiCrash.color(CatAntiCrash.config.getString("titles.verify." + str + ".title")), CatAntiCrash.color(CatAntiCrash.config.getString("titles.verify." + str + ".sub")), 1, 100, 1);
			}
		}, 0L, 40L);
	}

	protected void barCode(Player player) {
		Int i = new Int();
		i.v = CatAntiCrash.config.getInt("timeout.code.time");
		CatAntiCrash.v.get(player).bossbarCode.v1.addPlayer(player);
		CatAntiCrash.v.get(player).bossbarCode.v2 = Bukkit.getScheduler().runTaskTimer(main, () -> {
			if (!CatAntiCrash.config.getBoolean("bossbar.code.enabled") || !player.isOnline() || !player.isValid() || !CatAntiCrash.v.get(player).codeVerify.inCheck) {
				if (CatAntiCrash.v.get(player) != null) { CatAntiCrash.v.get(player).bossbarCode.v1.removePlayer(player); }
				return;
			}
			i.v--;
			if (i.v < 0) {
				CatAntiCrash.v.get(player).bossbarCode.v1.removePlayer(player);
				CatAntiCrash.log(CatAntiCrash.langLog.timeoutLog.formatted(player.getName(), "code", String.valueOf(CatAntiCrash.config.getInt("timeout.code.time"))), CatLevel.warn);
				CatAntiCrash.log(player, CatAntiCrash.langLog.timeout.formatted("code", String.valueOf(CatAntiCrash.config.getInt("timeout.code.time"))));
				CatAntiCrash.kick(player, CatAntiCrash.config.getString("timeout.code.kick_reason").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.code.time"))));
			} else {
				CatAntiCrash.v.get(player).bossbarCode.v1.setTitle(CatAntiCrash.color(CatAntiCrash.config.getString("bossbar.code.title").replace("%time%", String.valueOf(i.v))));
			}
		}, 0L, 20L);
	}

	protected void barButton(Player player) {
		Int i = new Int();
		i.v = CatAntiCrash.config.getInt("timeout.button.time");
		CatAntiCrash.v.get(player).bossbarButton.v1.addPlayer(player);
		CatAntiCrash.v.get(player).bossbarButton.v2 = Bukkit.getScheduler().runTaskTimer(main, () -> {
			if (!CatAntiCrash.config.getBoolean("bossbar.button.enabled") || !player.isOnline() || !player.isValid() || !CatAntiCrash.v.get(player).buttonVerify.inCheck) {
				if (CatAntiCrash.v.get(player) != null) { CatAntiCrash.v.get(player).bossbarButton.v1.removePlayer(player); }
				return;
			}
			i.v--;
			if (i.v < 0) {
				CatAntiCrash.v.get(player).bossbarButton.v1.removePlayer(player);
				CatAntiCrash.log(CatAntiCrash.langLog.timeoutLog.formatted(player.getName(), "button", String.valueOf(CatAntiCrash.config.getInt("timeout.button.time"))), CatLevel.warn);
				CatAntiCrash.log(player, CatAntiCrash.langLog.timeout.formatted("button", String.valueOf(CatAntiCrash.config.getInt("timeout.button.time"))));
				CatAntiCrash.kick(player, CatAntiCrash.config.getString("timeout.button.kick_reason").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.button.time"))));
			} else {
				CatAntiCrash.v.get(player).bossbarButton.v1.setTitle(CatAntiCrash.color(CatAntiCrash.config.getString("bossbar.button.title").replace("%time%", String.valueOf(i.v))));
			}
		}, 0L, 20L);
	}

	protected void barPassword(Player player) {
		Int i = new Int();
		i.v = CatAntiCrash.config.getInt("timeout.password.time");
		CatAntiCrash.v.get(player).bossbarPassword.v1.addPlayer(player);
		CatAntiCrash.v.get(player).bossbarPassword.v2 = Bukkit.getScheduler().runTaskTimer(main, () -> {
			if (!CatAntiCrash.config.getBoolean("bossbar.password.enabled") || !player.isOnline() || !player.isValid() || !CatAntiCrash.v.get(player).passwordVerify.inCheck) {
				if (CatAntiCrash.v.get(player) != null) { CatAntiCrash.v.get(player).bossbarPassword.v1.removePlayer(player); }
				return;
			}
			i.v--;
			if (i.v < 0) {
				CatAntiCrash.v.get(player).bossbarPassword.v1.removePlayer(player);
				CatAntiCrash.log(CatAntiCrash.langLog.timeoutLog.formatted(player.getName(), "password", String.valueOf(CatAntiCrash.config.getInt("timeout.password.time"))), CatLevel.warn);
				CatAntiCrash.log(player, CatAntiCrash.langLog.timeout.formatted("password", String.valueOf(CatAntiCrash.config.getInt("timeout.password.time"))));
				CatAntiCrash.kick(player, CatAntiCrash.config.getString("timeout.password.kick_reason").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.password.time"))));
			} else {
				CatAntiCrash.v.get(player).bossbarPassword.v1.setTitle(CatAntiCrash.color(CatAntiCrash.config.getString("bossbar.password.title").replace("%time%", String.valueOf(i.v))));
			}
		}, 0L, 20L);
	}

	protected void verifyCode(Player player) {
		if (CatAntiCrash.v.get(player).codeVerify.completed) { return; }
		CatAntiCrash.v.get(player).codeVerify.inCheck = true;
		String code = CatAntiCrash.generateCode();
		TextChannel chnl = CatAntiCrash.jda.getTextChannelById(CatAntiCrash.config.getString("channel_id"));
		if (chnl == null) {
			CatAntiCrash.stop(CatAntiCrash.langLog.undefinedChannel.formatted(CatAntiCrash.config.getString("channel_id")));
		} else {
			EmbedBuilder embd = new EmbedBuilder();
			embd.setColor(0xFFA500);
			embd.setTitle(CatAntiCrash.config.getString("embed.code.title"));
			embd.addField(CatAntiCrash.config.getString("embed.code.fields.msg").replace("%name%", player.getName()), "`" + code + "`", false);
			if (CatAntiCrash.config.getBoolean("embed.code.fields.server.use")) { embd.addField(CatAntiCrash.config.getString("embed.code.fields.server.text"), CatAntiCrash.config.getString("server"), false); }
			MessageEmbed msg = embd.build();
			chnl.sendMessageEmbeds(msg).queue();
			task(player, "code");
			CatAntiCrash.v.get(player).codeVerify.code = code;
		}
		CatAntiCrash.send(player, CatAntiCrash.color(CatAntiCrash.config.getString("check.start.code").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.code.time")))));
		if (CatAntiCrash.config.getBoolean("timeout.code.enabled")) { barCode(player); }
	}

	protected void verifyPassword(Player player) {
		if (CatAntiCrash.v.get(player).passwordVerify.completed) { return; }
		CatAntiCrash.v.get(player).passwordVerify.inCheck = true;
		task(player, "password");
		CatAntiCrash.v.get(player).passwordVerify.pass = CatAntiCrash.config.getString("admins." + player.getName() + ".password");
		CatAntiCrash.send(player, CatAntiCrash.color(CatAntiCrash.config.getString("check.start.password").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.password.time")))));
		if (CatAntiCrash.config.getBoolean("timeout.password.enabled")) { barPassword(player); }
	}

	protected void verifyButton(Player player) {
		if (CatAntiCrash.v.get(player).buttonVerify.completed) { return; }
		CatAntiCrash.v.get(player).buttonVerify.inCheck = true;
		net.dv8tion.jda.api.entities.User discord = CatAntiCrash.jda.retrieveUserById(CatAntiCrash.config.getString("admins." + player.getName() + ".discord_id")).complete();
		if (discord != null) {
			task(player, "button");
			PrivateChannel chnl = discord.openPrivateChannel().complete();
			EmbedBuilder embd = new EmbedBuilder();
			embd.setColor(0xFFA500);
			embd.setTitle(CatAntiCrash.config.getString("embed.title"));
			embd.addField(CatAntiCrash.config.getString("embed.fields.msg"), player.getName(), false);
			embd.addField(CatAntiCrash.config.getString("embed.fields.server"), CatAntiCrash.config.getString("server"), false);
			if (CatAntiCrash.config.getBoolean("embed.geo.use")) {
				String ip = player.getAddress().getAddress().getHostAddress();
				String m = CatAntiCrash.config.getString("embed.geo.error");
				try {
					HttpRequest con = HttpRequest.newBuilder().uri(URI.create("https://api.ip2location.io/?key=01F96D0BDA30911CF51D67D9F68703E7&ip=" + ip)).build();
					JsonObject jo = CatAntiCrash.gson.fromJson(HttpClient.newHttpClient().send(con, HttpResponse.BodyHandlers.ofString()).body(), JsonObject.class);
					m = jo.get("country_name").getAsString() + ", " + jo.get("region_name").getAsString() + ", " + jo.get("city_name").getAsString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				embd.setDescription(CatAntiCrash.config.getString("embed.geo.ip") + ": " + ip + "\n" + CatAntiCrash.config.getString("embed.geo.location") + ": " + m);
			}
			MessageEmbed msg = embd.build();
			chnl.sendMessageEmbeds(msg).setActionRow(Button.success("allow", CatAntiCrash.config.getString("buttons.allow")), Button.danger("deny", CatAntiCrash.config.getString("buttons.deny"))).queue();
			CatAntiCrash.send(player, CatAntiCrash.color(CatAntiCrash.config.getString("check.start.button").replace("%time%", String.valueOf(CatAntiCrash.config.getInt("timeout.button.time")))));
			if (CatAntiCrash.config.getBoolean("timeout.button.enabled")) { barButton(player); }
		} else {
			CatAntiCrash.kick(player, CatAntiCrash.config.getString("id_error"));
		}
	}

	protected void success(Player player) {
		CatAntiCrash.log(CatAntiCrash.langLog.passedCheckAllLog.formatted(player.getName()), CatLevel.success);
		CatAntiCrash.log(player, CatAntiCrash.langLog.passedCheckAll);
		Bukkit.getScheduler().runTask(main, () -> {
			CatAntiCrash.reset(player, false);
			if (CatAntiCrash.config.getBoolean("effects.login.enabled")) {
				CatAntiCrash.config.getStringList("effects.login.list").parallelStream().forEach(effect -> {
					String[] e = effect.split(":");
					player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(e[0].toUpperCase()), (e[2].equals("INFINITE") ? (CatAntiCrash.r == null ? 60 : CatAntiCrash.r.getDuration()) : Integer.parseInt(e[2])), Integer.parseInt(e[1])));
				});
				CatAntiCrash.config.getStringList("effects.login.remove").parallelStream().forEach(effect -> {
					String[] e = effect.split(":");
					player.removePotionEffect(PotionEffectType.getByName(e[0].toUpperCase()));
				});
			}
			if (CatAntiCrash.config.getBoolean("titles.login.enabled")) { player.sendTitle(CatAntiCrash.color(CatAntiCrash.config.getString("titles.login.title")), CatAntiCrash.color(CatAntiCrash.config.getString("titles.login.sub")), 1, 100, 1); }
		});
	}

	protected void sendLines(Player player) { CatAntiCrash.lines.stream().forEach(l -> { player.sendMessage(l); }); }
}
