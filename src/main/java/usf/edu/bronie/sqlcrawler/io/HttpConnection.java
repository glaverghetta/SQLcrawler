package usf.edu.bronie.sqlcrawler.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HttpConnection {

    public static String get(String url) {
        try {
            System.out.println(url);
            URLConnection conn = new URL(url).openConnection();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (IOException e) {
        }
        return null;
    }
   
    /**
     * This method will specifically get the number of pages, when querying
     * the GitHub API for 1/page. This value is also equal to certain values that
     * GitHub does not explicitly give regarding an API
     * Needs extra support to handle 0/1 cases
     * @param url
     * @return Number of pages
     */
    public static String getHeadersPageCount(String url) {
    	try {
            URLConnection conn = new URL(url).openConnection();
            List<String> headerFields = conn.getHeaderFields().get("Link");
            if(headerFields==null) {
            	// 0 or 1 outlier 
            	return String.valueOf(1);
            }
            String header = headerFields.get(0);
            int first = header.lastIndexOf("page=");
    		int last = header.lastIndexOf("rel=\"last\"");
    		return header.substring(first+5, last-3);
    		
        } catch (IOException e) {
        }
    	return null;
    }
}
