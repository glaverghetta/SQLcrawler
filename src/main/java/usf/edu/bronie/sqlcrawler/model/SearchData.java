package usf.edu.bronie.sqlcrawler.model;

public class SearchData {

    private String rawUrl;

    private String projectName;

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
