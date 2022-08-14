package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class ServerNameCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_SERVER,
            Pattern.CASE_INSENSITIVE);
    private static final String DBFIELD = "server_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes) {
        if (!RegexUtils.hasSpecificSingleKeyword(sqlCodes, RegexConstants.SERVER_KEYWORD))
            return SQLType.NONE;

        return RegexUtils.isSingleConcat(code, mStringLitWithPattern) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code) {
        return null;
    }

}
