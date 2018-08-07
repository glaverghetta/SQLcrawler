package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;

import java.sql.SQLException;

public class CrawlerMain {
    public static void main(String[] args) {
        setupEnv();

        try {
            new CodeAnalysisManager().analyzeCode();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupEnv() {
        System.setProperty("webdriver.chrome.driver",
                "/Users/cagricetin/Documents/DEV/BroNIE/Sqlcrawler/src/main/resources/chromedriver");
    }
}
