package usf.edu.bronie.sqlcrawler.model.SearchCode;

/**
 * Represents an individual file in the results from SearchCode. 
 * See {@link usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult SearchCodeResult}.
 * 
 */

public class Results {
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("filename")
    private String filename;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("id")
    private int id;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("md5hash")
    private String md5hash;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("url")
    private String url;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("name")
    private String name;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("location")
    private String location;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("linescount")
    private int linescount;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("language")
    private String language;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("repo")
    private String repo;

    public String getFilename() {
        return filename;
    }

    public int getId() {
        return id;
    }

    public String getMd5hash() {
        return md5hash;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getLinescount() {
        return linescount;
    }

    public String getLanguage() {
        return language;
    }

    public String getRepo() {
        return repo;
    }
}
