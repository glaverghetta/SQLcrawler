package usf.edu.bronie.sqlcrawler.io;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.Project;
import usf.edu.bronie.sqlcrawler.model.ProjectStats;
import usf.edu.bronie.sqlcrawler.model.Github.Item;
import usf.edu.bronie.sqlcrawler.model.Github.SearchCode;
import usf.edu.bronie.sqlcrawler.model.Project.noProjectFound;

/**
 * Retrieves data from Github using the official Github API
 */
public class GithubAPI {

    private static final Logger log = LogManager.getLogger(GithubAPI.class);
    private static final Logger timingLog = LogManager.getLogger("GithubAPILogger");
    private static final Logger networkTimingLog = LogManager.getLogger("NetworkLogger");
    private static final Logger throttleTimingLog = LogManager.getLogger("GithubThrottlingLogger");

    private static final String githubURL = "api.github.com";
    private static final String graphQLQuery = setupGraphQL();

    private int lastPage = 0;
    private int lastTotalCount = -1;

    private int minSize;
    private int maxSize;
    private Languages language;

    private final int SIZE_LIMIT = 100 * 1000000; // 100 MB
    private final int PER_PAGE = 100; // TODO: Make this non-constant
    private final int MAX_PAGE = 1000 / PER_PAGE; // 1000 is the hard-limit cap of results from GH
    private final int WAIT = 60; // TODO: Make this non-constant

    // Start all limits/times as unknown
    private int searchLimit = -1;
    private long searchReset = -1; // In Epoch time
    private int coreLimit = -1;
    private long coreReset = -1; // In Epoch time
    private int graphQLLimit = -1;
    private long graphQLReset = -1; // In Epoch time

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
            log.error("Invalid page number {} (expects 1-{})", page, MAX_PAGE);
            System.exit(-1);
        }

        // For 1 this becomes 0, which acts as if a new size was added
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
            Call c = client.newCall(request);
            long start = System.currentTimeMillis();
            r = c.execute();
            long end = System.currentTimeMillis();

            networkTimingLog.info("{} ~ {} ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start, url, r.code());
        } catch (SocketTimeoutException e) {
            //TODO: Handle this more dynamically
            log.debug("Socket timeout.  Trying a second time.");

            try {
                Call c = client.newCall(request);
                long start = System.currentTimeMillis();
                r = c.execute();
                long end = System.currentTimeMillis();

                networkTimingLog.info("{} ~ {} ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start, url,
                        r.code());
            } catch (IOException e2) {
                log.error("Failed to reach Github API", e2);
                System.exit(-1);
            }

        } catch (IOException e) {
            log.error("Failed to reach Github API", e);
            System.exit(-1);
        }
        return r;
    }

    private Response postURL(String endpoint, String bodyString) {

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

        // Build the url and make the request
        HttpUrl url = urlBuilder.build();

        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(new JSONObject().put("query", bodyString).toString(), JSON);

        OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(body)
                .build();
        log.debug("Getting {}", url);

        Response r = null;
        try {
            Call c = client.newCall(request);
            long start = System.currentTimeMillis();
            r = c.execute();
            long end = System.currentTimeMillis();

            networkTimingLog.info("{} ~ {} ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start, url, r.code());
        } catch (SocketTimeoutException e) {
            //TODO: Handle this more dynamically
            log.debug("Socket timeout.  Trying a second time.");

            try {
                Call c = client.newCall(request);
                long start = System.currentTimeMillis();
                r = c.execute();
                long end = System.currentTimeMillis();
    
                networkTimingLog.info("{} ~ {} ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start, url, r.code());
            }
            catch (IOException e2) {
                log.error("Failed to reach Github API", e2);
                System.exit(-1);
            }
        }
        catch (IOException e) {
            log.error("Failed to reach Github API", e);
            System.exit(-1);
        }

        return r;
    }

    private void updateRateLimit(Response r) {
        if (r.header("x-ratelimit-resource").equals("search")) {
            this.searchLimit = Integer.parseInt(r.header("x-ratelimit-remaining"));
            this.searchReset = Long.parseLong(r.header("x-ratelimit-reset"));
        } else if (r.header("x-ratelimit-resource").equals("core")) {
            this.coreLimit = Integer.parseInt(r.header("x-ratelimit-remaining"));
            this.coreReset = Long.parseLong(r.header("x-ratelimit-reset"));
        } else if (r.header("x-ratelimit-resource").equals("graphql")) {
            this.graphQLLimit = Integer.parseInt(r.header("x-ratelimit-remaining"));
            this.graphQLReset = Long.parseLong(r.header("x-ratelimit-reset"));
        } else {
            log.error("Unknown API type: {}", r.header("x-ratelimit-resource"));
            System.exit(-1);
        }
    }

    private void handleErrorCode(String url, Response r) throws RateLimitException, SecondaryLimitException {
        if (r.code() == 403) {
            // Check regular limit (may occur if user is using API elsewhere)
            if (Integer.parseInt(r.header("x-ratelimit-remaining")) == 0) {
                Long reset = Long.parseLong(r.header("x-ratelimit-reset")) * 1000; // Seconds to milliseconds
                throttleTimingLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {}", r.header("x-ratelimit-resource"),
                        new Date(reset), reset - System.currentTimeMillis(), this.lastPage + 1, this.minSize,
                        this.maxSize);
                r.close();
                throw new RateLimitException(Long.parseLong(r.header("x-ratelimit-reset")) * 1000,
                        r.header("x-ratelimit-resource"));
            }
            r.close();

            // Must be secondary
            Long current = System.currentTimeMillis();
            throttleTimingLog.info("Secondary ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(current + 60000), 60000,
                    this.lastPage + 1, this.minSize, this.maxSize);
            throw new SecondaryLimitException();
        } else if (r.code() == 401) {
            log.error("Bad credentials for Github API");
            System.exit(-1);
        } else if (r.code() != 200 && r.code() != 304) { // 200 OK, 304 Cache hit
            log.error("Unhandled HTTP code: {}", r.code());
            System.exit(-1);
        }
        log.debug("Request to {} returned code {}", url, r.code());
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

        // Check response code
        handleErrorCode(url, r);

        // Rate limits always included regardless of response
        updateRateLimit(r);

        return r;
    }

    private Response postRequest(String url, String body)
            throws RateLimitException, SecondaryLimitException {
        Response r = postURL(url, body);

        // Check response code
        handleErrorCode(url, r);

        // Rate limits always included regardless of response
        updateRateLimit(r);

        return r;
    }

    private void parseGraphQLResults(String results) {
        JSONObject all = new JSONObject(results);
        JSONObject data = all.getJSONObject("data");

        Iterator<String> keys = data.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            // Ignore nulls
            if ((data.isNull(key))) {
                continue;
            }

            if (data.get(key) instanceof JSONObject) {
                // do something with jsonObject here
                try {
                    new ProjectStats(data.getJSONObject(key)).save();
                } catch (noProjectFound e) {
                    log.error("Could not find project to save", e);
                    System.exit(-1);
                }
            }
        }

        if (all.has("errors")) {
            JSONArray errors = all.getJSONArray("errors");

            for (int i = 0; i < errors.length(); i++) {
                String msg = errors.getJSONObject(i).getString("message");
                if (errors.getJSONObject(i).getString("type").equals("NOT_FOUND")) {
                    // Message contains repo url in single quotes, like 'URL'
                    try {
                        ProjectStats.makeNullEntry(
                                Integer.parseInt(errors.getJSONObject(i).getJSONArray("path").getString(0).replace("p", "")));
                    } catch (noProjectFound e) {
                        log.error("Could not find project to save", e);
                        System.exit(-1);
                    } catch (JSONException e){
                        log.error("Error with JSON format: {}", errors.getJSONObject(i), e);     
                        System.exit(-1);
                    }
                } else {
                    log.error("Unknown error reading GraphQL error message: {} {}",
                            errors.getJSONObject(i).getString("type"), msg);
                    System.exit(-1);
                }
            }
        }
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
            mQueue.add(new File(r.getRepository().getNode_id(), r.getName(), r.getPath(), r.getRawUrl(), r.getSha(),
                    r.getCommit(), this.language));
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
        if (this.getSearchLimit() == 0 && this.getSearchReset() * 1000 > System.currentTimeMillis()) {
            Long reset = this.getSearchReset() * 1000;
            throttleTimingLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {}", "search",
                    new Date(reset), reset - System.currentTimeMillis(), this.lastPage + 1, this.minSize,
                    this.maxSize);
            throw new RateLimitException(this.getSearchReset() * 1000, "search");
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

        timingLog.info("{} ~ {} ~ {} ~ search ~ {} ~ {} ~ {} ~ {}", new Date(start), new Date(end), end - start,
                this.lastPage + 1,
                buildQuery(), body.length(), ret.size());

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
                try {
                    TimeUnit.SECONDS.sleep(WAIT);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (RateLimitException e) {
                // Sleep until the refresh time
                log.debug("{}", e);

                long time = e.getResetTime() * 1000 + 1 - System.currentTimeMillis();
                try {
                    if (time > 0) {
                        TimeUnit.SECONDS.sleep(time);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Performs project search, but sleeps if the API limit is reached
    public void projectSleep(Set<Project> projects, int maxNum) {
        while (true) {
            try {
                searchProject(projects, maxNum);
                return;
            } catch (SecondaryLimitException e) {
                // Sleep for a minute and try again
                log.debug("Hit secondary limit, waiting {} seconds", WAIT);
                try {
                    TimeUnit.SECONDS.sleep(WAIT);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (RateLimitException e) {
                // Sleep until the refresh time
                log.debug("{}", e);

                long time = e.getResetTime() * 1000 + 1 - System.currentTimeMillis();
                try {
                    if (time > 0) {
                        TimeUnit.SECONDS.sleep(time);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static String setupGraphQL() {
        String out = "";
        try {
            String text = new String(GithubAPI.class.getResourceAsStream("/graphQLQuery.txt").readAllBytes());
            out = String.join("\n", text);
        } catch (Exception e) {
            // TODO: handle exception
            log.error("Error reading GraphQL resource file", e);
            System.exit(-1);
        }
        return out;
    }

    public void searchProject(Set<Project> projects, int maxNum)
            throws RateLimitException, SecondaryLimitException {

        int count = 0;
        String query = "query {\n";

        Set<Project> toRemove = new HashSet<>();
        for (Project p : projects) {
            if (count >= maxNum) {
                break;
            }

            toRemove.add(p);

            query += "p" + p.getId() + ": ";

            // Saved as owner/name, query wants name first (name: %s, owner: %s)
            query += String.format(GithubAPI.graphQLQuery, p.getName(), p.getOwner());

            count++;
        }
        query += "}";

        long start = System.currentTimeMillis();

        // Check the graphQL API limit, check if we can use it
        if (this.getGraphQLLimit() == 0 && this.getGraphQLReset() * 1000 > System.currentTimeMillis()) {
            Long reset = this.getGraphQLReset() * 1000;
            throttleTimingLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {}", "search",
                    new Date(reset), reset - System.currentTimeMillis(), this.lastPage + 1, this.minSize,
                    this.maxSize);
            throw new RateLimitException(this.getGraphQLReset() * 1000, "graphQL");
        }

        Response r = postRequest("graphql", query);
        String body = null;
        try {
            body = r.body().string();
        } catch (IOException e) {
            log.error("Error retrieving response body", e);
            System.exit(-1);
        }
        r.close();

        parseGraphQLResults(body);

        long end = System.currentTimeMillis();

        timingLog.info("{} ~ {} ~ {} ~ graphql ~ {} ~ {} ~ {} ~ {}", new Date(start),
                new Date(end), end - start, this.lastPage + 1,
                "", body.length(), projects.size());
        projects.removeAll(toRemove);
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

    public int getGraphQLLimit() {
        return this.graphQLLimit;
    }

    public long getGraphQLReset() {
        return this.graphQLReset;
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
