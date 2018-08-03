package usf.edu.bronie.sqlcrawler.crawler;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubPageCrawler {

    private Pattern mStarCountPattern = Pattern.compile(RegexConstants.GITHUB_STAR_PATTERN);

    private Pattern mForkCountPattern = Pattern.compile(RegexConstants.GITHUB_FORK_PATTERN);

    private Pattern mWatchCountPattern = Pattern.compile(RegexConstants.GITHUB_WATCH_PATTERN);

    private Pattern mCommitDatePattern = Pattern.compile(RegexConstants.GITHUB_COMMIT_TIME_PATTERN);

    public GithubFileSpec getFileSpecByUrl(String url) {
        String page = HttpConnection.get(url);

        GithubFileSpec.GithubFileSpecBuilder githubFileSpecBuilder = new GithubFileSpec.GithubFileSpecBuilder();

//        Matcher starCountMatcher = mStarCountPattern.matcher(page);
//        if (starCountMatcher.find()) {
//            githubFileSpecBuilder.setStarCount(starCountMatcher.group());
//        }
//
//        Matcher forkCountMatcher = mForkCountPattern.matcher(page);
//        if (forkCountMatcher.find()) {
//            githubFileSpecBuilder.setForkCount(forkCountMatcher.group());
//        }
//
//        Matcher watchCountMatcher = mWatchCountPattern.matcher(page);
//        if (watchCountMatcher.find()) {
//            githubFileSpecBuilder.setWatchCount(watchCountMatcher.group());
//        }
//
//        Matcher commitDateMatcher = mCommitDatePattern.matcher(page);
//        if (commitDateMatcher.find()) {
//            githubFileSpecBuilder.setCommitDate(commitDateMatcher.group());
//        }

        return githubFileSpecBuilder.createGithubFileSpec();
    }
}
