package usf.edu.bronie.sqlcrawler.crawler;

import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;

/** 
 * Interface for Crawler
 * The goal of the crawler is to pull information about the repository,
 * such as statistics
 */

//TODO: Update to match new data types
public interface ProjectCrawler {
    
	GithubFileSpec getFileSpecByUrl(String url);  //Should probably be getProjectSpecByUrl
}


