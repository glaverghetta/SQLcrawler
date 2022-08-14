package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.List;

public interface CodeAnalyzer {

    public SQLType analyzeCode(String code, List sqlCodes);

    public SQLType analyzeCode(String code);

    public String getDBField();
}
