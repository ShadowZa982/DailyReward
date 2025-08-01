package org.kazamistudio.dailyReward.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.kazamistudio.dailyReward.DailyReward;
import org.kazamistudio.dailyReward.managers.DataManager;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DailyRewardExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "dailyreward"; // ví dụ: %dailyreward_streak%
    }

    @Override
    public @NotNull String getAuthor() {
        return "KazamiStudio";
    }

    @Override
    public @NotNull String getVersion() {
        return DailyReward.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        DataManager data = DailyReward.getInstance().getDataManager();
        String uuid = player.getUniqueId().toString();
        LocalDate now = LocalDate.now();

        switch (params.toLowerCase()) {
            case "streak":
                return String.valueOf(data.getCurrentStreak(uuid));

            case "total_checked":
                Map<LocalDate, String> map = data.getAllForMonth(uuid, now.getYear(), now.getMonthValue());
                long checked = map.values().stream().filter(s -> s.equalsIgnoreCase("checked")).count();
                return String.valueOf(checked);

            case "last_checked":
                Map<LocalDate, String> all = data.getAllForMonth(uuid, now.getYear(), now.getMonthValue());
                LocalDate last = all.entrySet().stream()
                        .filter(entry -> entry.getValue().equalsIgnoreCase("checked"))
                        .map(Map.Entry::getKey)
                        .max(LocalDate::compareTo)
                        .orElse(null);

                if (last != null) {
                    return last.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    return "Chưa có";
                }

            default:
                return null;
        }
    }
}
