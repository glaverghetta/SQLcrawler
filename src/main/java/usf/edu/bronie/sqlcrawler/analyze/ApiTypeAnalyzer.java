package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.ApiType;

public class ApiTypeAnalyzer {

    public ApiType analyzeCode(String code){

        if (code.contains(RegexConstants.IMPORT_JPA))
            return ApiType.JPA;

        if (code.contains(RegexConstants.IMPORT_HIBERNATE))
            return ApiType.HIBERNATE;

        if (code.contains(RegexConstants.IMPORT_SPRING))
            return ApiType.SPRING;

        if (code.contains(RegexConstants.IMPORT_JDBC))
            return ApiType.JDBC;

        return ApiType.NONE;
    }
}
