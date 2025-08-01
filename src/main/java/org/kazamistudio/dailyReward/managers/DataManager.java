package org.kazamistudio.dailyReward.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kazamistudio.dailyReward.DailyReward;

public class DataManager {
    private Connection connection;

    public DataManager() {
        this.connect();
        this.createTable();
    }

    private void connect() {
        try {
            File dbFile = new File(DailyReward.getInstance().getDataFolder(), "data.db");
            String url = "jdbc:sqlite:" + dbFile.getPath();
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS daily_checkin (uuid TEXT NOT NULL, date TEXT NOT NULL, status TEXT NOT NULL, PRIMARY KEY (uuid, date))";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean hasChecked(String uuid, LocalDate date) {
        String sql = "SELECT status FROM daily_checkin WHERE uuid = ? AND date = ?";

        try {
            boolean var6;
            try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setString(2, date.toString());
                ResultSet rs = ps.executeQuery();
                var6 = rs.next() && rs.getString("status").equals("checked");
            }

            return var6;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void markChecked(String uuid, LocalDate date) {
        this.setStatus(uuid, date, "checked");
    }

    public void markMissed(String uuid, LocalDate date) {
        this.setStatus(uuid, date, "missed");
    }

    public void markBypass(String uuid, LocalDate date) {
        this.setStatus(uuid, date, "bypass");
    }

    private void setStatus(String uuid, LocalDate date, String status) {
        String sql = "INSERT OR REPLACE INTO daily_checkin (uuid, date, status) VALUES (?, ?, ?)";

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, date.toString());
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Map<LocalDate, String> getAllForMonth(String uuid, int year, int month) {
        Map<LocalDate, String> result = new HashMap();
        String sql = "SELECT date, status FROM daily_checkin WHERE uuid = ? AND date LIKE ?";

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, year + "-" + String.format("%02d", month) + "%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date"));
                String status = rs.getString("status");
                result.put(date, status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<LocalDate> getMissedDays(String uuid, int year, int month) {
        List<LocalDate> missed = new ArrayList();
        Map<LocalDate, String> all = this.getAllForMonth(uuid, year, month);
        int days = LocalDate.of(year, month, 1).lengthOfMonth();

        for(int i = 1; i <= days; ++i) {
            LocalDate date = LocalDate.of(year, month, i);
            if (date.isBefore(LocalDate.now()) && !all.containsKey(date)) {
                missed.add(date);
            } else if ("missed".equals(all.get(date))) {
                missed.add(date);
            }
        }

        return missed;
    }

    public int getCurrentStreak(String uuid) {
        LocalDate today = LocalDate.now();
        int streak = 0;

        for(int i = 0; i < 7; ++i) {
            LocalDate check = today.minusDays((long)i);
            if (!this.hasChecked(uuid, check)) {
                break;
            }

            ++streak;
        }

        return streak;
    }
}
