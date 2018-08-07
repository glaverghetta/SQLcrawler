package usf.edu.bronie.sqlcrawler.crawler;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubProjectCrawler {

    WebDriver mChromeDriver;

    private Pattern mStarCountPattern = Pattern.compile(RegexConstants.GITHUB_STAR_PATTERN);

    private Pattern mForkCountPattern = Pattern.compile(RegexConstants.GITHUB_FORK_PATTERN);

    private Pattern mForkFromPattern = Pattern.compile(RegexConstants.GITHUB_FORK_FROM_PATTERN);

    private Pattern mWatchCountPattern = Pattern.compile(RegexConstants.GITHUB_WATCH_PATTERN);

    private Pattern mCommitDatePattern = Pattern.compile(RegexConstants.GITHUB_PROJECT_COMMIT_TIME_PATTERN);

    private Pattern mTotalCommitPattern = Pattern.compile(RegexConstants.GITHUB_TOTAL_COMMIT_PATTERN);

    private Pattern mTotalBranchPattern = Pattern.compile(RegexConstants.GITHUB_TOTAL_BRANCH_PATTERN);

    private Pattern mTotalReleasePattern = Pattern.compile(RegexConstants.GITHUB_TOTAL_RELEASE_PATTERN);

    private Pattern mTotalContPattern = Pattern.compile(RegexConstants.GITHUB_TOTAL_CONT_PATTERN);

    public GithubProjectCrawler() {
        mChromeDriver = new ChromeDriver();
        new WebDriverWait(mChromeDriver, UrlConstants.GITHUB_REQ_TIMEOUT).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    public GithubFileSpec getFileSpecByUrl(String url) {
        mChromeDriver.get(url);
        String page = mChromeDriver.getPageSource();

        if (page == null) return null;

        GithubFileSpec.GithubFileSpecBuilder githubFileSpecBuilder = new GithubFileSpec.GithubFileSpecBuilder();

        Matcher starCountMatcher = mStarCountPattern.matcher(page);
        if (starCountMatcher.find()) {
            githubFileSpecBuilder.setStarCount(starCountMatcher.group());
        }

        Matcher forkCountMatcher = mForkCountPattern.matcher(page);
        if (forkCountMatcher.find()) {
            githubFileSpecBuilder.setForkCount(forkCountMatcher.group());
        }

        Matcher watchCountMatcher = mWatchCountPattern.matcher(page);
        if (watchCountMatcher.find()) {
            githubFileSpecBuilder.setWatchCount(watchCountMatcher.group());
        }

        Matcher commitDateMatcher = mCommitDatePattern.matcher(page);
        boolean isDateAve = commitDateMatcher.find();
        if (isDateAve) {
            githubFileSpecBuilder.setCommitDate(commitDateMatcher.group());
        }

        Matcher forkFromMatcher = mForkFromPattern.matcher(page);
        if (forkFromMatcher.find()) {
            githubFileSpecBuilder.setForkedFrom(forkFromMatcher.group());
        }

        page = StringUtils.normalizeSpace(page).replaceAll(",", "");

        Matcher totalCommitMatcher = mTotalCommitPattern.matcher(page);
        if (totalCommitMatcher.find()) {
            githubFileSpecBuilder.setTotalCommit(totalCommitMatcher.group());
        }

        Matcher totalBranchMatcher = mTotalBranchPattern.matcher(page);
        if (totalBranchMatcher.find()) {
            githubFileSpecBuilder.setTotalBranch(totalBranchMatcher.group());
        }

        Matcher totalReleaseMatcher = mTotalReleasePattern.matcher(page);
        if (totalReleaseMatcher.find()) {
            githubFileSpecBuilder.setTotalRelease(totalReleaseMatcher.group());
        }

        Matcher totalContMatcher = mTotalContPattern.matcher(page);
        if (totalContMatcher.find()) {
            githubFileSpecBuilder.setTotalContribution(totalContMatcher.group());
        }

        return githubFileSpecBuilder.createGithubFileSpec();
    }
}
