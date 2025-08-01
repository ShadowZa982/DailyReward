package org.kazamistudio.dailyReward.utils;

public class MessageUtil {
    public static String color(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
