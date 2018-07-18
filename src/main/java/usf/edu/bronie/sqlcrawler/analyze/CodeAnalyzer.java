package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.model.SQLType;

public interface CodeAnalyzer {

    public SQLType analyzeCode(String code);
}
