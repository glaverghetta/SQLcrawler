package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;

import java.sql.SQLException;

public class CrawlerMain {
    public static void main(String[] args) {
        setupEnv();

        try {
            int min = Integer.valueOf(args[0]);
            int max = Integer.valueOf(args[1]);
            new CodeAnalysisManager().analyzeCode(min, max);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupEnv() {
        System.setProperty("webdriver.chrome.driver",
                CredentialConstants.RESOURCES + "chromedriver");
    }
}
