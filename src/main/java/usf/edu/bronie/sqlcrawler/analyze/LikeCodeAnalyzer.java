package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    private Pattern mStringLitWithLikePattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_LIKE,
            Pattern.CASE_INSENSITIVE);

    private Pattern mStringLikePrepPattern = Pattern.compile(RegexConstants.STRING_LITERAL_PREP_STATE_LIKE,
            Pattern.CASE_INSENSITIVE);
            
    private static final String DBFIELD = "like_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        // return null; //Old code
        return analyzeCode(code);
    }

    public SQLType analyzeCode(String code) {
        if (!RegexUtils.hasSpecificKeyword(code, RegexConstants.LIKE_KEYWORD))
            return SQLType.NONE;

        if (isLikeConcat(code)) {
            return SQLType.STRING_CONCAT;
        }

        if (isLikePrepStatement(code)) {
            return SQLType.PARAMATIZED_QUERY;
        }

        return SQLType.HARDCODED;
    }

    private boolean containsLike(String keyword) {
        // if (StringUtils.containsIgnoreCase(keyword, RegexConstants.LIKE_KEYWORD)) {
        // return true;
        // }

        return false;
    }

    private boolean isLikeConcat(String code) {
        Matcher stringLitWithOrderByMatcher = mStringLitWithLikePattern.matcher(code);
        while (stringLitWithOrderByMatcher.find()) {
            String keyword = stringLitWithOrderByMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikePrepStatement(String code) {
        Matcher stringLitMatcher = mStringLitPattern.matcher(code);
        while (stringLitMatcher.find()) {
            String keyword = stringLitMatcher.group();
            if (RegexUtils.isSQLCode(keyword) && containsLike(keyword)) {
                Matcher matcher = mStringLikePrepPattern.matcher(keyword);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }
}
