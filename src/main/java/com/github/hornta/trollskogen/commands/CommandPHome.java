package com.github.hornta.trollskogen.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.sassyspawn.SassySpawn;
import com.github.hornta.sassyspawn.Spawn;
import com.github.hornta.sassyspawn.SpawnManager;
import com.github.hornta.trollskogen.Main;
import com.github.hornta.trollskogen.User;
import com.github.hornta.trollskogen.homes.Home;
import com.github.hornta.trollskogen.messagemanager.MessageManager;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CommandPHome implements ICommandHandler, Listener {
  private Main main;
  private HashMap<UUID, Long> immortals = new HashMap<>();
  private HashMap<UUID, Boolean> immortalsTeleported = new HashMap<>();
  private HashMap<UUID, String> immortalsHomeOwner = new HashMap<>();
  private static HashSet<String> blockedCmds;

  static {
    blockedCmds = new HashSet<>();
    blockedCmds.add("sethome");
    blockedCmds.add("top");
    blockedCmds.add("etop");
    blockedCmds.add("tpahere");
    blockedCmds.add("etpahere");
    blockedCmds.add("tpaccept");
    blockedCmds.add("etpaccept");
    blockedCmds.add("tpyes");
    blockedCmds.add("etpyes");
  }

  public CommandPHome(Main main) {
    this.main = main;
  }

  @Override
  public void handle(CommandSender sender, String[] args, int typedArgs) {
    User user = main.getUser(args[0]);
    Home home = user.getHome(args[1]);

    User currentUser = main.getUser(sender);
    if(currentUser == user) {
      return;
    }

    Player player = (Player) sender;
    immortals.put(player.getUniqueId(), System.currentTimeMillis() + 5000);
    immortalsHomeOwner.put(player.getUniqueId(), user.getLastSeenAs());
    immortalsTeleported.put(player.getUniqueId(), false);
    player.teleport(home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    String commandWithoutSlash = event.getMessage().trim().substring(1);
    int firstSpaceIndex = commandWithoutSlash.indexOf(" ");

    String command;
    if(firstSpaceIndex == -1) {
      command = commandWithoutSlash;
    } else {
      command = commandWithoutSlash.substring(0, firstSpaceIndex).toLowerCase();
    }

    if(!blockedCmds.contains(command)) {
      return;
    }

    Home home = main.getHomeManager().getNearestPublicHome(event.getPlayer());
    if(home == null) {
      return;
    }

    main.getMessageManager().setValue("player_name", main.getUser(home.getOwner()).getLastSeenAs());
    main.getMessageManager().sendMessage(event.getPlayer(), "phome_blocked_cmd");
    event.setCancelled(true);
  }

  @EventHandler
  void onPlayerDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();
    if (immortals.containsKey(player.getUniqueId())) {
      long expire = immortals.get(player.getUniqueId());
      if (System.currentTimeMillis() >= expire) {
        immortals.remove(player.getUniqueId());
        immortalsHomeOwner.remove(player.getUniqueId());
      } else {
        event.setCancelled(true);
        player.setFireTicks(0);

        if (main.getSassySpawn() != null && !immortalsTeleported.get(player.getUniqueId())) {
          Spawn spawn = SassySpawn.getSpawnManager().getSpawn(SpawnManager.DEFAULT_SPAWN);
          immortalsTeleported.put(player.getUniqueId(), true);
          Bukkit.getScheduler().runTaskLater(main, () -> {
            player.teleport(spawn.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFireTicks(0);
            immortals.remove(player.getUniqueId());
            main.getMessageManager().setValue("player_name", immortalsHomeOwner.get(player.getUniqueId()));
            main.getMessageManager().sendMessage(player, "phome_safe_teleport");
            immortalsHomeOwner.remove(player.getUniqueId());
          }, 1);
        }

      }
    }
  }

}
