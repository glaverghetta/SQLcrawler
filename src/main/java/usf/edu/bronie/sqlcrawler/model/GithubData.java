package usf.edu.bronie.sqlcrawler.model;

public class GithubData {
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String url;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public GithubData(String name, String url) {
    	this.name = name;
    	this.url = url;
    }
}
