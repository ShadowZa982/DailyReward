package org.kazamistudio.dailyReward.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.kazamistudio.dailyReward.DailyReward;

import java.io.File;
import java.util.List;
import java.util.Random;

public class RewardManager {
    private final File rewardFile = new File(DailyReward.getInstance().getDataFolder(), "dally/reward.yml");
    private final File streakFile = new File(DailyReward.getInstance().getDataFolder(), "dally/7day.yml");
    private YamlConfiguration rewardConfig;
    private YamlConfiguration streakConfig;

    public RewardManager() {
        this.rewardConfig = YamlConfiguration.loadConfiguration(this.rewardFile);
        this.streakConfig = YamlConfiguration.loadConfiguration(this.streakFile);
    }

    public void reload() {
        this.rewardConfig = YamlConfiguration.loadConfiguration(this.rewardFile);
        this.streakConfig = YamlConfiguration.loadConfiguration(this.streakFile);
    }

    public void giveDailyReward(Player player, int day) {
        FileConfiguration config = this.rewardConfig;
        String group;
        if (day >= 1 && day <= 10) {
            group = "group1";
        } else if (day >= 11 && day <= 20) {
            group = "group2";
        } else {
            group = "group3";
        }

        List<String> rewards = config.getStringList("rewards." + group);
        if (rewards.isEmpty()) {
            player.sendMessage("§cKhông tìm thấy phần thưởng cho ngày " + day);
        } else {
            String command = ((String)rewards.get((new Random()).nextInt(rewards.size()))).replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.sendMessage("§aBạn đã nhận phần thưởng điểm danh!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }
    }

    public void giveStreakReward(Player player) {
        List<String> commands = this.streakConfig.getStringList("commands");
        String message = this.streakConfig.getString("message", "&aBạn đã nhận phần thưởng chuỗi 7 ngày!");
        commands.forEach((cmd) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
        player.sendMessage(message.replace("&", "§"));
    }
}
