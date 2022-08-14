package usf.edu.bronie.sqlcrawler.model;

/**
 * A generic class representing the results received from a provider.
 * For example, the {@link usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult SearchCodeResult}
 * class represents all of the data from SearchCode (including search metadata), which is then filtered 
 * and returned as {@link usf.edu.bronie.sqlcrawler.model.SearchData SearchData} from the 
 * {@link usf.edu.bronie.sqlcrawler.provider.SearchCodeProvider SearchCodeProvider}.
 * 
 * TODO: Will want to add some additional fields, such as repo url, filename, etc.   
 */

public class SearchData {

    private String projectName;

    private String rawUrl;

    private String code;

    public SearchData(String rawUrl, String projectName) {
        this.rawUrl = rawUrl;
        this.projectName = projectName;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
