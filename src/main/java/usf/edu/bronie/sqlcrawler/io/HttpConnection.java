package usf.edu.bronie.sqlcrawler.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that provides functions for retrieving data over HTTP(S).
 */

public class HttpConnection {

    private static final Logger log = LoggerFactory.getLogger(HttpConnection.class);

    // Should switch these over to using OKHttp

    /**
     * Sends an HTTP GET request to the provided URL and returns the resulting data.
     * No HTTP codes are checked; the url is assumed to always be accessible
     * 
     * @param url, String containing the URL to get
     * @return the results as a String
     */
    public static String get(String url) {
        try {
            URLConnection conn = new URL(url).openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (IOException e) {
            log.error("Error retrieving {}", url, e);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Sends an HTTP GET request to the provided URL and returns the resulting data.
     * Allows for additional headers to be added to the request.
     * 
     * @param url,     String containing the URL to get
     * @param headers, Map<String, String> containing the headers to add to the
     *                 request
     * @return the results as a String
     * 
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            URLConnection conn = new URL(url).openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;

            // Add the requested headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpConn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            switch (httpConn.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    // Ok
                    break;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    log.error(" HTTP Error **gateway timeout**");
                    System.exit(-1);
                    break;// retry
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    log.error(" HTTP Error **Unavailable**");
                    System.exit(-1);
                    break;// retry, server is unstable
                default:
                    log.error(" HTTP Error **Unknown** {} {}", url, httpConn.getResponseCode());
                    System.exit(-1);
                    break; // abort
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (IOException e) {
            log.error("Error retrieving {}", url, e);
            System.exit(-1);
        }
        return null;
    }

    /**
     * This method will specifically get the number of pages, when querying
     * the GitHub API for 1/page. This value is also equal to certain values that
     * GitHub does not explicitly give regarding an API
     * Needs extra support to handle 0/1 cases
     * 
     * @param url
     * @return Number of pages
     */
    public static String getHeadersPageCount(String url) {
        try {
            URLConnection conn = new URL(url).openConnection();
            List<String> headerFields = conn.getHeaderFields().get("Link");
            if (headerFields == null) {
                // 0 or 1 outlier
                return String.valueOf(1);
            }
            String header = headerFields.get(0);
            int first = header.lastIndexOf("page=");
            int last = header.lastIndexOf("rel=\"last\"");
            return header.substring(first + 5, last - 3);

        } catch (IOException e) {
            log.error("Error retrieving {}", url, e);
            System.exit(-1);
        }
        return null;
    }
}
