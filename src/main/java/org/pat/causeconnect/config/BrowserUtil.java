package org.pat.causeconnect.config;

public class BrowserUtil {
    public static void openBrowser() {
        try {
            String url = "http://localhost:8080/";
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("open " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
