package org.kazamistudio.dailyReward.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kazamistudio.dailyReward.DailyReward;
import org.kazamistudio.dailyReward.managers.DataManager;
import org.kazamistudio.dailyReward.utils.MessageUtil;

import java.time.LocalDate;

public class JoinNotifyListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(DailyReward.getInstance(), () -> {
            DataManager data = DailyReward.getInstance().getDataManager();
            LocalDate today = LocalDate.now();
            String uuid = event.getPlayer().getUniqueId().toString();
            if (!data.hasChecked(uuid, today)) {
                String msg = DailyReward.getInstance().getConfig().getString("messages.notify_unchecked", "&eBạn chưa điểm danh hôm nay! Dùng lệnh &a/diemdanh &eđể nhận thưởng.");
                event.getPlayer().sendMessage(MessageUtil.color(msg));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.2F);
            }

        }, 40L);
    }
}
