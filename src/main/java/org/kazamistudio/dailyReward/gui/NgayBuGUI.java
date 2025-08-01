package org.kazamistudio.dailyReward.gui;

import java.time.LocalDate;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kazamistudio.dailyReward.DailyReward;
import org.kazamistudio.dailyReward.managers.DataManager;

public class NgayBuGUI {
    public static void open(Player player) {
        LocalDate today = LocalDate.now();
        open(player, today.getYear(), today.getMonthValue());
    }

    public static void open(Player player, int year, int month) {
        String title = "\ud83d\udcc5 Ngày bỏ lỡ - tháng " + month + " năm " + year;
        Inventory gui = Bukkit.createInventory((InventoryHolder)null, 54, title);
        refresh(player, gui, year, month);
        player.openInventory(gui);
    }

    public static void refresh(Player player, Inventory inventory, int year, int month) {
        inventory.clear();
        DataManager data = DailyReward.getInstance().getDataManager();
        List<LocalDate> missedDays = data.getMissedDays(player.getUniqueId().toString(), year, month);
        int slot = 0;

        for(LocalDate date : missedDays) {
            if (slot >= inventory.getSize()) {
                break;
            }

            ItemStack item = new ItemStack(Material.RED_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cBỏ lỡ: Ngày " + date.getDayOfMonth());
            meta.setLore(List.of("§7Click để điểm danh lại!"));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

    }

    public static void refresh(Player player, Inventory inventory) {
        String title = player.getOpenInventory().getTitle();
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        try {
            String[] parts = title.replace("\ud83d\udcc5 Ngày bỏ lỡ - tháng ", "").split(" năm ");
            month = Integer.parseInt(parts[0]);
            year = Integer.parseInt(parts[1]);
        } catch (Exception var6) {
        }

        refresh(player, inventory, year, month);
    }
}
