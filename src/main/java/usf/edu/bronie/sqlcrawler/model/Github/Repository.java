package usf.edu.bronie.sqlcrawler.model.Github;

import java.util.List;

/**
 * Represents the results from a Github search code API call. 
 * 
 */

public class Repository {

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("node_id")
    private String node_id;
    public String getNode_id() {
        return node_id;
    }
    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("name")
    private String name;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("full_name")
    private String full_name;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("private")
    private boolean is_private;  //Should always be false
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("description")
    private String description;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("created_at")
    private String created_at;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("updated_at")
    private String updated_at;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("pushed_at")
    private String pushed_at; //The last time the repo had a commit pushed
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("size")
    private int repo_size;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("stargazers_count")
    private int stargazers_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("watchers_count")
    private int watchers_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("language")
    private String language;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("forks_count")
    private int forks_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("open_issues_count")
    private int open_issues_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("default_branch")
    private String default_branch;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("network_count")
    private int network_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("subscribers_count")
    private int subscribers_count;
}
