package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class GroupOrderByCodeAnalyzer implements CodeAnalyzer {

	 private String mStringLitPattern = RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR;
	 private String mStringLitPatternMultiple = RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR_MULTIPLE;
	/*
    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR,
            Pattern.CASE_INSENSITIVE);
    
    private Pattern mStringLitPatternMultiple = Pattern.compile(RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR_MULTIPLE,
    		Pattern.CASE_INSENSITIVE);
    */
    
    private static final String DBFIELD = "order_group_usage";

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.GROUP_ORDER_BY_KEYWORD)) return SQLType.NONE;

        String variable = CodeAnalyzer.getVariable(language);
        String concat = CodeAnalyzer.getConcat(language);

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
