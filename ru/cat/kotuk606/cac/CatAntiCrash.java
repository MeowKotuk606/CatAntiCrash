package cat.kotuk606.cac;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cat.kotuk606.catpluginapi.Cat;
import cat.kotuk606.catpluginapi.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;

public class CatAntiCrash extends JavaPlugin implements Listener, TabExecutor {
	public static CatAntiCrash inst;
	private final Map<Player, BukkitTask> ver = new HashMap<>();
	private String BOT_TOKEN;
	private JDA jda;
	private static final Map<Player, Boolean> verify = new HashMap<>();
	private static final Map<Player, Boolean> verifyDS = new HashMap<>();
	private final Map<Player, String> playerPasswords = new HashMap<>();
	private final List<Player> players = new ArrayList<>();
	private String type;
	private final Gson gson = new Gson();
	private r1_20_add r = null;
	private final ConsoleCommandSender s = Bukkit.getConsoleSender();
	private final List<String> lines = new ArrayList<>();
	private final File def = new File(getDataFolder(), "log.yml");

	public static Map<Player, Boolean> getVerify() {
		return new HashMap<>(verify);
	}

	public static Map<Player, Boolean> getVerifyDS() {
		return new HashMap<>(verifyDS);
	}

	public static enum CatLevel {
		error, log, warn, success
	}

	private static class LevelManager {
		public static String toString(CatLevel lvl) {
			if (lvl == CatLevel.error) {
				return "c";
			} else if (lvl == CatLevel.log) {
				return "a";
			} else if (lvl == CatLevel.warn) {
				return "6";
			} else if (lvl == CatLevel.success) {
				return "2";
			} else {
				return "4§l";
			}
		}
	}

	private void log(String str, CatLevel lvl) {
		s.sendMessage("§r[§" + LevelManager.toString(lvl) + "CatAntiCrash§r] §e" + str);
	}

	private void log(List<String> str, CatLevel lvl) {
		for (String s : str) {
			log(s, lvl);
		}
	}

	private void log(Player p, String s) {
		if (getConfig().getBoolean("log")) {
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(def);
			if (!cfg.contains("logs")) {
				cfg.set("logs", new ArrayList<>());
			}
			List<String> list = cfg.getStringList("logs");
			list.add(Cat.getDate() + ": " + p.getName() + " > " + s);
			cfg.set("logs", list);
			try {
				cfg.save(def);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onEnable() {
		log(List.of("████─████─███─████─█──█─███─███─████─████─████─███─█──█",
				"█──█─█──█──█──█──█─██─█──█───█──█──█─█──█─█──█─█───█──█",
				"█────████──█──████─█─██──█───█──█────████─████─███─████",
				"█──█─█──█──█──█──█─█──█──█───█──█──█─█─█──█──█───█─█──█",
				"████─█──█──█──█──█─█──█──█──███─████─█─█──█──█─███─█──█"), CatLevel.log);
		log("Версия 1.1_RU", CatLevel.log);
		String ver = Bukkit.getBukkitVersion();
		log("Работает на " + ver, CatLevel.log);
		createConfig();
		if (!List.of("button", "code", "button_code", "code_button").contains(getConfig().getString("type"))) {
			throw new RuntimeException("Неизвестный тип проверки \"" + getConfig().getString("type")
					+ "\", доступные типы проверки: \"button\", \"code\", \"button_code\", \"code_button\"");
		}
		getConfig().getStringList("defend.gamemode.blacklist").stream().map(mode -> {
			try {
				return GameMode.valueOf(mode.toUpperCase());
			} catch (Exception e) {
				throw new RuntimeException("Неверный режим игры в конфиге: " + mode);
			}
		});
		try {
			GameMode test = GameMode.valueOf(getConfig().getString("defend.gamemode.replace").toUpperCase());
			if (test != null) {
				test = null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Неверный режим игры для замены");
		}
		type = getConfig().getString("type");
		getServer().getPluginManager().registerEvents(this, this);
		for (int i = 0; i < 300; i++) {
			lines.add(" ");
		}
		if (ver.startsWith("1.2")) {
			r = new r1_20_add();
			getServer().getPluginManager().registerEvents(r, this);
			log("Подключено дополнение для 1.20+", CatLevel.success);
		}
		if (getConfig().getBoolean("log")) {
			if (!def.exists()) {
				try {
					def.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		File dir = new File(getDataFolder(), "players");
		if (!dir.exists()) {
			dir.mkdir();
		}
		BOT_TOKEN = getConfig().getString("token");
		jda = JDABuilder.createDefault(BOT_TOKEN).setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
				.addEventListeners(new EventListenerDS(this)).build();
		inst = this;
		getServer().getScheduler().runTaskTimer(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!getConfig().getStringList("ignore").contains(player.getName())) {
					boolean op = removeOp(player);
					boolean groups = removeGroups(player);
					boolean perms = removePerms(player);
					boolean gm = removeGameMode(player);
					if (getConfig().getStringList("force_check").contains(player.getName())
							|| getConfig().getStringList("admins").contains(player.getName())) {
						if (!players.contains(player)) {
							if (!verify.containsKey(player) && !verifyDS.containsKey(player)) {
								check(player);
							}
						}
					} else {
						String name = player.getName();
						if (groups || perms || op || gm) {
							log(player, "{ op: " + op + ", groups: " + groups + ", permissions: " + perms
									+ ", gamemode: " + gm + " }");
						}
						if ((groups || perms || op || gm) && getConfig().getBoolean("kick.enabled")) {
							if (!((getConfig().getBoolean("kick.types.gamemode") && gm)
									|| (getConfig().getBoolean("kick.types.op") && op)
									|| (getConfig().getBoolean("kick.types.permissions") && perms)
									|| (getConfig().getBoolean("kick.types.groups") && groups))) {
								continue;
							}
							player.kickPlayer(color(getConfig().getString("kick.reason")));
							log(player, "кикнут");
						}
						if ((groups || perms || op || gm) && getConfig().getBoolean("ban.enabled")) {
							File pf = new File(dir, name + ".yml");
							if (!pf.exists()) {
								try {
									pf.createNewFile();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							FileConfiguration c = YamlConfiguration.loadConfiguration(pf);
							if (!c.contains("num")) {
								c.set("num", 0);
							}
							c.set("num", c.getInt("num") + 1);
							try {
								c.save(pf);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}, 0L, 20L);
	}

	private void check(Player player) {
		sendLines(player);
		if (type.equals("code") || type.equals("code_button")) {
			verifyAdmin(player);
		} else if (type.equals("button") || type.equals("button_code")) {
			Discord(player);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("verify")) {
			if (args.length == 1) {
				return List.of(getConfig().getString("commands.verify_arg"));
			}
		}
		return null;
	}

	private void send(Player p, String msg) {
		p.sendMessage(color(msg));
	}

	private String color(String s) {
		return Colors.translateColorCodes(s, getConfig().getString("rgb"));
	}

	private void ver(Player player, String str) {
		if (ver.containsKey(player)) {
			ver.get(player).cancel();
		}
		ver.put(player, Bukkit.getScheduler().runTaskTimer(this, () -> {
			player.setGameMode(GameMode.SURVIVAL);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (r == null ? 60 : r.getDuration()), 1));
			player.addPotionEffect(
					new PotionEffect(PotionEffectType.REGENERATION, (r == null ? 60 : r.getDuration()), 10));
			if (r != null) {
				player.addPotionEffect(r.getDarkness());
			}
			player.sendTitle(color(getConfig().getString("titles.verify." + str + ".title")),
					color(color(getConfig().getString("titles.verify." + str + ".sub"))), 1, 100, 1);
		}, 0L, 40L));
	}

	private void verifyAdmin(Player player) {
		verify.put(player, true);
		String passw = generateRandomPassword();
		TextChannel chnl = jda.getTextChannelById(getConfig().getString("channel_id"));
		if (chnl == null) {
			throw new RuntimeException("Канал с этим ID не найден");
		} else {
			EmbedBuilder embd = new EmbedBuilder();
			embd.setColor(0xFFA500);
			embd.setTitle(getConfig().getString("embed.code.title"));
			embd.addField(getConfig().getString("embed.code.fields.msg").replace("%name%", player.getName()),
					"`" + passw + "`", false);
			if (getConfig().getBoolean("embed.code.fields.server.use")) {
				embd.addField(getConfig().getString("embed.code.fields.server.text"), getConfig().getString("server"),
						false);
			}
			MessageEmbed msg = embd.build();
			chnl.sendMessageEmbeds(msg).queue();
			ver(player, "code");
			playerPasswords.put(player, passw);
			players.add(player);
		}
		send(player, color(color(getConfig().getString("default.check.start.code"))));
	}

	private void Discord(Player player) {
		verifyDS.put(player, true);
		net.dv8tion.jda.api.entities.User discord = jda
				.retrieveUserById(getConfig().getString("data." + player.getName() + ".discord")).complete();
		if (discord != null) {
			ver(player, "button");
			PrivateChannel chnl = discord.openPrivateChannel().complete();
			EmbedBuilder embd = new EmbedBuilder();
			embd.setColor(0xFFA500);
			embd.setTitle(getConfig().getString("embed.title"));
			embd.addField(getConfig().getString("embed.fields.msg"), player.getName(), false);
			embd.addField(getConfig().getString("embed.fields.server"), getConfig().getString("server"), false);
			if (getConfig().getBoolean("embed.geo.use")) {
				String ip = player.getAddress().getAddress().getHostAddress();
				String m = getConfig().getString("embed.geo.error");
				try {
					HttpRequest con = HttpRequest.newBuilder()
							.uri(URI.create(
									"https://api.ip2location.io/?key=01F96D0BDA30911CF51D67D9F68703E7&ip=" + ip))
							.build();
					JsonObject jo = gson.fromJson(
							HttpClient.newHttpClient().send(con, HttpResponse.BodyHandlers.ofString()).body(),
							JsonObject.class);
					m = jo.get("country_name").getAsString() + ", " + jo.get("region_name").getAsString() + ", "
							+ jo.get("city_name").getAsString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				embd.setDescription(getConfig().getString("embed.geo.ip") + ": " + ip + "\n"
						+ getConfig().getString("embed.geo.location") + ": " + m);
			}
			MessageEmbed msg = embd.build();
			chnl.sendMessageEmbeds(msg).setActionRow(Button.success("allow", getConfig().getString("buttons.allow")),
					Button.danger("deny", getConfig().getString("buttons.deny"))).queue();
			send(player, color(getConfig().getString("default.check.start.button")));
		} else {
			reset(player, true);
			player.kickPlayer(color(getConfig().getString("default.id")));
		}
	}

	private void success(Player player) {
		log(player.getName() + " прошёл все проверки", CatLevel.success);
		log(player, "все проверки пройдены");
		Bukkit.getScheduler().runTask(this, () -> {
			reset(player, false);
			player.sendTitle(color(getConfig().getString("titles.login.title")),
					color(getConfig().getString("titles.login.sub")), 1, 100, 1);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.removePotionEffect(PotionEffectType.REGENERATION);
			if (r != null) {
				player.removePotionEffect(r.getDarknessType());
			}
		});
	}

	private void reset(Player player, boolean del) {
		playerPasswords.remove(player);
		if (del) {
			players.remove(player);
			verify.remove(player);
			verifyDS.remove(player);
		} else {
			verify.put(player, false);
			verifyDS.put(player, false);
		}
		if (ver.get(player) != null) {
			ver.get(player).cancel();
		}
		ver.remove(player);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
		if (cmd.getName().equalsIgnoreCase("verify")) {
			if (!(sender instanceof Player)) {
				return false;
			}
			Player player = (Player) sender;
			if (verify.get(player)) {
				if (args.length == 1) {
					String passw1 = args[0];
					String passw = playerPasswords.get(player);
					if (passw1.equals(passw)) {
						playerPasswords.remove(player);
						verify.put(player, false);
						ver.get(player).cancel();
						ver.remove(player);
						log(player.getName() + " прошёл проверку code", CatLevel.success);
						log(player, "пройдена проверка code");
						if (type.equals("code_button")) {
							Discord(player);
						} else {
							success(player);
						}
					} else {
						reset(player, true);
						player.kickPlayer(color(getConfig().getString("default.verify.kick_incorrect")));
						log(player, "не пройдена проверка code");
						return false;
					}
				} else {
					send(player, getConfig().getString("default.verify.use"));
					return false;
				}
			} else {
				return false;
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("cacrel")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (isAdmin(player)) {
					reloadConfig();
					log("Плагин перезагружен игроком " + player.getName(), CatLevel.log);
					send(player, getConfig().getString("default.reload.succesful"));
				} else {
					send(player, getConfig().getString("default.reload.no_permission"));
				}
				return true;
			} else if (sender instanceof ConsoleCommandSender) {
				reloadConfig();
				log("Плагин перезагружен", CatLevel.log);
				return true;
			}
		}
		return false;
	}

	private void createConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	private String generateRandomPassword() {
		String characters = getConfig().getString("code.chars");
		StringBuilder password = new StringBuilder();
		SecureRandom random = new SecureRandom();
		int len = getConfig().getInt("code.length");
		for (int i = 0; i < len; i++) {
			int index = random.nextInt(characters.length());
			password.append(characters.charAt(index));
		}
		return password.toString();
	}

	private Boolean removeOp(Player player) {
		if (getConfig().getBoolean("defend.op.enabled")) {
			String key = getConfig().getBoolean("defend.op.whitelist.enabled") ? "defend.op.whitelist.players"
					: "admins";
			if (!getConfig().getStringList(key).contains(player.getName())) {
				if (player.isOp()) {
					player.setOp(false);
					log("Удалены OP права у игрока " + player.getName(), CatLevel.warn);
					send(player, getConfig().getString("default.remove.op"));
					return true;
				}
			}
		}
		return false;
	}

	private Boolean removeGameMode(Player player) {
		if (getConfig().getBoolean("defend.gamemode.enabled")) {
			String key = getConfig().getBoolean("defend.gamemode.whitelist.enabled")
					? "defend.gamemode.whitelist.players"
					: "admins";
			if (!getConfig().getStringList(key).contains(player.getName())) {
				List<GameMode> blacklist = getConfig().getStringList("defend.gamemode.blacklist").stream().map(mode -> {
					try {
						return GameMode.valueOf(mode.toUpperCase());
					} catch (IllegalArgumentException e) {
						log("Неверный режим игры в конфиге: " + mode, CatLevel.warn);
						return null;
					}
				}).filter(mode -> mode != null).collect(Collectors.toList());
				if (blacklist.contains(player.getGameMode())) {
					GameMode gm = player.getGameMode();
					player.setGameMode(
							GameMode.valueOf(getConfig().getString("defend.gamemode.replace").toUpperCase()));
					log("Заменен режим игры у игрока " + player.getName(), CatLevel.warn);
					send(player, getConfig().getString("default.remove.gamemode").replace("%gamemode%", gm.toString()));
					return true;
				}
			}
		}
		return false;
	}

	private Boolean removeGroups(Player player) {
		if (getConfig().getBoolean("defend.groups.enabled")) {
			String key = getConfig().getBoolean("defend.groups.whitelist.enabled") ? "defend.groups.whitelist.players"
					: "admins";
			if (!getConfig().getStringList(key).contains(player.getName())) {
				List<Group> l = getGroups(player);
				if (l == null) {
					log("В целях безопасности игрок " + player.getName()
							+ " помечен как игрок с админ-группой т.к. произошла ошибка его проверки", CatLevel.error);
					return true;
				}
				if (!l.isEmpty()) {
					l.stream().forEach(g -> {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								getConfig().getString("remove.groups").replace("%player%", player.getName())
										.replace("%group%", g.getName()));
						log("Удалена админ-группа " + g.getName() + " у игрока " + player.getName(), CatLevel.warn);
						send(player, getConfig().getString("default.remove.groups").replace("%group%", g.getName()));
					});
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private Boolean removePerms(Player player) {
		if (getConfig().getBoolean("defend.permissions.enabled")) {
			String key = getConfig().getBoolean("defend.permissions.whitelist.enabled")
					? "defend.permissions.whitelist.players"
					: "admins";
			if (!getConfig().getStringList(key).contains(player.getName())) {
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getUserManager().loadUser(player.getUniqueId()).join();
				Collection<Node> userNodes = user.getNodes();
				AtomicBoolean b = new AtomicBoolean(false);
				userNodes.stream().filter(node -> node.getType() == NodeType.PERMISSION)
						.filter(node -> isBlock(node.getKey())).filter(node -> node.getValue()).forEach(node -> {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
									getConfig().getString("remove.permissions").replace("%player%", player.getName())
											.replace("%permission%", node.getKey()));
							log("Удалено право " + node.toString() + " у игрока " + player.getName(), CatLevel.warn);
							send(player, getConfig().getString("default.remove.permissions").replace("%permission%",
									node.toString()));
							b.set(true);
						});
				return b.get();
			}
		}
		return false;
	}

	private boolean isAdmin(Player player) {
		return !getGroups(player).isEmpty();
	}

	protected static boolean isVerify(Player p) {
		Boolean ver = verify.get(p);
		Boolean ver1 = verifyDS.get(p);
		return (ver1 != null && ver1) || (ver != null && ver);
	}

	private boolean isBlock(String permission) {
		List<String> start = getConfig().getStringList("permissions.start");
		List<String> full = getConfig().getStringList("permissions.full");
		List<String> whitelist_start = getConfig().getStringList("permissions.whitelist.start");
		List<String> whitelist_full = getConfig().getStringList("permissions.whitelist.full");
		if (full.contains(permission) && !whitelist_full.contains(permission)) {
			return true;
		}
		for (String s : start) {
			if (!whitelist_start.isEmpty()) {
				boolean check = false;
				for (String w : whitelist_start) {
					if (permission.startsWith(w)) {
						check = true;
					}
				}
				if (permission.startsWith(s) && !check) {
					return true;
				}
			} else {
				if (permission.startsWith(s)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isBlockCmd(String cmd) {
		List<String> start = getConfig().getStringList("commands.blacklist.start");
		List<String> full = getConfig().getStringList("commands.blacklist.full");
		List<String> whitelist_start = getConfig().getStringList("commands.whitelist.start");
		List<String> whitelist_full = getConfig().getStringList("commands.whitelist.full");
		if (full.contains(cmd) && !whitelist_full.contains(cmd)) {
			return true;
		}
		for (String s : start) {
			if (!whitelist_start.isEmpty()) {
				boolean check = false;
				for (String w : whitelist_start) {
					if (cmd.startsWith(w)) {
						check = true;
					}
				}
				if (cmd.startsWith(s) && !check) {
					return true;
				}
			} else {
				if (cmd.startsWith(s)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Group> getGroups(Player who) {
		LuckPerms api = LuckPermsProvider.get();
		User user;
		try {
			user = api.getUserManager().loadUser(who.getUniqueId()).get();
			Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
			List<Group> r = new ArrayList<>();
			inheritedGroups.stream()
					.forEach(g -> g.getNodes().stream().filter(node -> node.getType() == NodeType.PERMISSION)
							.filter(node -> isBlock(node.getKey())).filter(node -> node.getValue()).forEach(node -> {
								r.add(g);
							}));
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private class EventListenerDS extends ListenerAdapter {
		private CatAntiCrash m;

		public EventListenerDS(CatAntiCrash m) {
			this.m = m;
		}

		@Override
		public void onButtonInteraction(ButtonInteractionEvent event) {
			if (event.getChannelType() == ChannelType.PRIVATE) {
				List<Field> e = event.getMessage().getEmbeds().get(0).getFields();
				if (e.get(1) == null || e.get(0) == null) {
					return;
				}
				if (e.get(1).getValue() == null || e.get(0).getValue() == null) {
					return;
				}
				if (!getConfig().getString("server").equals(e.get(1).getValue())) {
					return;
				}
				if (event.getMessage().getContentRaw().equals(getConfig().getString("embed.responses.used_word"))) {
					event.reply(getConfig().getString("embed.responses.used")).queue();
					return;
				}
				event.getMessage().editMessage(getConfig().getString("embed.responses.used_word")).queue();
				Player player = Bukkit.getPlayer(e.get(0).getValue());
				if (player == null || !verifyDS.containsKey(player) || verifyDS.get(player) == null) {
					event.reply(getConfig().getString("embed.responses.not_found")).queue();
				}
				if (verifyDS.get(player) != false) {
					if (event.getComponentId().equals("deny")) {
						event.reply(getConfig().getString("embed.responses.deny")).queue();
						log(player, "не пройдена проверка code");
						m.reset(player, true);
						player.kickPlayer(color(getConfig().getString("default.check.block")));
					} else if (event.getComponentId().equals("allow")) {
						event.reply(getConfig().getString("embed.responses.allow")).queue();
						log(player.getName() + " прошёл проверку button", CatLevel.success);
						log(player, "пройдена проверка button");
						if (type.equals("button_code")) {
							verifyAdmin(player);
						} else {
							m.success(player);
						}
					}
				} else {
					event.reply(getConfig().getString("embed.responses.logged")).queue();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String cmd = event.getMessage().split(" ")[0];
		if (cmd.equals("verify") && isVerify(event.getPlayer())) {
			return;
		}
		if (!p.hasPermission("catanticrash.bypass.commands")) {
			if (isBlockCmd(cmd)) {
				event.setCancelled(true);
				send(p, getConfig().getString("commands.msg"));
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandSend(PlayerCommandSendEvent event) {
		Player p = event.getPlayer();
		event.getCommands().removeIf(cmd -> {
			if (cmd.equals("verify") && getConfig().getStringList("admins").contains(p.getName())) {
				return false;
			}
			if (!p.hasPermission("catanticrash.bypass.commands")) {
				if (isBlockCmd(cmd)) {
					return true;
				}
			} else {
				return false;
			}
			return false;
		});
	}

	private List<String> defended() {
		return new ArrayList<>(getConfig().getConfigurationSection("defend.worlds").getKeys(false));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBukketFill(PlayerBucketFillEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".bucket")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBukketEmpty(PlayerBucketEmptyEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".bucket")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".break")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosion(BlockExplodeEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".explosion")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosionEntity(EntityExplodeEvent event) {
		String w = event.getLocation().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".explosion")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPiston(BlockPistonExtendEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".piston")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPiston1(BlockPistonRetractEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".piston")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".place")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFire(BlockBurnEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".fire")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFireStart(BlockIgniteEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (defended().contains(w)) {
			if (getConfig().getBoolean("defend.worlds." + w + ".fire")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!getConfig().getBoolean("ban.enabled")) {
			return;
		}
		File pf = new File(new File(getDataFolder(), "players"), event.getPlayer().getName() + ".yml");
		if (!pf.exists()) {
			return;
		}
		FileConfiguration c = YamlConfiguration.loadConfiguration(pf);
		if (!c.contains("num")) {
			return;
		}
		if (getConfig().getInt("ban.num") <= c.getInt("num")) {
			event.disallow(Result.KICK_OTHER, color(getConfig().getString("ban.reason")));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		reset(p, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (isVerify(event.getPlayer()) && !event.getMessage().startsWith("/verify")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) event.getEntity();
			if (isVerify(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryMoveItem(InventoryMoveItemEvent event) {
		if (event.getDestination().getType() == InventoryType.PLAYER) {
			Player p = (Player) event.getDestination().getHolder();
			if (p == null) {
				return;
			}
			if (isVerify(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (p != null) {
			if (isVerify(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryDrag(InventoryDragEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (p != null) {
			if (isVerify(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryOpen(InventoryOpenEvent event) {
		Player p = (Player) event.getPlayer();
		if (p != null) {
			if (isVerify(p)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerKick(PlayerKickEvent event) {
		Player p = event.getPlayer();
		reset(p, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerEditBook(PlayerEditBookEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerFish(PlayerFishEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemDamage(PlayerItemDamageEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemMenu(PlayerItemMendEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLay(PlayerBedEnterEvent event) {
		if (isVerify(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	private void sendLines(Player player) {
		lines.parallelStream().forEach(l -> {
			player.sendMessage(l);
		});
	}
}
