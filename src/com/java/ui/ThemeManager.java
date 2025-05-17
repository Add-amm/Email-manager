package com.java.ui;

import java.awt.Color;

public class ThemeManager {
    private static boolean darkMode = false;

    public static void toggleTheme() {
        darkMode = !darkMode;
    }

    public static Color getBackgroundColor() {
        return darkMode ? new Color(25, 29, 40) : Color.WHITE;
    }

    public static Color getTextColor() {
        return darkMode ? Color.WHITE : Color.BLACK;
    }

    public static Color getSidebarColor() {
        return darkMode ? new Color(20, 22, 30) : new Color(245, 245, 245);
    }

    public static Color getTopBarColor() {
        return darkMode ? new Color(30, 33, 45) : new Color(250, 250, 250);
    }

     public static Color getButtonColor() {
        return darkMode ? new Color(60, 90, 240) : new Color(60, 180, 100);
    }

    public static Color getItemBackground() {
        return darkMode ? new Color(35, 39, 50) : new Color(255, 255, 255);
    }

    public static Color getLinkColor() {
    return darkMode ? new Color(100, 170, 255) : Color.BLUE.darker();
}


}