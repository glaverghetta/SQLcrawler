package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class ViewNameCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_VIEW,
            Pattern.CASE_INSENSITIVE);
    
    private static final String DBFIELD = "view_usage";

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificSingleKeyword(sqlCodes, RegexConstants.VIEW_KEYWORD))
            return SQLType.NONE;

        return RegexUtils.isSingleConcat(code, mStringLitPattern) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}
