package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.HashMap;
import java.util.Map;

public class CodeStatistics {

    private Map mProjectMap = new HashMap<String, Integer>();

    private long mNumberOfFiles = 0;

    private long mNumberOfProjects = 0;

    private long mParamQuery = 0;

    private long mStringConcat = 0;

    private long mHardCoded = 0;

    private long mParamQueryAndStringConcat = 0;

    public void collectData(SQLType type, String projectName) {

        switch (type) {
            case NONE: return;
            case HARDCODED: mHardCoded++; break;
            case PARAMATIZED_QUERY: mParamQuery++; break;
            case PARAMATIZED_QUERY_AND_CONCAT: mParamQueryAndStringConcat++; break;
            case STRING_CONCAT: mStringConcat++; break;
            default:return;
        }

        collectStats(projectName);
    }

    public void printResults() {
        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files: " + mNumberOfFiles);
        System.out.println("Total number of project: " + mNumberOfProjects);
        System.out.println(" ====================================== ");
        System.out.println("Total number of param query only: " + mParamQuery);
        System.out.println("Total number of hardcoded string only: " + mHardCoded);
        System.out.println("Total number of string concat only: " + mStringConcat);
        System.out.println("Total number of param query and string concat: " + mParamQueryAndStringConcat);
        System.out.println(" ====================================== ");
    }

    private void collectStats(String projectName) {
        mNumberOfFiles++;
        if (mProjectMap.get(projectName) == null) {
            mNumberOfProjects++;
            mProjectMap.put(projectName, 1);
        }
    }
}
