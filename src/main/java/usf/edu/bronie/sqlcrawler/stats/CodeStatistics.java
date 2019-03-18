package usf.edu.bronie.sqlcrawler.stats;

import usf.edu.bronie.sqlcrawler.io.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CodeStatistics {
    final static String FIELD = "total_commit";

    private Connection mConnection = DBConnection.getConnection();

    public void printResults() throws SQLException {
        int greater = 0;
        int less = 1;


        float total = getCount("select count(*) from code_stats where url not like '%test%' and " + FIELD + "="
                + less + " and sql_usage!=0");

        String sqlHC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "=" + less + " " +
                "and sql_usage=1";
        String sqlC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "=" + less + " " +
                "and sql_usage=2";
        String sqlPC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "=" + less + " " +
                "and sql_usage=3";
        String sqlP = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "=" + less + " " +
                "and sql_usage=4";

        printResult(less, total, sqlHC, sqlC, sqlPC, sqlP);

        less = 50000;
        greater = 300;
        total = getCount("select count(*) from code_stats where url not like '%test%' and " + FIELD + "<" + less +
                " and " + FIELD + ">" + greater + "  and sql_usage!=0");

        sqlHC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "<" + less + " " +
                "and " + FIELD + ">" + greater + " and sql_usage=1";
        sqlC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "<" + less + " " +
                "and " + FIELD + ">" + greater + " and sql_usage=2";
        sqlPC = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "<" + less + " " +
                "and " + FIELD + ">" + greater + " and sql_usage=3";
        sqlP = "select count(*) from code_stats where url not like '%test%' and " + FIELD + "<" + less + " " +
                "and " + FIELD + ">" + greater + " and sql_usage=4";

        printResult(less, total, sqlHC, sqlC, sqlPC, sqlP);
    }

    private void printResult(int i, float total, String sqlHC, String sqlC, String sqlPC, String sqlP) throws SQLException {
        float countHC = getCount(sqlHC);
        float countC = getCount(sqlC);
        float countPC = getCount(sqlPC);
        float countP = getCount(sqlP);

        System.out.println(FIELD + ": " + i + " total: " + total);
        System.out.println("HardCoded: " + ((countHC * 100) / total));
        System.out.println("Concat: " + ((countC * 100) / total));
        System.out.println("Param & Concat: " + ((countPC * 100) / total));
        System.out.println("Param: " + ((countP * 100) / total));
        System.out.println("--");
    }

    public void printResultsYear() throws SQLException {

        for (int i = 3; i < 9; i++) {
            String year = "201" + i;
            float total = getCount("select count(*) from code_stats where url not like '%test%' and commit_date='"
                    + year + "' and sql_usage!=0");

            String sqlHC = "select count(*) from code_stats where url not like '%test%' and commit_date='" + year + "' " +
                    "and sql_usage=1";
            String sqlC = "select count(*) from code_stats where url not like '%test%' and commit_date='" + year + "' " +
                    "and sql_usage=2";
            String sqlPC = "select count(*) from code_stats where url not like '%test%' and commit_date='" + year + "' " +
                    "and sql_usage=3";
            String sqlP = "select count(*) from code_stats where url not like '%test%' and commit_date='" + year + "' " +
                    "and sql_usage=4";

            float countHC = getCount(sqlHC);
            float countC = getCount(sqlC);
            float countPC = getCount(sqlPC);
            float countP = getCount(sqlP);

            System.out.println("Year: " + year);
            System.out.println("HardCoded: " + ((countHC * 100) / total));
            System.out.println("Concat: " + ((countC * 100) / total));
            System.out.println("Param & Concat: " + ((countPC * 100) / total));
            System.out.println("Param: " + ((countP * 100) / total));
            System.out.println("==============================");
        }
    }

    public void printResultsAPI() throws SQLException {

        for (int i = 0; i < 1; i++) {
            String year = "none";
            float total = getCount("select count(*) from code_stats where url not like '%test%' and api_type='"
                    + year + "' and sql_usage!=0");

            String sqlHC = "select count(*) from code_stats where url not like '%test%' and api_type='" + year + "' " +
                    "and sql_usage=1";
            String sqlC = "select count(*) from code_stats where url not like '%test%' and api_type='" + year + "' " +
                    "and sql_usage=2";
            String sqlPC = "select count(*) from code_stats where url not like '%test%' and api_type='" + year + "' " +
                    "and sql_usage=3";
            String sqlP = "select count(*) from code_stats where url not like '%test%' and api_type='" + year + "' " +
                    "and sql_usage=4";

            float countHC = getCount(sqlHC);
            float countC = getCount(sqlC);
            float countPC = getCount(sqlPC);
            float countP = getCount(sqlP);

            System.out.println("API: " + year);
            System.out.println("HardCoded: " + ((countHC * 100) / total));
            System.out.println("Concat: " + ((countC * 100) / total));
            System.out.println("Param & Concat: " + ((countPC * 100) / total));
            System.out.println("Param: " + ((countP * 100) / total));
            System.out.println("==============================");
        }
    }

    private float getCount(String sql) throws SQLException {
        Statement stmt = mConnection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        resultSet.next();
        float count = resultSet.getInt("count(*)");
        resultSet.close();
        return count;
    }

}
