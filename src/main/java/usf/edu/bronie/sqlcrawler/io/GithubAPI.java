package usf.edu.bronie.sqlcrawler.io;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

public class GithubAPI {

    private static final Logger log = LoggerFactory.getLogger(GithubAPI.class);

    private static final String githubURL = "https://api.github.com/";

    // Start all limits/times as unknown
    private int searchLimit = -1;
    private long searchReset = -1; // In Epoch time
    private int coreLimit = -1;
    private long coreReset = -1; // In Epoch time

    private Response getURL(String endpoint, Map<String, String> params) {

        Cache cache = new Cache(new File(CredentialConstants.GITHUB_CACHE_FILE), CredentialConstants.GITHUB_CACHE_SIZE); // 10MB
                                                                                                                         // cache

        Response r = null;
        Map<String, String> headers = Map.of(
                "Accept", "application/vnd.github+json",
                "Authorization", "token " + CredentialConstants.GITHUB_TOKEN);

        String url = githubURL + endpoint;

        int start = 1;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if(start == 1){
                url = url + "?" + entry.getKey() + "=" + entry.getValue();
                start--;
            }
            else{
                url = url + "&" + entry.getKey() + "=" + entry.getValue();
            }
        }

        OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();
        log.debug("Getting {}", url);

        try {
            r = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("Failed to reach Github API", e);
            System.exit(-1);
        }
        return r;
    }

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

    public String search(int page, String language) throws RateLimitException, SecondaryLimitException {

        Map<String, String> params = new HashMap<>();

        // Add the query string for the language
        String q = "";
        if (language.equals("Java")) {
            q = RegexConstants.JAVA_SEARCH_TERMS;
        } else {
            log.error("Unknown language requested {}", language);
            System.exit(-1);
        }

        params.put("q", q + "+language:" + language);
        params.put("per_page", "100");
        params.put("page", String.valueOf(page));

        // Check the search API limit, check if we can use it
        if (this.getSearchLimit() == 0 && this.getSearchReset() > Instant.now().getEpochSecond()) {
            throw new RateLimitException(this.getSearchReset(), "search");
        }

        Response r = request("search/code", params);
        String ret = null;
        try{
            ret = r.body().string();
        }catch(IOException e)
        {
            log.error("Error retrieving response body", e);
            System.exit(-1);
        }

        return ret;
    }

    // Performs search, but sleeps if the API limit is reached
    public String searchSleep(int page, String language) {

        while (true) {
            try {
                return search(page, language);
            } catch (SecondaryLimitException e) {
                // Sleep for a minute and try again
            } catch (RateLimitException e) {
                // Sleep until the refresh time
            }
        }
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
