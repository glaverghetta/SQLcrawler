package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class EventNameCodeAnalyzer implements CodeAnalyzer {

    private String mStringLitWithPattern = RegexConstants.STRING_LITERAL_CONCAT_WITH_EVENT;

    RegexConstants.Languages CompiledLang = null;
    Pattern stringLiteralPatternCompiled;
    
    private static final String DBFIELD = "event_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificSingleKeyword(sqlCodes, RegexConstants.EVENT_KEYWORD))
            return SQLType.NONE;

        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);

        if(language != CompiledLang){
            CompiledLang = language;
            stringLiteralPatternCompiled = Pattern.compile(String.format(mStringLitWithPattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
  
        }
        
        return RegexUtils.isSingleConcat(code, stringLiteralPatternCompiled) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        return null;
    }

}
