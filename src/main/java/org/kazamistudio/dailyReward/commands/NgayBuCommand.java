package org.kazamistudio.dailyReward.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kazamistudio.dailyReward.gui.NgayBuGUI;

public class NgayBuCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player player) {
            NgayBuGUI.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            return true;
        } else {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi.");
            return true;
        }
    }
}
