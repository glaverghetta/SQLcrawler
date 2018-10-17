package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableNameCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithFromIntoPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_TABLE,
            Pattern.CASE_INSENSITIVE);

    @Override
    public SQLType analyzeCode(String code, List sqlCodes) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.TABLE_KEYWORD))
            return SQLType.NONE;

        return RegexUtils.isConcat(code, mStringLitWithFromIntoPattern) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}