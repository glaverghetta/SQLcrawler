package usf.edu.bronie.sqlcrawler.utils;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    public static String convertSearchCodeToRawDataUrl(String url) {
        return url.replace("view", "raw");
    }

    public static String createGithubUrl(String repoName, String ref, String path) {
        StringBuilder sb = new StringBuilder("https://github.com/");
        sb.append(repoName).append("/blob/");
        sb.append(getBranch(ref)).append("/").append(path);
        return sb.toString().replaceAll(" ", "%20");
    }

    private static String getBranch(String ref) {
        Pattern mLastSlashPattern = Pattern.compile(RegexConstants.AFTER_LAST_SLASH);
        Matcher appendMatcher = mLastSlashPattern.matcher(ref);
        if (appendMatcher.find()) {
            return appendMatcher.group();
        }
        return ref;
    }

    public static String createGithubRawUrl(String repoName, String ref, String path) {
        StringBuilder sb = new StringBuilder("https://raw.githubusercontent.com/");
        sb.append(repoName).append("/");
        sb.append(getBranch(ref)).append("/").append(path);
        return sb.toString().replaceAll(" ", "%20");
    }
}
