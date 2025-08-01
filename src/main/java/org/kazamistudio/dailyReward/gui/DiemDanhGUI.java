package org.kazamistudio.dailyReward.gui;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kazamistudio.dailyReward.DailyReward;
import org.kazamistudio.dailyReward.managers.DataManager;

public class DiemDanhGUI {
    public static void open(Player player) {
        LocalDate now = LocalDate.now();
        open(player, now.getYear(), now.getMonthValue());
    }

    public static void open(Player player, int year, int month) {
        DailyReward plugin = DailyReward.getInstance();
        FileConfiguration config = plugin.getConfig();
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        String title = config.getString("gui.title", "\ud83d\udcc5 Điểm danh tháng %month% năm %year%").replace("%month%", String.valueOf(month)).replace("%year%", String.valueOf(year));
        int size = config.getInt("gui.size", 54);
        Inventory gui = Bukkit.createInventory((InventoryHolder)null, size, title);
        DataManager dataManager = plugin.getDataManager();
        Map<LocalDate, String> statusMap = dataManager.getAllForMonth(player.getUniqueId().toString(), year, month);

        for(int day = 1; day <= daysInMonth; ++day) {
            LocalDate date = LocalDate.of(year, month, day);
            String status;
            if (statusMap.containsKey(date)) {
                status = "checked";
            } else {
                status = getDefaultStatus(date);
            }

            String path = "items." + status;
            String materialName = config.getString(path + ".material", "GRAY_DYE");
            Material material = Material.getMaterial(materialName.toUpperCase());
            if (material == null) {
                material = Material.GRAY_DYE;
            }

            String nameTemplate = config.getString(path + ".name", "&7Ngày %day%").replace("%day%", String.valueOf(day));
            String displayName = translateColor(nameTemplate);
            int modelData = config.getInt(path + ".model-data", 0);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                if (modelData > 0) {
                    meta.setCustomModelData(modelData);
                }

                item.setItemMeta(meta);
            }

            gui.setItem(day - 1, item);
        }

        ItemStack prevMonth = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevMonth.getItemMeta();
        prevMeta.setDisplayName("§e⬅ Tháng trước");
        prevMonth.setItemMeta(prevMeta);
        gui.setItem(45, prevMonth);
        ItemStack nextMonth = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextMonth.getItemMeta();
        nextMeta.setDisplayName("§e➡ Tháng sau");
        nextMonth.setItemMeta(nextMeta);
        gui.setItem(53, nextMonth);
        player.openInventory(gui);
    }

    private static String getDefaultStatus(LocalDate date) {
        return date.isBefore(LocalDate.now()) ? "missed" : "default";
    }

    private static String translateColor(String input) {
        return input.replace("&", "§");
    }
}
