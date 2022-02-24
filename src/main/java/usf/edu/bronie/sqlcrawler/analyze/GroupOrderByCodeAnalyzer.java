package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class GroupOrderByCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_GROUP_ORDER_BY,
            Pattern.CASE_INSENSITIVE);

    @Override
    public SQLType analyzeCode(String code, List sqlCodes) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.GROUP_ORDER_BY_KEYWORD)) return SQLType.NONE;

        return RegexUtils.isConcat(code, mStringLitPattern) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}
