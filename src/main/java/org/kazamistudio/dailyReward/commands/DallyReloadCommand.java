package org.kazamistudio.dailyReward.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kazamistudio.dailyReward.DailyReward;

public class DallyReloadCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dailyreward.admin")) {
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
            return true;
        } else {
            try {
                DailyReward.getInstance().reloadConfig();
                DailyReward.getInstance().getRewardManager().reload();
                sender.sendMessage("§aĐã reload toàn bộ cấu hình thành công.");
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                }
            } catch (Exception e) {
                sender.sendMessage("§cCó lỗi xảy ra khi reload: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }
    }
}