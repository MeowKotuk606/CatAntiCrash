package cat.meowkotuk606.anticrash;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;

import cat.meowkotuk606.anticrash.CatListener.EventListenerDS;
import cat.meowkotuk606.anticrash.Lang.EnLang;
import cat.meowkotuk606.anticrash.Lang.RuLang;
import cat.meowkotuk606.anticrash.Objects.VerifyStatus;
import cat.meowkotuk606.anticrash.stats.Metrics;
import cat.meowkotuk606.anticrash.stats.Metrics.SimplePie;
import cat.meowkotuk606.lib.Cat;
import cat.meowkotuk606.lib.Colors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;

public class CatAntiCrash extends JavaPlugin {
	protected static final Map<Player, VerifyStatus> v = new HashMap<>();
	protected static JDA jda;
	protected static final Gson gson = new Gson();
	protected static r1_20 r = null;
	protected static r1_13 r1 = null;
	protected static r1_20_1 r2 = null;
	protected static final ConsoleCommandSender s = Bukkit.getConsoleSender();
	protected static final List<String> lines = new ArrayList<>();
	protected static File def;
	protected static Metrics metrics;
	protected static final Set<String> langs = Set.of("en", "ru");
	protected static YamlConfiguration config;
	protected static YamlConfiguration settings;
	protected static LuckPerms lp;
	protected static File dir;
	protected static final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
	protected static ProtocolManager plib;
	protected static USP usp = null;
	protected static Utils utils;
	protected static VerifyManager manage;
	protected static CatListener listener;
	protected static File dataFold;
	protected static Map<Integer, String> checkInt = new HashMap<>();
	protected static Lang langLog;
	private static CatAntiCrash inst;

	protected enum CatLevel {
		error, log, warn, success;

		public String toString() {
			if (this == error) return "c";
			if (this == log) return "a";
			if (this == warn) return "6";
			if (this == success) return "2";
			return "4§lUnkownFormat_";
		}
	}

	protected static void log(String str, CatLevel lvl) { s.sendMessage("§r[§" + lvl.toString() + "CatAntiCrash§r] §e" + str); }

	protected static void log(Player p, String s) {
		if (config.getBoolean("log.enabled")) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(def);
			if (!cfg.contains("logs")) { cfg.set("logs", new ArrayList<>()); }
			List<String> list = cfg.getStringList("logs");
			list.add("[" + Cat.getDate1(config.getString("time_zone")) + " | " + Cat.getDate2(config.getString("time_zone")) + "]: " + (p == null ? "null" : p.getName()) + " > " + s);
			cfg.set("logs", list);
			try {
				cfg.save(def);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected static void createConfig() {
		for (String lang : langs) {
			File f = new File(dataFold, "configs");
			if (!f.exists()) { f.mkdirs(); }
			File lf = new File(f, "config_" + lang + ".yml");
			YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(lf);
			YamlConfiguration defaultConfig = new YamlConfiguration();
			try (InputStream inputStream = inst.getResource("config_" + lang + ".yml")) {
				defaultConfig.loadFromString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean updated = false;
			for (String key : defaultConfig.getKeys(true)) {
				if (!existingConfig.contains(key)) {
					existingConfig.set(key, defaultConfig.get(key));
					updated = true;
				}
			}
			if (!lf.exists() || updated) {
				try {
					existingConfig.save(lf);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		File settingsFile = new File(dataFold, "settings.yml");
		if (!settingsFile.exists()) { inst.saveResource("settings.yml", true); }
		settings = YamlConfiguration.loadConfiguration(settingsFile);
		String lang = settings.getString("config_lang");
		if (lang == null) { stop("\"null\" is not \"en\" or \"ru\""); }
		if (!langs.contains(lang)) { stop("\"" + settings.getString("config_lang") + "\" is not \"en\" or \"ru\""); }
		// I: 37 (34)
		if (lang.equals("ru")) {
			langLog = new RuLang();
		} else if (lang.equals("en")) langLog = new EnLang();
		config = YamlConfiguration.loadConfiguration(new File(new File(dataFold, "configs"), "config_" + lang + ".yml"));
	}

	protected static void stop(String r) { throw new RuntimeException(r); }

	protected static String generateCode() {
		String chars = config.getString("code.chars");
		StringBuilder code = new StringBuilder();
		SecureRandom random = new SecureRandom();
		int len = config.getInt("code.length");
		for (int i = 0; i < len; i++) {
			int index = random.nextInt(chars.length());
			code.append(chars.charAt(index));
		}
		return code.toString();
	}

	public void onDisable() { es.shutdownNow(); }

	public void onEnable() {
		if (Cat.getUpdate() != 2) { stop("Unsupported CatLib version detected. This plugin requires CatLib version 2.1. Please download version 2.1 from: https://github.com/MeowKotuk606/CatLib/releases/"); }
		inst = this;
		utils = new Utils();
		dataFold = getDataFolder();
		manage = new VerifyManager(this);
		dir = new File(dataFold, "players");
		def = new File(dataFold, "log.yml");
		createConfig();
		plib = ProtocolLibrary.getProtocolManager();
		int size = config.getStringList("types").size();
		if (size > 3 || size < 1) { stop(langLog.checksCount); }
		checkInt.put(0, config.getStringList("types").get(0));
		if (size >= 2) {
			checkInt.put(1, config.getStringList("types").get(1));
			if (size == 3) {
				checkInt.put(2, config.getStringList("types").get(2));
			} else {
				checkInt.put(2, "none");
			}
		} else {
			checkInt.put(1, "none");
			checkInt.put(2, "none");
		}
		Set<String> checkTypes = Set.of("button", "code", "password");
		for (String check : checkInt.values()) { if (!checkTypes.contains(check)) { stop(langLog.undefinedType.formatted(check)); } }
		config.getStringList("defend.gamemode.blacklist").stream().map(mode -> {
			try {
				return GameMode.valueOf(mode.toUpperCase());
			} catch (Exception e) {
				throw new RuntimeException(langLog.undefinedGM.formatted(mode));
			}
		});
		try {
			GameMode test = GameMode.valueOf(config.getString("defend.gamemode.replace").toUpperCase());
			if (test != null) { test = null; }
		} catch (Exception e) {
			stop(langLog.undefinedGMRepl);
		}
		if (config.getBoolean("log")) {
			if (!def.exists()) {
				try {
					def.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!dir.exists()) { dir.mkdir(); }
		metrics = new Metrics(this, 22483);
		metrics.addCustomChart(new SimplePie("version_lang", () -> { return settings.getString("config_lang"); }));
		for (String st : List.of("████─████─███─████─█──█─███─███─████─████─████─███─█──█", "█──█─█──█──█──█──█─██─█──█───█──█──█─█──█─█──█─█───█──█", "█────████──█──████─█─██──█───█──█────████─████─███─████",
			"█──█─█──█──█──█──█─█──█──█───█──█──█─█─█──█──█───█─█──█", "████─█──█──█──█──█─█──█──█──███─████─█─█──█──█─███─█──█")) {
			s.sendMessage("§r[§aCatAntiCrash§r] §e" + st);
		}
		log(langLog.version.formatted(this.getDescription().getVersion()), CatLevel.log);
		String ver = Bukkit.getBukkitVersion();
		for (int i = 0; i < 300; i++) { lines.add(" "); }
		lp = LuckPermsProvider.get();
		if (Bukkit.getPluginManager().isPluginEnabled("UltimateServerProtector")) {
			log(langLog.USP, CatLevel.success);
			usp = new USP();
		}
		plib.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Client.TAB_COMPLETE) {
			@Override
			public void onPacketReceiving(PacketEvent event) { CatListener.onPacket(event); }
		});
		listener = new CatListener(this);
		double dVer = ver(ver);
		if (dVer >= 113) {
			r1 = new r1_13(this);
			log(langLog.addonConnected.formatted("1.13+"), CatLevel.success);
			if (dVer >= 120) {
				r = new r1_20();
				log(langLog.addonConnected.formatted("1.20+"), CatLevel.success);
				if (dVer >= 120.1) {
					r2 = new r1_20_1(this);
					log(langLog.addonConnected.formatted("1.20.1+"), CatLevel.success);
				}
			}
		}
		getCommand("verify").setExecutor(listener);
		getCommand("cacrel").setExecutor(listener);
		jda = JDABuilder.createDefault(config.getString("token")).setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).addEventListeners(new EventListenerDS(this)).build();
		getServer().getScheduler().runTaskTimer(this, () -> {
			if (config.getBoolean("defend.groups.settings.permanent_permissions.enabled")) {
				Set<Node> toRemove = new HashSet<>();
				config.getConfigurationSection("defend.groups.settings.permanent_permissions.settings").getKeys(false).parallelStream().forEach(g -> {
					Set<String> pb = new HashSet<>(config.getStringList("defend.groups.settings.permanent_permissions." + g + ".blacklist"));
					Set<String> pwt = new HashSet<>(config.getStringList("defend.groups.settings.permanent_permissions." + g + ".whitelist"));
					Group gr = lp.getGroupManager().getGroup(g);
					if (gr != null) {
						gr.getNodes().stream().filter(node -> node.getValue() && !pwt.contains(node.getKey()) && pb.contains(node.getKey())).forEach(node -> {
							boolean block = config.getBoolean("defend.groups.settings.permanent_permissions." + g + ".blacklist_perms") ? isBlock(node) : true;
							if (block) { toRemove.add(node); }
						});
						if (!toRemove.isEmpty()) {
							toRemove.parallelStream().forEach(node -> {
								gr.data().remove(node);
								log(CatAntiCrash.langLog.removePermGroup.formatted(node.getKey(), g), CatLevel.warn);
								gr.data().add(node.toBuilder().value(false).build());
							});
							lp.getGroupManager().saveGroup(gr);
						}
					}
				});
			}
		}, 0L, 20L);
		getServer().getScheduler().runTaskTimer(this, () -> {
			Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
				if (!config.getStringList("ignore").contains(player.getName())) {
					boolean op = utils.removeOp(player);
					boolean groups = utils.removeGroups(player);
					boolean perms = utils.removePerms(player);
					boolean gm = utils.removeGameMode(player);
					if (config.getStringList("force_check").contains(player.getName()) || config.getConfigurationSection("admins").getKeys(false).contains(player.getName())) {
						boolean u = (usp == null ? true : usp.logged(player));
						if (!v.containsKey(player) && u) { manage.check(player); }
						return;
					}
					if (groups || perms || op || gm) { if (config.getBoolean("log.types.defend")) { log(player, "{ op: " + op + ", groups: " + groups + ", permissions: " + perms + ", gamemode: " + gm + " }"); } }
					if ((groups || perms || op || gm) && config.getBoolean("kick.enabled")) {
						if (((config.getBoolean("kick.types.gamemode") && gm) || (config.getBoolean("kick.types.op") && op) || (config.getBoolean("kick.types.permissions") && perms) || (config.getBoolean("kick.types.groups") && groups))) {
							kick(player, config.getString("kick.reason"));
							if (config.getBoolean("log.types.kick")) { log(player, langLog.kicked); }
						}
					}
					if ((groups || perms || op || gm) && config.getBoolean("ban.enabled")) {
						if (((config.getBoolean("ban.types.gamemode") && gm) || (config.getBoolean("ban.types.op") && op) || (config.getBoolean("ban.types.permissions") && perms) || (config.getBoolean("ban.types.groups") && groups))) {
							banAdd(player);
						}
					}
				}
			});
		}, 0L, 20L);
	}

	private static double ver(String ver) {
		String res = ver.substring(0, 4).replace(".", "");
		if (ver.length() >= 6) {
			char c = ver.charAt(5);
			if (Character.isDigit(c)) { res = res.concat("." + c); }
		}
		return Double.parseDouble(res);
	}

	protected static void send(Player p, String msg) { p.sendMessage(color(msg)); }

	protected static String color(String s) { return Colors.translateColorCodes(s, config.getString("rgb")); }

	protected static void reset(Player player, boolean del) {
		if (v.get(player).tsk != null && !v.get(player).tsk.isCancelled()) { v.get(player).tsk.cancel(); }
		v.get(player).bossbarCode.v1.removePlayer(player);
		v.get(player).bossbarButton.v1.removePlayer(player);
		if (v.get(player).bossbarCode.v2 != null && !v.get(player).bossbarCode.v2.isCancelled()) { v.get(player).bossbarCode.v2.cancel(); }
		if (v.get(player).bossbarButton.v2 != null && !v.get(player).bossbarButton.v2.isCancelled()) { v.get(player).bossbarButton.v2.cancel(); }
		if (del) { v.remove(player); }
		Set<PotionEffectType> e = player.getActivePotionEffects().stream().map(eff -> { return eff.getType(); }).collect(Collectors.toSet());
		e.stream().forEach(type -> { player.removePotionEffect(type); });
	}

	protected static boolean isAdmin(Player player) { return !getGroups(player).isEmpty(); }

	protected static boolean isBlock(Node node) {
		if (node.getType() != NodeType.PERMISSION || !node.getValue()) { return false; }
		String p = node.getKey();
		Set<String> start = new HashSet<>(config.getStringList("permissions.start"));
		Set<String> full = new HashSet<>(config.getStringList("permissions.full"));
		Set<String> whitelist_start = new HashSet<>(config.getStringList("permissions.whitelist.start"));
		Set<String> whitelist_full = new HashSet<>(config.getStringList("permissions.whitelist.full"));
		if (full.contains(p) && !whitelist_full.contains(p)) { return true; }
		for (String s : start) {
			if (!whitelist_start.isEmpty()) {
				boolean check = false;
				for (String w : whitelist_start) { if (p.startsWith(w)) { check = true; } }
				if (p.startsWith(s) && !check) { return true; }
			} else {
				if (p.startsWith(s)) { return true; }
			}
		}
		return false;
	}

	protected static boolean isBlockCmd(String cmd, boolean isCheck) {
		Set<String> start = new HashSet<>(config.getStringList("commands.blacklist.start"));
		Set<String> full = new HashSet<>(config.getStringList("commands.blacklist.full"));
		Set<String> whitelist_start = new HashSet<>(config.getStringList("commands.whitelist.start"));
		Set<String> whitelist_full = new HashSet<>(config.getStringList("commands.whitelist.full"));
		if (isCheck) {
			whitelist_start.addAll(config.getStringList("commands.whitelist.check.start"));
			whitelist_full.addAll(config.getStringList("commands.whitelist.check.full"));
		}
		if (full.contains(cmd) && !whitelist_full.contains(cmd)) { return true; }
		for (String s : start) {
			if (!whitelist_start.isEmpty()) {
				boolean check = false;
				for (String w : whitelist_start) { if (cmd.startsWith(w)) { check = true; } }
				if (cmd.startsWith(s) && !check) { return true; }
			} else {
				if (cmd.startsWith(s)) { return true; }
			}
		}
		return false;
	}

	protected static Set<Group> getGroups(Player who) {
		User user;
		try {
			user = lp.getUserManager().loadUser(who.getUniqueId()).get();
			Collection<Group> gs = user.getInheritedGroups(user.getQueryOptions());
			Set<Group> r = new HashSet<>();
			Set<String> w = new HashSet<>(config.getStringList("defend.groups.settings.whitelist"));
			Set<String> b = new HashSet<>(config.getStringList("defend.groups.settings.blacklist"));
			boolean ups = config.getBoolean("defend.groups.settings.use_permissions_settings");
			gs.stream().filter(g -> (b.contains(g.getName()) && !w.contains(g.getName())) || (ups && g.getNodes().stream().anyMatch(node -> isBlock(node) && !w.contains(g.getName())))).forEach(r::add);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static Set<String> defended() { return config.getConfigurationSection("defend.worlds").getKeys(false); }

	protected static boolean blocked(String type, String key) {
		Set<String> set = config.getStringList(key).stream().map(String::toLowerCase).collect(Collectors.toSet());
		if (set.contains("!" + type)) { return false; }
		if (set.contains(type) || set.contains("*")) { return true; }
		return false;
	}

	protected static int packetCheck(PacketContainer packet) {
		try {
			return packet.getEnumModifier(InventoryClickType.class, 5).read(0).ordinal();
		} catch (Exception e1) {
			try {
				int modeInt = packet.getIntegers().read(3);
				return InventoryClickType.values()[modeInt].ordinal();
			} catch (Exception e2) {
				List<Object> allObjects = packet.getModifier().getValues();
				for (Object obj : allObjects) { if (obj instanceof Enum) { return InventoryClickType.name(((Enum<?>) obj).name()).ordinal(); } }
				return InventoryClickType.QUICK_MOVE.ordinal();
			}
		}
	}

	protected static void packet(Player player, PacketEvent e) {
		if (player != null) {
			boolean a = config.getBoolean("defend.packets.whitelist.admins") ? !config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!config.getStringList("defend.packets.whitelist.players").contains(player.getName()) && a) {
				e.setCancelled(true);
				log(langLog.exploitLog.formatted(player.getName()), CatLevel.warn);
				if (config.getBoolean("log.types.defend")) { log(player, langLog.exploit); }
				if (config.getBoolean("kick.packets.enabled")) { kick(player, config.getString("kick.packets.reason")); }
				if (config.getBoolean("ban.types.packets")) { banAdd(player); }
			}
		} else {
			e.setCancelled(true);
		}
	}

	protected static void exploit(Player player) {
		if (player != null) {
			boolean a = config.getBoolean("defend.exploits.whitelist.admins") ? !config.getConfigurationSection("admins").getKeys(false).contains(player.getName()) : true;
			if (!config.getStringList("defend.exploits.whitelist.players").contains(player.getName()) && a) {
				exploitPunish(player);
				if (config.getBoolean("kick.exploits.enabled")) { kick(player, config.getString("kick.exploits.reason")); }
				if (config.getBoolean("ban.types.exploits")) { banAdd(player); }
			}
		}
	}

	protected static void exploitPunish(Player player) {
		log(langLog.exploitLog.formatted(player == null ? "null" : player.getName()), CatLevel.warn);
		if (config.getBoolean("log.types.defend")) { log(player, langLog.exploit); }
	}

	protected static void banAdd(Player player) {
		if (!dir.exists()) { dir.mkdir(); }
		File pf = new File(dir, player.getName() + ".yml");
		if (!pf.exists()) {
			try {
				pf.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration c = YamlConfiguration.loadConfiguration(pf);
		if (!c.contains("num")) { c.set("num", 0); }
		c.set("num", c.getInt("num") + 1);
		try {
			c.save(pf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void kick(Player player, String r) { if (!config.getStringList("kick.ignore").contains(player.getName())) { player.kickPlayer(color(r)); } }

	protected enum InventoryClickType {
		PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;

		public static InventoryClickType name(String name) {
			try {
				return InventoryClickType.valueOf(name.toUpperCase());
			} catch (IllegalArgumentException e) {
				return QUICK_MOVE;
			}
		}
	}
}
