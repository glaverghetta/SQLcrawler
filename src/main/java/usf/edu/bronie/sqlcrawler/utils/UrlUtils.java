package usf.edu.bronie.sqlcrawler.utils;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines several useful functions for interacting with URLs, such as retrieving
 * the github repo URL from a raw github source file url 
 * 
 */

public class UrlUtils {

    /**
     * The searchcode url returned in {@link usf.edu.bronie.sqlcrawler.model.SearchCode.Results Search Code Results} links to
     * a nicely formatted site instead of the raw code file. This function converts the "pretty" URL to the
     * raw file URL.
     * @param url The "pretty" searchcode url to convert
     * @return The URL to the raw source file with no formatting or HTML
     */
    public static String convertSearchCodeToRawDataUrl(String url) {
        return url.replace("view", "raw");
    }


    // For example, https://raw.githubusercontent.com/Ktrio3/SQLcrawler/cfc9c02c0ea846148299a15570a4b06b10fcd3f8/src/main/java/usf/edu/bronie/sqlcrawler/CrawlerMain.java
    // becomes https://github.com/Ktrio3/SQLcrawler
    public static String convertRawGithubToRepo(String url){
        int drop = "https://raw.githubusercontent.com/".length();  //Drop the url
        String a = url.substring(drop);
        int firstSlash = a.indexOf("/");
        int secondSlash = a.indexOf("/", firstSlash+1);
        //Grab everything before secondSlash

        return "https://github.com/" + a.substring(0, secondSlash);
    }

    public static String convertGithubPathToRepo(){
        return "";
    }

    /**
     * Creates a Github URL using the repo name, branch, and path to the file
     * TODO: This function is currently unused. Is it useful?
     * 
     * @param repoName The name of the repository, including both owner and project, such as "Ktrio3/SQLcrawler"
     * @param ref The branch to use in the URL
     * @param path The path to the file
     * @return A String containing the new URL
     */
    public static String createGithubUrl(String repoName, String ref, String path) {
        StringBuilder sb = new StringBuilder("https://github.com/");
        sb.append(repoName).append("/blob/");
        sb.append(getBranch(ref)).append("/").append(path);
        return sb.toString().replaceAll(" ", "%20");
    }

    public static String createGithubProjectUrl(String repoName, String ref) {
        StringBuilder sb = new StringBuilder("https://github.com/");
        sb.append(repoName).append("/tree/");
        sb.append(getBranch(ref));
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

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes("UTF-8"));
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
