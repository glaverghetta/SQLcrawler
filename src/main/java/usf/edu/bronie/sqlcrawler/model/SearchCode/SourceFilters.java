package usf.edu.bronie.sqlcrawler.model.SearchCode;

/**
 * Represents the various source (i.e., Github, BitBucket) filters used to obtain the results from SearchCode. 
 * See {@link usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult SearchCodeResult}.
 * 
 */


public class SourceFilters {
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("id")
    private int id;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("source")
    private String source;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("count")
    private int count;

    public int getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public int getCount() {
        return count;
    }
}
