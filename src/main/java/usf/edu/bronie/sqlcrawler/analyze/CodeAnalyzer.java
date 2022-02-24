package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.List;

public interface CodeAnalyzer {

    SQLType analyzeCode(String code, List sqlCodes);

    SQLType analyzeCode(String code);
}
