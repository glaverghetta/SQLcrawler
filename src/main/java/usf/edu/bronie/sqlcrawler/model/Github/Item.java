package usf.edu.bronie.sqlcrawler.model.Github;

/**
 * Represents an individual result from a Github search code API call.
 * 
 *  
 * 
 */

public class Item {

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("name")
    private String name;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("path")
    private String path;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("sha")
    private String sha;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("url")
    private String url;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("git_url")
    private String git_url;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("html_url")
    private String html_url;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("repository")
    private Repository repository;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getSha() {
        return sha;
    }
    public void setSha(String sha) {
        this.sha = sha;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getGit_url() {
        return git_url;
    }
    public void setGit_url(String git_url) {
        this.git_url = git_url;
    }
    public String getHtml_url() {
        return html_url;
    }
    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }
    public Repository getRepository() {
        return repository;
    }
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    //Extracts the most recent commit for the HTML_url
    public String getCommit(){
        int commit_start = this.html_url.indexOf("/blob/") + "/blob/".length();
        return html_url.substring(commit_start, commit_start + 40);
    }

    public String getRawUrl(){
        return this.html_url.replaceFirst("/blob/", "/raw/");
    }
}
