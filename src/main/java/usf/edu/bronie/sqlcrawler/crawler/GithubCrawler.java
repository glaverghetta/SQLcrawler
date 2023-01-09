package usf.edu.bronie.sqlcrawler.crawler;

import org.json.JSONObject;

import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;

//TODO: Update to match new DB interface

public class GithubCrawler implements ProjectCrawler{
	
    
    public GithubCrawler() {
    }

    /**
     * Returns repository information given a GitHub URL
     * Currently makes 4 requests to GitHub API per repository
     * Url should be in the formal https://github.com/owner/repo
     */
	@Override
	public GithubFileSpec getFileSpecByUrl(String url) {
		// Convert to URL for API calls
		// https://github.com/aseprite/aseprite -> 
		// https://api.github.com/repos/aseprite/aseprite
		String apiUrl = url.substring(0, 9) + "api." +
				url.substring(9, 19) + "/repos" +
				url.substring(19, url.length());
		
		GithubFileSpec.GithubFileSpecBuilder githubFileSpecBuilder = new GithubFileSpec.GithubFileSpecBuilder();
		
		// API call to GitHub API for repository information
		String s = HttpConnection.get(apiUrl);
		JSONObject json = new JSONObject(s);
		
		// Building the GithubFileSpec from the json data
		githubFileSpecBuilder.setStarCount(json.getInt("stargazers_count"));
		githubFileSpecBuilder.setWatchCount(json.getInt("watchers_count"));
		githubFileSpecBuilder.setForkCount(json.getInt("forks_count"));
		githubFileSpecBuilder.setTotalRelease(json.getString("updated_at"));
		githubFileSpecBuilder.setForkedFrom(json.getBoolean("fork"));
		githubFileSpecBuilder.setCommitDate(json.getString("pushed_at"));
		
		// Getting number of contributors
		String contributorsUrl = apiUrl + "/contributors?&per_page=1";
		String contributors = HttpConnection.getHeadersPageCount(contributorsUrl);
		githubFileSpecBuilder.setTotalContribution(contributors);
		
		// Getting number of commits
		String commitUrl = apiUrl + "/commits?&per_page=1";
		String commits = HttpConnection.getHeadersPageCount(commitUrl);
		githubFileSpecBuilder.setTotalCommit(commits);
		
		// Getting number of branches
		String branchesUrl = apiUrl + "/branches?&per_page=1";
		String branches = HttpConnection.getHeadersPageCount(branchesUrl);
		githubFileSpecBuilder.setTotalBranch(branches);

		return githubFileSpecBuilder.createGithubFileSpec();
	}

}
