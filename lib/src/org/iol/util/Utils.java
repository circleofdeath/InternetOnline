package org.iol.util;

import java.util.Objects;

public class Utils {
    public static final int WINDOWS = 0;
    public static final int LINUX = 1;
    public static final int MAC = 2;

    public static int getOperationSystemID() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if(os.contains("win")) {
            return WINDOWS;
        } else if(os.contains("mac")) {
            return MAC;
        } else {
            return LINUX;
        }
    }

    public static String concatChild(String path, String child) {
        return normalise(path + "/" + child);
    }

    public static String normaliseWindows(String path) {
        path = path.replace("/", "\\");
        if(path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String normaliseLinux(String path) {
        path = path.replace("\\", "/");
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String normalise(String path) {
        return getOperationSystemID() == WINDOWS ? normaliseWindows(path) : normaliseLinux(path);
    }

    public static String getApplicationData() {
        return switch (getOperationSystemID()) {
            case WINDOWS -> System.getenv("APPDATA");
            case LINUX -> {
                String xdgHome = System.getenv("XDG_DATA_HOME");
                yield Objects.requireNonNullElseGet(xdgHome, () -> System.getProperty("user.home") + "/.local/share");
            }
            case MAC -> "~/Library/";
            default -> System.getProperty("user.home");
        };
    }
}
