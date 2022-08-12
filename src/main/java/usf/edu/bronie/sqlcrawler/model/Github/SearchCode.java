package usf.edu.bronie.sqlcrawler.model.Github;

import java.util.List;

/**
 * Represents the results from a Github search code API call. 
 * 
 */

public class SearchCode {

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("total_count")
    private String total_count;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("incomplete_results")
    private boolean incomplete_results;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("items")
    private List<Item> items;

    //Getters and setters
    public String getTotal_count() {
        return total_count;
    }
    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }
    public boolean isIncomplete_results() {
        return incomplete_results;
    }
    public void setIncomplete_results(boolean incomplete_results) {
        this.incomplete_results = incomplete_results;
    }
    public List<Item> getItems() {
        return items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
    }

    
}
