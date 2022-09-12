package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class GroupOrderByCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR,
            Pattern.CASE_INSENSITIVE);
    
    private Pattern mStringLitPatternMultiple = Pattern.compile(RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR_MULTIPLE,
    		Pattern.CASE_INSENSITIVE);
    
    private static final String DBFIELD = "order_group_usage";

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.GROUP_ORDER_BY_KEYWORD)) return SQLType.NONE;

        return RegexUtils.isConcat(code, mStringLitPattern)  || RegexUtils.isConcat(code, mStringLitPatternMultiple)? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}
