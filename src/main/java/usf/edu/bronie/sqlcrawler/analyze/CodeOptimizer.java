package usf.edu.bronie.sqlcrawler.analyze;

public class CodeOptimizer {

    public static String optimizeCode(String code) {
        String args[] = {"-O", "/Users/cagricetin/Downloads/test.java"};
        soot.Main.main(args);

        return null;
    }
}
