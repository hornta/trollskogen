package com.github.hornta.trollskogen.commands;

import com.github.hornta.trollskogen.Main;
import org.bukkit.command.CommandSender;
import se.hornta.carbon.ICommandHandler;

import java.text.DecimalFormat;

public class CommandAnnouncementEnable implements ICommandHandler {
  private Main main;
  public CommandAnnouncementEnable(Main main) {
    this.main = main;
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    main.getAnnouncements().setEnabled(true);
    main.getMessageManager().sendMessage(commandSender, "enabled");
  }
}