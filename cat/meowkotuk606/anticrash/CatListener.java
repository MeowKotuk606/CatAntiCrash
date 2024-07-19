package cat.meowkotuk606.anticrash;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import cat.meowkotuk606.anticrash.CatAntiCrash.CatLevel;
import cat.meowkotuk606.anticrash.Objects.VerifyStatus;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CatListener implements CommandExecutor, Listener, TabExecutor {
	public CatListener(CatAntiCrash main) { Bukkit.getPluginManager().registerEvents(this, main); }

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("verify")) { if (args.length == 1) { return List.of(CatAntiCrash.config.getString("commands.verify_arg")); } }
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
		if (cmd.getName().equalsIgnoreCase("verify")) {
			if (!(sender instanceof Player)) { return false; }
			Player player = (Player) sender;
			if (CatAntiCrash.v.get(player) != null) {
				if (args.length == 1) {
					if (CatAntiCrash.v.get(player).codeVerify.inCheck) {
						String typed = args[0];
						VerifyStatus status = CatAntiCrash.v.get(player);
						String real = status.codeVerify.code;
						if (typed.equals(real)) {
							status.codeVerify.inCheck = false;
							status.codeVerify.completed = true;
							if (status.bossbarCode.v2 != null && !status.bossbarCode.v2.isCancelled()) { status.bossbarCode.v2.cancel(); }
							status.bossbarCode.v1.removePlayer(player);
							status.tsk.cancel();
							CatAntiCrash.log(CatAntiCrash.langLog.passedCheckLog.formatted(player.getName(), "code"), CatLevel.success);
							CatAntiCrash.log(player, CatAntiCrash.langLog.passedCheck.formatted("code"));
							if (status.checkInt >= 2) {
								CatAntiCrash.manage.success(player);
								return true;
							}
							status.checkInt++;
							if (CatAntiCrash.checkInt.get(status.checkInt).equals("password")) {
								CatAntiCrash.manage.verifyPassword(player);
							} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("button")) {
								CatAntiCrash.manage.verifyButton(player);
							} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("code")) {
								CatAntiCrash.manage.verifyCode(player);
							} else CatAntiCrash.manage.success(player);
						} else {
							CatAntiCrash.log(CatAntiCrash.langLog.failedCheckLog.formatted(player.getName(), "code"), CatLevel.log);
							CatAntiCrash.log(player, CatAntiCrash.langLog.failedCheck.formatted("code"));
							CatAntiCrash.kick(player, CatAntiCrash.config.getString("verify.incorrect.code"));
							return false;
						}
					} else if (CatAntiCrash.v.get(player).passwordVerify.inCheck) {
						String typed = args[0];
						VerifyStatus status = CatAntiCrash.v.get(player);
						String real = status.passwordVerify.pass;
						if (typed.equals(real)) {
							status.passwordVerify.inCheck = false;
							status.passwordVerify.completed = true;
							if (status.bossbarPassword.v2 != null && !status.bossbarPassword.v2.isCancelled()) { status.bossbarPassword.v2.cancel(); }
							status.bossbarPassword.v1.removePlayer(player);
							status.tsk.cancel();
							CatAntiCrash.log(CatAntiCrash.langLog.passedCheckLog.formatted(player.getName(), "password"), CatLevel.success);
							CatAntiCrash.log(player, CatAntiCrash.langLog.passedCheck.formatted("password"));
							if (status.checkInt >= 2) {
								CatAntiCrash.manage.success(player);
								return true;
							}
							status.checkInt++;
							if (CatAntiCrash.checkInt.get(status.checkInt).equals("password")) {
								CatAntiCrash.manage.verifyPassword(player);
							} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("button")) {
								CatAntiCrash.manage.verifyButton(player);
							} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("code")) {
								CatAntiCrash.manage.verifyCode(player);
							} else CatAntiCrash.manage.success(player);
						} else {
							CatAntiCrash.log(CatAntiCrash.langLog.failedCheckLog.formatted(player.getName(), "password"), CatLevel.log);
							CatAntiCrash.log(player, CatAntiCrash.langLog.failedCheck.formatted("password"));
							CatAntiCrash.kick(player, CatAntiCrash.config.getString("verify.incorrect.password"));
							return false;
						}
					}
				} else {
					CatAntiCrash.send(player, CatAntiCrash.config.getString("verify.use"));
					return false;
				}
			} else return false;
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("cacrel")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (CatAntiCrash.isAdmin(player)) {
					CatAntiCrash.createConfig();
					CatAntiCrash.log(CatAntiCrash.langLog.reload.formatted(player.getName()), CatLevel.log);
					CatAntiCrash.send(player, CatAntiCrash.config.getString("reload.succesful"));
				} else CatAntiCrash.send(player, CatAntiCrash.config.getString("reload.no_permission"));
				return true;
			} else if (sender instanceof ConsoleCommandSender) {
				CatAntiCrash.createConfig();
				CatAntiCrash.log(CatAntiCrash.langLog.reloadConsole, CatLevel.log);
				return true;
			}
		}
		return false;
	}

	protected static class EventListenerDS extends ListenerAdapter {
		protected CatAntiCrash m;

		public EventListenerDS(CatAntiCrash m) { this.m = m; }

		@Override
		public void onButtonInteraction(ButtonInteractionEvent event) {
			if (event.getChannelType() == ChannelType.PRIVATE) {
				List<Field> e = event.getMessage().getEmbeds().get(0).getFields();
				if (e.get(1) == null || e.get(0) == null) { return; }
				if (e.get(1).getValue() == null || e.get(0).getValue() == null) { return; }
				if (!CatAntiCrash.config.getString("server").equals(e.get(1).getValue())) { return; }
				if (event.getMessage().getContentRaw().equals(CatAntiCrash.config.getString("embed.responses.used.text"))) {
					event.reply(CatAntiCrash.config.getString("embed.responses.used.msg")).queue();
					return;
				}
				event.getMessage().editMessage(CatAntiCrash.config.getString("embed.responses.used.text")).queue();
				Player player = Bukkit.getPlayer(e.get(0).getValue());
				if (player == null || !CatAntiCrash.v.containsKey(player)) {
					event.reply(CatAntiCrash.config.getString("embed.responses.not_found")).queue();
					return;
				}
				VerifyStatus status = CatAntiCrash.v.get(player);
				if (status.buttonVerify.completed == false) {
					if (event.getComponentId().equals("deny")) {
						event.reply(CatAntiCrash.config.getString("embed.responses.deny")).queue();
						CatAntiCrash.log(CatAntiCrash.langLog.failedCheck.formatted(player.getName(), "button"), CatLevel.log);
						CatAntiCrash.log(player, CatAntiCrash.langLog.failedCheck.formatted("button"));
						CatAntiCrash.kick(player, CatAntiCrash.config.getString("check.block"));
					} else if (event.getComponentId().equals("allow")) {
						event.reply(CatAntiCrash.config.getString("embed.responses.allow")).queue();
						CatAntiCrash.log(CatAntiCrash.langLog.passedCheck.formatted(player.getName(), "button"), CatLevel.success);
						CatAntiCrash.log(player, CatAntiCrash.langLog.passedCheck.formatted("button"));
						status.buttonVerify.completed = true;
						status.buttonVerify.inCheck = false;
						status.tsk.cancel();
						if (status.bossbarButton.v2 != null && !status.bossbarButton.v2.isCancelled()) { status.bossbarButton.v2.cancel(); }
						status.bossbarButton.v1.removePlayer(player);
						if (status.checkInt >= 2) {
							CatAntiCrash.manage.success(player);
							return;
						}
						status.checkInt++;
						if (CatAntiCrash.checkInt.get(status.checkInt).equals("password")) {
							CatAntiCrash.manage.verifyPassword(player);
						} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("button")) {
							CatAntiCrash.manage.verifyButton(player);
						} else if (CatAntiCrash.checkInt.get(status.checkInt).equals("code")) {
							CatAntiCrash.manage.verifyCode(player);
						} else CatAntiCrash.manage.success(player);
					}
				} else event.reply(CatAntiCrash.config.getString("embed.responses.logged")).queue();
				if (CatAntiCrash.config.getBoolean("embed.responses.used.delete")) { event.getMessage().delete().queue(); }
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		if (!p.isOnline()) {
			event.setCancelled(true);
			CatAntiCrash.exploitPunish(null);
			return;
		}
		if (p.isDead()) {
			event.setCancelled(true);
			CatAntiCrash.exploit(p);
			return;
		}
		String cmd = event.getMessage().split(" ")[0];
		if (cmd.equals("verify") && !CatAntiCrashAPI.isVerify(event.getPlayer())) { return; }
		if (!p.hasPermission("catanticrash.bypass.commands")) {
			if (CatAntiCrash.isBlockCmd(cmd, !CatAntiCrashAPI.isVerify(event.getPlayer()))) {
				event.setCancelled(true);
				CatAntiCrash.send(p, CatAntiCrash.config.getString("commands.msg"));
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBukketFill(PlayerBucketFillEvent event) {
		if (!CatAntiCrashAPI.isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".bucket.fill")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBukketEmpty(PlayerBucketEmptyEvent event) {
		if (!CatAntiCrashAPI.isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".bucket.empty")) { event.setCancelled(true); } }
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			Player player = event.getPlayer();
			Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
			String materialName = relative.getType().name();
			if (materialName.contains("END_PORTAL") || materialName.contains("ENDER_PORTAL") || materialName.contains("END_GATEWAY")) {
				event.setCancelled(true);
				player.updateInventory();
				CatAntiCrash.exploit(p);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!CatAntiCrashAPI.isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) {
			if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".break.enabled") && CatAntiCrash.blocked(event.getBlock().getType().name().toLowerCase(), "defend.worlds." + w + ".break.types")) { event.setCancelled(true); }
		}
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			try {
				InventoryType type = p.getOpenInventory().getType();
				if (type != InventoryType.PLAYER && type != InventoryType.CRAFTING && type != InventoryType.CREATIVE) {
					event.setCancelled(true);
					p.closeInventory();
					CatAntiCrash.exploit(p);
				}
			} catch (IncompatibleClassChangeError e) {}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockDispense(BlockDispenseEvent event) {
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			Block block = event.getBlock();
			Location location = block.getLocation();
			World world = location.getWorld();
			boolean b = (CatAntiCrash.r1 != null) ? CatAntiCrash.r1.onBlockDispense(event, block, world, location) : false;
			if (!b) {
				if (world != null) {
					int maxHeight = world.getMaxHeight() - 1;
					if (block.getState() instanceof Dispenser) {
						Dispenser dispenser = (Dispenser) block.getState();
						BlockFace face = ((org.bukkit.material.Dispenser) dispenser.getData()).getFacing();
						double y = location.getY();
						if ((y >= maxHeight && face == BlockFace.UP) || (y <= 1.0D && face == BlockFace.DOWN)) {
							event.setCancelled(true);
							CatAntiCrash.exploitPunish(null);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			Entity[] entities = event.getChunk().getEntities();
			for (Entity entity : entities) {
				if (entity instanceof InventoryHolder) {
					InventoryHolder inventoryHolder = (InventoryHolder) entity;
					Inventory inventory = inventoryHolder.getInventory();
					if (inventory != null) {
						List<HumanEntity> viewers = inventory.getViewers();
						for (HumanEntity viewer : viewers) { viewer.closeInventory(); }
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onFT(BlockFromToEvent event) {
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			Block toBlock = event.getToBlock();
			if (toBlock.getType() == Material.TRIPWIRE) {
				event.setCancelled(true);
				toBlock.setType(Material.AIR);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosion(BlockExplodeEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".explosion.block")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntitySpawn(EntitySpawnEvent event) {
		String w = event.getLocation().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".mobs")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosionEntity(EntityExplodeEvent event) {
		String w = event.getLocation().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".explosion.mob")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPiston(BlockPistonExtendEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".piston.to")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPiston1(BlockPistonRetractEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".piston.from")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!CatAntiCrashAPI.isVerify(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		Player p = event.getPlayer();
		String w = p.getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) {
			if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".place.enabled") && CatAntiCrash.blocked(event.getBlock().getType().name().toLowerCase(), "defend.worlds." + w + ".place.types")) {
				event.setCancelled(true);
				return;
			}
		}
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			Block block = event.getBlock();
			if (block != null) {
				String blockType = block.getType().name().replace("LEGACY_", "");
				if (blockType.equals("REDSTONE") || blockType.equals("REDSTONE_WIRE")) {
					Block blockBelow = block.getRelative(BlockFace.DOWN);
					if (blockBelow.getType().name().contains("TRAPDOOR")) {
						event.setCancelled(true);
						CatAntiCrash.exploit(p);
					}
				}
			}
			try {
				InventoryType type = p.getOpenInventory().getType();
				if (type != InventoryType.PLAYER && type != InventoryType.CRAFTING && type != InventoryType.CREATIVE) {
					event.setCancelled(true);
					p.closeInventory();
					CatAntiCrash.exploit(p);
				}
			} catch (IncompatibleClassChangeError e) {}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFire(BlockBurnEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".fire")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFireStart(BlockIgniteEvent event) {
		String w = event.getBlock().getWorld().getName();
		if (CatAntiCrash.defended().contains(w)) { if (CatAntiCrash.config.getBoolean("defend.worlds." + w + ".fire")) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!CatAntiCrash.config.getBoolean("ban.enabled") || CatAntiCrash.config.getStringList("ban.ignore").contains(event.getPlayer().getName())) { return; }
		File pf = new File(new File(CatAntiCrash.dataFold, "players"), event.getPlayer().getName() + ".yml");
		if (!pf.exists()) { return; }
		YamlConfiguration c = YamlConfiguration.loadConfiguration(pf);
		if (!c.contains("num")) { return; }
		if (CatAntiCrash.config.getInt("ban.num") <= c.getInt("num")) {
			event.disallow(Result.KICK_OTHER, CatAntiCrash.color(CatAntiCrash.config.getString("ban.reason")));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (!CatAntiCrashAPI.isVerify(p)) {
			event.setCancelled(true);
			return;
		}
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			if (p.isInsideVehicle() && !p.getVehicle().isValid()) {
				p.getVehicle().eject();
				CatAntiCrash.exploit(p);
			}
			if (!p.isValid() && !p.isDead()) CatAntiCrash.exploit(p);
			Location to = event.getTo();
			World world = to.getWorld();
			Chunk chunk = to.getChunk();
			if (chunk == null || !world.isChunkLoaded(chunk)) { event.setCancelled(true); }
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onStructureGrow(StructureGrowEvent event) {
		if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
			World world = event.getWorld();
			for (BlockState blockBefore : event.getBlocks()) {
				Block block = world.getBlockAt(blockBefore.getLocation());
				String type = block.getType().name();
				if (type.contains("ENDER_PORTAL") || type.contains("END_PORTAL") || type.contains("END_GATEWAY")) {
					event.setCancelled(true);
					CatAntiCrash.exploit(event.getPlayer());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		CatAntiCrash.reset(p, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer()) && !event.getMessage().startsWith("/verify")) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) event.getEntity();
			if (!CatAntiCrashAPI.isVerify(p)) { event.setCancelled(true); }
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryMoveItem(InventoryMoveItemEvent event) {
		if (event.getDestination().getType() == InventoryType.PLAYER) {
			Player p = (Player) event.getDestination().getHolder();
			if (p == null) { return; }
			if (!CatAntiCrashAPI.isVerify(p)) { event.setCancelled(true); }
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (p.getGameMode() != GameMode.CREATIVE && (p == null || p.getOpenInventory() != event.getView())) {
			event.setCancelled(true);
			p.closeInventory();
			CatAntiCrash.exploit(p);
			return;
		}
		if (p != null) {
			if (!CatAntiCrashAPI.isVerify(p)) {
				event.setCancelled(true);
				return;
			}
			if (event.getInventory().getType() == InventoryType.PLAYER) {
				if (CatAntiCrash.config.getBoolean("defend.exploits.enabled")) {
					ItemStack i = event.getCurrentItem();
					if (event.getInventory().contains(i)) {
						event.setCancelled(true);
						CatAntiCrash.exploit(p);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryDrag(InventoryDragEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (p != null) { if (!CatAntiCrashAPI.isVerify(p)) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInventoryOpen(InventoryOpenEvent event) {
		Player p = (Player) event.getPlayer();
		if (p != null) { if (!CatAntiCrashAPI.isVerify(p)) { event.setCancelled(true); } }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerEditBook(PlayerEditBookEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerFish(PlayerFishEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemDamage(PlayerItemDamageEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerItemHeld(PlayerItemHeldEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPortal(PlayerPortalEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerVelocity(PlayerVelocityEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLay(PlayerBedEnterEvent event) { if (!CatAntiCrashAPI.isVerify(event.getPlayer())) { event.setCancelled(true); } }

	protected static void onPacket(PacketEvent e) {
		if (CatAntiCrash.config.getBoolean("defend.packets.enabled")) {
			if (e.getPacketType() == PacketType.Play.Client.WINDOW_CLICK) {
				PacketContainer packet = e.getPacket();
				int windowId = packet.getIntegers().read(0);
				int slot = packet.getIntegers().read(1);
				int button = packet.getIntegers().read(2);
				int mode = CatAntiCrash.packetCheck(packet);
				if (((mode == 1 || mode == 2) && windowId >= 0 && button < 0) || (windowId >= 0 && mode == 2 && slot < 0)) { CatAntiCrash.packet(e.getPlayer(), e); }
			} else if (e.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
				PacketContainer packet = e.getPacket();
				int l = packet.getStrings().read(0).length();
				if (l > 256) { CatAntiCrash.packet(e.getPlayer(), e); }
			}
		}
	}
}
