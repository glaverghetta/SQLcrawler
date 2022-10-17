package usf.edu.bronie.sqlcrawler.io;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.Github.Item;
import usf.edu.bronie.sqlcrawler.model.Github.SearchCode;

/**
 * Retrieves data from Github using the official Github API
 */
public class GithubAPI {

    private static final Logger log = LoggerFactory.getLogger(GithubAPI.class);
    private static final Logger timingLog = LoggerFactory.getLogger("GithubAPILogger");

    private static final String githubURL = "api.github.com";

    private int lastPage = 0;
    private int lastTotalCount = -1;

    private int minSize;
    private int maxSize;
    private Languages language;

    private final int SIZE_LIMIT = 100 * 1000000; // 100 MB
    private final int PER_PAGE = 100; // TODO: Make this non-constant
    private final int MAX_PAGE = 1000 / PER_PAGE;  //1000 is the hard-limit cap of results from GH
    private final int WAIT = 60;  //TODO: Make this non-constant

    // Start all limits/times as unknown
    private int searchLimit = -1;
    private long searchReset = -1; // In Epoch time
    private int coreLimit = -1;
    private long coreReset = -1; // In Epoch time

    /**
     * @param size The initial size to use when searching
     * @param lang The initial lnaguage to use when searching
     */
    public GithubAPI(int size, Languages lang) {
        setSize(size);
        setLanguage(lang);
    }

    /**
     * @param minSize The initial minimum size to use when searching
     * @param maxSize The initial maximum size to use when searching
     * @param lang    The initial lnaguage to use when searching
     */
    public GithubAPI(int minSize, int maxSize, Languages lang) {
        setSize(minSize, maxSize);
        setLanguage(lang);
    }

    /**
     * Returns true if there is another page to read
     * 
     * @return true if there is another page to read, otherwise false
     */
    public boolean isNextPage() {
        if (lastTotalCount < 0) { // No way to know, just say true
            return true;
        }

        if (lastTotalCount == 0) {
            return false; // We pulled it once already and there were no results!
        }

        int numPages = (lastTotalCount / PER_PAGE) + 1;
        int maxPage = numPages > MAX_PAGE ? MAX_PAGE : numPages; // Set to 10 if numPages > 10
        return lastPage < maxPage;
    }

    public int getLastTotalCount() {
        return lastTotalCount;
    }

    /**
     * Manually set the page that should be read next.
     * May result in no results (i.e., search gives only 5 pages and page is 9)
     * 
     * @param page the next page to read (1 to 10)
     */
    public void setPage(int page) {

        if (page > MAX_PAGE || page < 1) {
            log.error("Invalid page number {} (expects 0-{})", page, MAX_PAGE);
            System.exit(-1);
        }

        // For 0 this becomes -1, which acts as if a new size was added
        this.lastPage = page - 1;
    }

    /**
     * Set an exact size that should be used when searching. Resets the page to 1.
     * 
     * @param size
     */
    public void setSize(int size) {
        if (size < 1 || size > SIZE_LIMIT) {
            log.error("Invalid size {} (expects 1-{})", size, SIZE_LIMIT);
            System.exit(-1);
        }
        this.lastPage = 0;
        this.lastTotalCount = -1;
        this.minSize = size;
        this.maxSize = size;
    }

    /**
     * Sets a size range that should be used when searching. Resets the page to 1.
     * 
     * @param size
     */
    public void setSize(int min, int max) {
        if (min > max) {
            log.error("Min size ({}) greater than max size ({})", min, max);
            System.exit(-1);
        }

        if (min < 1 || min > SIZE_LIMIT) {
            log.error("Invalid min size {} (expects 1-{})", min, SIZE_LIMIT);
            System.exit(-1);
        }

        if (max < 1 || max > SIZE_LIMIT) {
            log.error("Invalid max size {} (expects 1-{})", max, SIZE_LIMIT);
            System.exit(-1);
        }

        this.lastPage = 0;
        this.lastTotalCount = -1;
        this.minSize = min;
        this.maxSize = max;
    }

    /**
     * Sets a new language to use when searching
     * 
     * @param lang The new language to use
     */
    public void setLanguage(Languages lang) {
        this.language = lang;
    }

    /**
     * Makes an API call to the requested endpoint with the provided params
     * 
     * @param endpoint The endpoint to request
     * @param params   The parameters used for this request
     * @return The resulting request
     */
    private Response getURL(String endpoint, Map<String, String> params) {

        // Create the cache
        Cache cache = new Cache(new java.io.File(CredentialConstants.GITHUB_CACHE_FILE),
                CredentialConstants.GITHUB_CACHE_SIZE);

        // Setup the headers
        Map<String, String> headers = Map.of(
                "Accept", "application/vnd.github+json",
                "Authorization", "token " + CredentialConstants.GITHUB_TOKEN);

        // Create the url and add params
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(githubURL)
                .addPathSegments(endpoint);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
        }

        // Build the url and make the request
        HttpUrl url = urlBuilder.build();

        OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();
        log.debug("Getting {}", url);

        Response r = null;
        try {
            r = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("Failed to reach Github API", e);
            System.exit(-1);
        }
        return r;
    }

    /**
     * Makes an API request by calling {@link #getURL(String, Map)}
     * and provides error handling and rate limiting for the result
     * 
     * @param url    The endpoint to request
     * @param params The parameters used for this request
     * @return A valid 200 response with rate limits parsed
     * @throws RateLimitException
     * @throws SecondaryLimitException
     */
    private Response request(String url, Map<String, String> params)
            throws RateLimitException, SecondaryLimitException {
        Response r = getURL(url, params);

        // Rate limits always included regardless of response
        if (r.header("x-ratelimit-resource").equals("search")) {
            this.searchLimit = Integer.parseInt(r.header("x-ratelimit-remaining"));
            this.searchReset = Long.parseLong(r.header("x-ratelimit-reset"));
        } else if (r.header("x-ratelimit-resource").equals("core")) {
            this.coreLimit = Integer.parseInt(r.header("x-ratelimit-remaining"));
            this.coreReset = Long.parseLong(r.header("x-ratelimit-reset"));
        } else {
            log.error("Unknown API type: {}", r.header("x-ratelimit-resource"));
            System.exit(-1);
        }

        // Check response code
        if (r.code() == 403) {
            // Check regular limit (may occur if user is using API elsewhere)
            if (Integer.parseInt(r.header("x-ratelimit-remaining")) == 0) {
                throw new RateLimitException(Long.parseLong(r.header("x-ratelimit-reset")),
                        r.header("x-ratelimit-resource"));
            }
            r.close();

            // Must be secondary
            throw new SecondaryLimitException();
        } else if (r.code() == 401) {
            log.error("Bad credentials for Github API");
            System.exit(-1);
        } else if (r.code() != 200 && r.code() != 304) { // 200 OK, 304 Cache hit
            log.error("Unhandled HTTP code: {}", r.code());
            System.exit(-1);
        }
        log.debug("Request to {} returned code {}", url, r.code());

        return r;
    }

    /**
     * Parses the resulting JSON and converts it into the file format used by the
     * database
     * 
     * @param results The page to parse
     * @return A queue containing the resulting {@link File}
     */
    private Queue<File> parseSearchResults(String results) {
        Gson gson = new Gson();
        SearchCode scr = gson.fromJson(results, SearchCode.class);
        
        if (scr == null) {
            // Unknown error, print out the response
            log.error("Unknown response from Github:\n {}", results);
            System.exit(-1);
        }
        
        this.lastTotalCount = Integer.valueOf(scr.getTotal_count());

        List<Item> list = scr.getItems();

        Queue<File> mQueue = new LinkedList<File>();
        for (Item r : list) {
            mQueue.add(new File(r.getName(), r.getPath(), r.getRawUrl(), r.getSha(), r.getCommit()));
        }
        return mQueue;
    }

    private String buildQuery() {
        // Get the query string for the language
        String q = RegexConstants.getSearchTerms(language);

        q = q + " language:" + language.searchString();
        if (minSize != maxSize) {
            q = q + " size:" + minSize + ".." + maxSize;
        } else {
            q = q + " size:" + minSize; // An exact size
        }

        return q;
    }

    /**
     * Calls the search API on Github. Uses the language and size set during
     * construction
     * or using {@link #setLanguage(Languages)}/{@link #setSize(int)}
     * 
     * @return A queue containing the next page of results.
     * @throws RateLimitException
     * @throws SecondaryLimitException
     * @throws PageLimitException
     */
    public Queue<File> search() throws RateLimitException, SecondaryLimitException, PageLimitException {
        long start = System.currentTimeMillis();

        Map<String, String> params = new HashMap<>();

        params.put("q", buildQuery());
        params.put("per_page", String.valueOf(PER_PAGE));

        if (!isNextPage()) {
            throw new PageLimitException();
        }
        params.put("page", String.valueOf(this.lastPage + 1));

        // Check the search API limit, check if we can use it
        if (this.getSearchLimit() == 0 && this.getSearchReset() > Instant.now().getEpochSecond()) {
            throw new RateLimitException(this.getSearchReset(), "search");
        }

        Response r = request("search/code", params);
        String body = null;
        try {
            body = r.body().string();
        } catch (IOException e) {
            log.error("Error retrieving response body", e);
            System.exit(-1);
        }
        r.close();

        Queue<File> ret = parseSearchResults(body);

        long end = System.currentTimeMillis();

        timingLog.info("{} ~ {} ~ {} ~ search ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start, buildQuery(), body.length(), ret.size());

        lastPage++; // Successfully parsed the current page, move on
        return ret;
    }

    // Performs search, but sleeps if the API limit is reached
    public Queue<File> searchSleep() throws PageLimitException {
        while (true) {
            try {
                return search();
            } catch (SecondaryLimitException e) {
                // Sleep for a minute and try again
                log.debug("Hit secondary limit, waiting {} seconds", WAIT);
                try        
                {
                    TimeUnit.SECONDS.sleep(WAIT);
                } 
                catch(InterruptedException ex) 
                {
                    Thread.currentThread().interrupt();
                }
            } catch (RateLimitException e) {
                // Sleep until the refresh time
                log.debug("{}", e);

                long time = e.getResetTime() + 1 - Instant.now().getEpochSecond();
                try        
                {
                    if(time > 0){
                        TimeUnit.SECONDS.sleep(time); 
                    }
                } 
                catch(InterruptedException ex) 
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Returns the last page searched
     * 
     * @return The last page pulled (0 if no page pulled for this size)
     */
    public int lastPagePulled() {
        return lastPage;
    }

    /**
     * Returns the minimum file size being used to filter the results down to
     * 10 pages.
     * 
     * @return The current size used to filter
     */
    public int currentMinimumSize() {
        return minSize;
    }

    /**
     * Returns the maximum file size being used to filter the results down to
     * 10 pages
     * 
     * @return The current size used to filter
     */
    public int currentMaximumSize() {
        return maxSize;
    }

    public int getCoreLimit() {
        return this.coreLimit;
    }

    public long getCoreReset() {
        return this.coreReset;
    }

    public int getSearchLimit() {
        return this.searchLimit;
    }

    public long getSearchReset() {
        return this.searchReset;
    }

    public class SecondaryLimitException extends Exception {
        @Override
        public String toString() {
            return "Secondary rate limit reached for GitHub API. Try again in a few minutes.";
        }
    }

    public class PageLimitException extends Exception {
        @Override
        public String toString() {
            return "Reached page limit, all pages scanned";
        }
    }

    public class RateLimitException extends Exception {
        private long resetTime;
        private String type;

        public RateLimitException(long resetTime, String type) {
            this.resetTime = resetTime;
        }

        public long getResetTime() {
            return resetTime;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "RateLimitException for GitHub " + type + " API (resets "
                    + new SimpleDateFormat("HH:mm:ss z").format(new Date(this.getResetTime())) + ")";
        }

    }

}
