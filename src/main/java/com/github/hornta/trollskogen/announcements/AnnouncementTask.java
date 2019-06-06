package com.github.hornta.trollskogen.announcements;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnouncementTask extends BukkitRunnable {
  private JavaPlugin plugin;
  private Announcements announcements;

  AnnouncementTask(JavaPlugin plugin, Announcements announcements) {
    this.plugin = plugin;
    this.announcements = announcements;
  }

  @Override
  public void run() {
    if(!announcements.isEnabled()) {
      return;
    }

    Announcement announcement = announcements.getNextAnnouncement();
    if(announcement == null) {
      return;
    }

    plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', announcement.getMessage()));
  }
}