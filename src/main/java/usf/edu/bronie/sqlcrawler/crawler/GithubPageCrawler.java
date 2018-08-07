package usf.edu.bronie.sqlcrawler.crawler;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubPageCrawler {
    private Pattern mCommitDatePattern = Pattern.compile(RegexConstants.GITHUB_PROJECT_COMMIT_TIME_PATTERN);

    private WebDriver mChromeDriver;

    public GithubPageCrawler() {
        mChromeDriver = new ChromeDriver();
        new WebDriverWait(mChromeDriver, UrlConstants.GITHUB_REQ_TIMEOUT).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    public void loadUrl(String url) {
        mChromeDriver.get(url);
    }

    public String getFileCommitDate() {
        String page = mChromeDriver.getPageSource();
        if (page == null) return null;

        String date = null;

        Matcher commitDateMatcher = mCommitDatePattern.matcher(page);
        if (commitDateMatcher.find()) {
            date = commitDateMatcher.group();
        }

        return date;
    }
}
