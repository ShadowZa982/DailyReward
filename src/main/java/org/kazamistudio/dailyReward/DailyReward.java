package org.kazamistudio.dailyReward;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.kazamistudio.dailyReward.commands.DallyReloadCommand;
import org.kazamistudio.dailyReward.commands.DiemDanhCommand;
import org.kazamistudio.dailyReward.commands.NgayBuCommand;
import org.kazamistudio.dailyReward.listeners.DiemDanhListener;
import org.kazamistudio.dailyReward.listeners.JoinNotifyListener;
import org.kazamistudio.dailyReward.managers.DataManager;
import org.kazamistudio.dailyReward.managers.RewardManager;
import org.kazamistudio.dailyReward.placeholders.DailyRewardExpansion;
import org.kazamistudio.dailyReward.utils.EconomyHook;

public final class DailyReward extends JavaPlugin {
    private static DailyReward instance;
    private DataManager dataManager;
    private RewardManager rewardManager;

    public void onEnable() {
        instance = this;
        if (!EconomyHook.setupEconomy()) {
            this.getLogger().severe("Không tìm thấy Vault! Plugin sẽ không hoạt động đúng.");
            this.getServer().getPluginManager().disablePlugin(this);
        } else {
            this.saveDefaultConfig();
            this.saveResource("dally/reward.yml", false);
            this.saveResource("dally/7day.yml", false);
            this.dataManager = new DataManager();
            this.rewardManager = new RewardManager();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    (new DailyRewardExpansion()).register();
                    this.getLogger().info("✅ Đăng ký thành công");
                } else {
                    this.getLogger().warning("❌ PlaceholderAPI vẫn chưa được bật sau delay!");
                }

            }, 1L);
            this.getCommand("diemdanh").setExecutor(new DiemDanhCommand());
            this.getCommand("ngaybu").setExecutor(new NgayBuCommand());
            this.getCommand("dallyreload").setExecutor(new DallyReloadCommand());
            Bukkit.getPluginManager().registerEvents(new DiemDanhListener(), this);
            this.getServer().getPluginManager().registerEvents(new JoinNotifyListener(), this);
            this.logBanner();
            this.getLogger().info("DailyReward Plugin enabled!");
        }
    }

    public void onDisable() {
        this.getLogger().info("DailyReward Plugin disabled.");
    }

    public static DailyReward getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public RewardManager getRewardManager() {
        return this.rewardManager;
    }

    private void logBanner() {
        this.logWithColor("&b===== &fKazami Studio &b=====");
        this.logWithColor("&7[&a✔&7] &aSystems have been started..");
        this.logWithColor("&7[&a✔&7] &fVersion: " + this.getDescription().getVersion());
        this.logWithColor("&7[&a✔&7] &fAuthor by &bKazami Studio");
        this.logWithColor("&7[&a✔&7] &9Discord: https://discord.gg/ThEFtBxpRf");
        this.logWithColor("&7[&a✔&7] " + this.getDescription().getName() + " started successfully!");
        this.logWithColor("&b===== &fKazami Studio &b=====");
    }

    private void logWithColor(String msg) {
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}