package usf.edu.bronie.sqlcrawler.utils;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
