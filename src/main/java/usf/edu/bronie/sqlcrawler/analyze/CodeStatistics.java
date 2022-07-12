package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.CodeStatData;
import usf.edu.bronie.sqlcrawler.model.SQLTypeDTO;

import java.util.HashMap;
import java.util.Map;

public class CodeStatistics {

    private Map mProjectMap = new HashMap<String, Integer>();

    private long mNumberOfFiles = 0;

    private long mNumberOfOrderByFiles = 0;

    private long mNumberOfProjects = 0;

    private CodeStatData mOrderByData = new CodeStatData();

    private CodeStatData mNoOrderByData = new CodeStatData();

    public void collectData(SQLTypeDTO sqlTypeDTO, String projectName) {

        if (sqlTypeDTO.isOrderByConcat()) mNumberOfOrderByFiles++;

        switch (sqlTypeDTO.getSQLType()) {
            case NONE:
                return;
            case HARDCODED:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incHardCoded();
                } else {
                    mNoOrderByData.incHardCoded();
                }
                break;
            case PARAMATIZED_QUERY:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incParamQuery();
                } else {
                    mNoOrderByData.incParamQuery();
                }
                break;
            case PARAMATIZED_QUERY_AND_CONCAT:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incParamQueryAndStringConcat();
                } else {
                    mNoOrderByData.incParamQueryAndStringConcat();
                }
                break;
            case STRING_CONCAT:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incStringConcat();
                } else {
                    mNoOrderByData.incStringConcat();
                }
                break;
            default:
                return;
        }

        collectStats(projectName);
    }

    public void printResults() {
        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files: " + mNumberOfFiles);
        System.out.println("Total number of project: " + mNumberOfProjects);
        System.out.println(" ====================================== ");
        System.out.println("Total number of files with order by concat: " + mNumberOfOrderByFiles);
        System.out.println("Total number of param query only: " + mOrderByData.getParamQuery());
        System.out.println("Total number of hardcoded string only: " + mOrderByData.getHardCoded());
        System.out.println("Total number of string concat only: " + mOrderByData.getStringConcat());
        System.out.println("Total number of param query and string concat: " + mOrderByData.getParamQueryAndStringConcat());
        System.out.println(" ====================================== ");
        System.out.println("Total number of files without order by concat: " + (mNumberOfFiles - mNumberOfOrderByFiles));
        System.out.println("Total number of param query only: " + mNoOrderByData.getParamQuery());
        System.out.println("Total number of hardcoded string only: " + mNoOrderByData.getHardCoded());
        System.out.println("Total number of string concat only: " + mNoOrderByData.getStringConcat());
        System.out.println("Total number of param query and string concat: " + mNoOrderByData.getParamQueryAndStringConcat());
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