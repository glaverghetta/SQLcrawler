package usf.edu.bronie.sqlcrawler.utils;

public class UrlUtils {

    public static String convertSearchCodeToRawDataUrl(String url) {
        return url.replace("view", "raw");
    }
}
