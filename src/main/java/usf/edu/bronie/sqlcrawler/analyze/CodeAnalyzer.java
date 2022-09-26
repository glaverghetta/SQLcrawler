package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.List;

public interface CodeAnalyzer {

    public static String getVariable(RegexConstants.Languages language) {
    	switch(language) {
    		case JAVA:
    			return RegexConstants.JAVA_VARIABLE;
    		case PHP:
    			return RegexConstants.PHP_VARIABLE;
    	}
    	return "";
    }
    
    public static String getConcat(RegexConstants.Languages language) {
    	switch(language) {
    		case JAVA:
    			return RegexConstants.JAVA_CONCAT;
    		case PHP:
    			return RegexConstants.PHP_CONCAT;
    	}
    	return "";
    }

    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language);

    public SQLType analyzeCode(String code);

    public String getDBField();
}
