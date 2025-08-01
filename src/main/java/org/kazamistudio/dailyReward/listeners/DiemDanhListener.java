package org.kazamistudio.dailyReward.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.kazamistudio.dailyReward.DailyReward;
import org.kazamistudio.dailyReward.gui.DiemDanhGUI;
import org.kazamistudio.dailyReward.gui.NgayBuGUI;
import org.kazamistudio.dailyReward.managers.DataManager;
import org.kazamistudio.dailyReward.managers.RewardManager;
import org.kazamistudio.dailyReward.utils.EconomyHook;
import org.kazamistudio.dailyReward.utils.MessageUtil;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiemDanhListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().matches(".*Điểm danh tháng \\d+ năm \\d+.*")) return;

        e.setCancelled(true); // Không cho lấy item ra

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot == 45 || slot == 53) {
            String title = e.getView().getTitle();
            int currentMonth = LocalDate.now().getMonthValue();
            int currentYear = LocalDate.now().getYear();

            try {
                Pattern pattern = Pattern.compile("tháng (\\d+) năm (\\d+)");
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    currentMonth = Integer.parseInt(matcher.group(1));
                    currentYear = Integer.parseInt(matcher.group(2));
                }
            } catch (Exception ignored) {}

            int targetMonth = currentMonth;
            int targetYear = currentYear;

            if (slot == 45) {
                targetMonth--;
                if (targetMonth < 1) {
                    targetMonth = 12;
                    targetYear--;
                }
            } else if (slot == 53) {
                targetMonth++;
                if (targetMonth > 12) {
                    targetMonth = 1;
                    targetYear++;
                }
                if ((targetYear > LocalDate.now().getYear()) ||
                        (targetYear == LocalDate.now().getYear() && targetMonth > LocalDate.now().getMonthValue())) {
                    player.sendMessage(MessageUtil.color("&cKhông thể xem trước tháng chưa đến."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }
            }

            int finalTargetMonth = targetMonth;
            int finalTargetYear = targetYear;
            Bukkit.getScheduler().runTaskLater(DailyReward.getInstance(), () -> {
                DiemDanhGUI.open(player, finalTargetYear, finalTargetMonth);
            }, 2L);
            return;
        }



        int day = slot + 1;
        String title = e.getView().getTitle();
        Pattern pattern = Pattern.compile("tháng (\\d+) năm (\\d+)");
        Matcher matcher = pattern.matcher(title);

        int guiMonth = LocalDate.now().getMonthValue();
        int guiYear = LocalDate.now().getYear();

        if (matcher.find()) {
            guiMonth = Integer.parseInt(matcher.group(1));
            guiYear = Integer.parseInt(matcher.group(2));
        }

        LocalDate clickedDate;
        try {
            clickedDate = LocalDate.of(guiYear, guiMonth, day);
        } catch (Exception ex) {
            player.sendMessage(MessageUtil.color("&cNgày không hợp lệ."));
            return;
        }

        LocalDate today = LocalDate.now();


        // 🔴 Nếu là ngày trong tương lai
        if (clickedDate.isAfter(today)) {
            player.sendMessage(MessageUtil.color("&eNgày này chưa đến. Hãy quay lại vào ngày đó để điểm danh!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
            return;
        }

        // 🔴 Nếu là ngày đã qua (chỉ cho điểm danh hôm nay)
        if (!clickedDate.equals(today)) {
            player.sendMessage(MessageUtil.color("&cBạn đã bỏ lỡ ngày này. Dùng chức năng Ngày Bù để điểm danh lại!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        DataManager data = DailyReward.getInstance().getDataManager();
        if (data.hasChecked(player.getUniqueId().toString(), today)) {
            player.sendMessage(MessageUtil.color(DailyReward.getInstance().getConfig().getString(
                    "messages.checked_success", "&cBạn đã điểm danh hôm nay rồi!"
            )));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return;
        }

        // ✅ Cập nhật điểm danh
        data.markChecked(player.getUniqueId().toString(), today);

        // Mở lại GUI để cập nhật trạng thái
        Bukkit.getScheduler().runTaskLater(DailyReward.getInstance(), () -> {
            org.kazamistudio.dailyReward.gui.DiemDanhGUI.open(player);
        }, 2L);

        // 🎁 Thưởng ngày
        RewardManager rewardManager = DailyReward.getInstance().getRewardManager();
        rewardManager.giveDailyReward(player, today.getDayOfMonth());

        // 🔥 Kiểm tra chuỗi 7 ngày
        int streak = data.getCurrentStreak(player.getUniqueId().toString());
        if (streak >= 7) {
            rewardManager.giveStreakReward(player);
        }
    }


    @EventHandler
    public void onNgayBuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().contains("Ngày bỏ lỡ")) return;

        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.getType().equals(Material.RED_DYE)) return;

        String name = item.getItemMeta().getDisplayName();
        if (!name.contains("Ngày")) return;

        try {
            int day = Integer.parseInt(name.replaceAll("[^0-9]", ""));
            LocalDate today = LocalDate.now();
            LocalDate targetDate = LocalDate.of(today.getYear(), today.getMonthValue(), day);

            if (targetDate.isAfter(today)) {
                player.sendMessage(MessageUtil.color("&eNgày này chưa đến, không thể điểm danh trước!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                return;
            }

            DataManager data = DailyReward.getInstance().getDataManager();
            if (data.hasChecked(player.getUniqueId().toString(), targetDate)) {
                player.sendMessage(MessageUtil.color(DailyReward.getInstance().getConfig().getString(
                        "messages.already_checked", "&cBạn đã điểm danh ngày này rồi!"
                )));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            // Bỏ qua điểm danh - cấu hình
            int cost = DailyReward.getInstance().getConfig().getInt("bypass.cost", 500);
            boolean enable = DailyReward.getInstance().getConfig().getBoolean("bypass.enabled", true);

            if (enable) {
                double balance = EconomyHook.getEconomy().getBalance(player);
                if (balance < cost) {
                    player.sendMessage(MessageUtil.color(DailyReward.getInstance().getConfig().getString(
                            "bypass.message_not_enough", "&cKhông đủ tiền để điểm danh lại. Cần %cost% xu."
                    ).replace("%cost%", String.valueOf(cost))));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                EconomyHook.getEconomy().withdrawPlayer(player, cost);
                player.sendMessage(MessageUtil.color(DailyReward.getInstance().getConfig().getString(
                        "bypass.message_paid", "&aĐã trừ %cost% xu để điểm danh lại ngày %day%."
                ).replace("%cost%", String.valueOf(cost)).replace("%day%", String.valueOf(day))));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }

            // Đánh dấu là đã bypass và nhận thưởng
            data.markBypass(player.getUniqueId().toString(), targetDate);
            DailyReward.getInstance().getRewardManager().giveDailyReward(player, day);

            // Mở lại GUI sau khi cập nhật
            Bukkit.getScheduler().runTaskLater(DailyReward.getInstance(), () -> {
                NgayBuGUI.refresh(player, player.getOpenInventory().getTopInventory());
            }, 2L);

        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage(MessageUtil.color("&cCó lỗi xảy ra khi xử lý điểm danh lại!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);
        }
    }

}

