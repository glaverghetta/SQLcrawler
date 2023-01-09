package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.List;

public interface CodeAnalyzer {
	public SQLType analyzeCode(String code, List<String> sqlCodes, RegexConstants.Languages language);

	public SQLType analyzeCode(String code, RegexConstants.Languages language);

	public String getDBField();
}
