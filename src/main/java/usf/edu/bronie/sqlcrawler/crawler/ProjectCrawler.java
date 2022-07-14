package usf.edu.bronie.sqlcrawler.crawler;

import java.util.regex.Pattern;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;

/** 
 * Interface for Crawler
 * The goal of the crawler is to pull information about the repository,
 * such as statistics
 */
public interface ProjectCrawler {
    
	GithubFileSpec getFileSpecByUrl(String url);
}


