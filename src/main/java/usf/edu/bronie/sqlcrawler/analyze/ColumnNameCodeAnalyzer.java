package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class ColumnNameCodeAnalyzer implements CodeAnalyzer {
	private String mStringLitPattern = RegexConstants.STRING_LITERAL_COLUMN + RegexConstants.CONCAT_VAR;
	private String mStringLitPatternMultiple = RegexConstants.STRING_LITERAL_COLUMN + RegexConstants.CONCAT_VAR_MULTIPLE;

    //private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL_COLUMN + RegexConstants.CONCAT_VAR,
            //Pattern.CASE_INSENSITIVE);

    //private Pattern mStringLitPatternMultiple = Pattern.compile(RegexConstants.STRING_LITERAL_COLUMN + RegexConstants.CONCAT_VAR,
            //Pattern.CASE_INSENSITIVE);
    
    private static final String DBFIELD = "column_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, Languages language) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.COLUMN_KEYWORD))
            return SQLType.NONE;

        String variable = CodeAnalysisManager.getVariable(language);
        String concat = CodeAnalysisManager.getConcat(language);

        Pattern stringLiteralPatternJava = Pattern.compile(String.format(mStringLitPattern,
        		concat,
				variable),
	            Pattern.CASE_INSENSITIVE);

	    Pattern stringLitPatternMultipleJava = Pattern.compile(String.format(mStringLitPatternMultiple,
	    		concat,
				variable),
	            Pattern.CASE_INSENSITIVE);
	    
        return RegexUtils.isConcat(code, stringLiteralPatternJava) || RegexUtils.isConcat(code, stringLitPatternMultipleJava) ?
        		SQLType.STRING_CONCAT : SQLType.HARDCODED;

        
    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}
