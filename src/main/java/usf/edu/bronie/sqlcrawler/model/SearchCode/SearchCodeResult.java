package usf.edu.bronie.sqlcrawler.model.SearchCode;

import java.util.List;

/**
 * Represents the results from a SearchCode query. Includes metadata, such as the
 * page number, query, and total number of results. The {@link usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult#results results}
 * member is a {@link java.util.List List} containing the actual results for the given page number. 
 * See {@link usf.edu.bronie.sqlcrawler.model.SearchCode.Results Results class}.  
 * 
 */

public class SearchCodeResult {

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("source_filters")
    private List<SourceFilters> sourceFilters;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("nextpage")
    private int nextpage;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("page")
    private int page;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("results")
    private List<Results> results;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("total")
    private int total;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("language_filters")
    private List<LanguageFilters> languageFilters;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("query")
    private String query;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("searchterm")
    private String searchterm;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("previouspage")
    private int previouspage;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("matchterm")
    private String matchterm;

    public List<SourceFilters> getSourceFilters() {
        return sourceFilters;
    }

    public int getNextpage() {
        return nextpage;
    }

    public int getPage() {
        return page;
    }

    public List<Results> getResults() {
        return results;
    }

    public int getTotal() {
        return total;
    }

    public List<LanguageFilters> getLanguageFilters() {
        return languageFilters;
    }

    public String getQuery() {
        return query;
    }

    public String getSearchterm() {
        return searchterm;
    }

    public int getPreviouspage() {
        return previouspage;
    }

    public String getMatchterm() {
        return matchterm;
    }
}
