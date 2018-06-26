package usf.edu.bronie.sqlcrawler.analyze;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class JavaCodeParser {

    public static CompilationUnit parseJavaCode(String code) {
        return JavaParser.parse(code);
    }
}
