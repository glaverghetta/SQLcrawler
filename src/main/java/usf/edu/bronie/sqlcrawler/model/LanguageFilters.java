package usf.edu.bronie.sqlcrawler.model;

public class LanguageFilters {
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("language")
    private String language;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("id")
    private int id;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("count")
    private int count;

    public String getLanguage() {
        return language;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }
}
