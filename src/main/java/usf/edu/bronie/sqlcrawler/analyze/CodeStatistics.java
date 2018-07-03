package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.CodeStatData;
import usf.edu.bronie.sqlcrawler.model.SQLTypeDTO;

import java.util.HashMap;
import java.util.Map;

public class CodeStatistics {

    private Map mProjectMap = new HashMap<String, Integer>();

    private long mNumberOfFiles = 0;

    private long mNumberOfLikeConcatFiles = 0;

    private long mNumberOfLikeParamStateFiles = 0;

    private long mNumberOfProjects = 0;

    private CodeStatData mOrderByData = new CodeStatData();

    private CodeStatData mNoOrderByData = new CodeStatData();

    private CodeStatData mlikeByPrep = new CodeStatData();

    public void collectData(SQLTypeDTO sqlTypeDTO, String projectName) {

        if (sqlTypeDTO.isOrderByConcat())
            mNumberOfLikeConcatFiles++;
        else if (sqlTypeDTO.isLikePrep())
            mNumberOfLikeParamStateFiles++;

        switch (sqlTypeDTO.getSQLType()) {
            case NONE:
                return;
            case HARDCODED:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incHardCoded();
                } else {
                    if (sqlTypeDTO.isLikePrep())
                        mlikeByPrep.incHardCoded();
                    else
                        mNoOrderByData.incHardCoded();
                }
                break;
            case PARAMATIZED_QUERY:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incParamQuery();
                } else {
                    if (sqlTypeDTO.isLikePrep())
                        mlikeByPrep.incParamQuery();
                    else
                        mNoOrderByData.incParamQuery();
                }
                break;
            case PARAMATIZED_QUERY_AND_CONCAT:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incParamQueryAndStringConcat();
                } else {
                    if (sqlTypeDTO.isLikePrep())
                        mlikeByPrep.incParamQueryAndStringConcat();
                    else
                        mNoOrderByData.incParamQueryAndStringConcat();
                }
                break;
            case STRING_CONCAT:
                if (sqlTypeDTO.isOrderByConcat()) {
                    mOrderByData.incStringConcat();
                } else {
                    if (sqlTypeDTO.isLikePrep())
                        mlikeByPrep.incStringConcat();
                    else
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
        System.out.println("Total number of files with like concat: " + mNumberOfLikeConcatFiles);
        System.out.println("Total number of param query only: " + mOrderByData.getParamQuery());
        System.out.println("Total number of hardcoded string only: " + mOrderByData.getHardCoded());
        System.out.println("Total number of string concat only: " + mOrderByData.getStringConcat());
        System.out.println("Total number of param query and string concat: " + mOrderByData.getParamQueryAndStringConcat());
        System.out.println(" ====================================== ");
        System.out.println("Total number of files hardcoded like: " + (mNumberOfFiles - mNumberOfLikeConcatFiles
                - mNumberOfLikeParamStateFiles));
        System.out.println("Total number of param query only: " + mNoOrderByData.getParamQuery());
        System.out.println("Total number of hardcoded string only: " + mNoOrderByData.getHardCoded());
        System.out.println("Total number of string concat only: " + mNoOrderByData.getStringConcat());
        System.out.println("Total number of param query and string concat: " + mNoOrderByData.getParamQueryAndStringConcat());
        System.out.println(" ====================================== ");
        System.out.println("Total number of files with like prepared: " + mNumberOfLikeParamStateFiles);
        System.out.println("Total number of param query only: " + mlikeByPrep.getParamQuery());
        System.out.println("Total number of hardcoded string only: " + mlikeByPrep.getHardCoded());
        System.out.println("Total number of string concat only: " + mlikeByPrep.getStringConcat());
        System.out.println("Total number of param query and string concat: " + mlikeByPrep.getParamQueryAndStringConcat());
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
