package usf.edu.bronie.sqlcrawler.constants;

public class UrlConstants {

    public static final String SEARCHCODE_URL = "https://searchcode.com/api/codesearch_I/?q=executeQuery&lan=23&per_page=100&p=";

    public static final int SEARCHCODE_MAX_PAGE = 6;

    public static final String GDRIVE_URL = "https://script.google.com/macros/s/AKfycbyc8wzUqaygUV6fqy26EW7PNqwHzGCs7z15ulKhW9tJpJnK8Zls/exec?returnJson=true&sheetNumber=";

    public static final int GDRIVE_PAGE = 0;

    public static final int GITHUB_REQ_TIMEOUT = 3000; // In milliseconds

    //Default Github request parameters
    public static final String GITHUB_SEARCH_URL = "https://api.github.com/search/code?q=%s&per_page=%d&p=%d";

    public static final int GITHUB_DEFAULT_MAX_PAGE = 10;
}
