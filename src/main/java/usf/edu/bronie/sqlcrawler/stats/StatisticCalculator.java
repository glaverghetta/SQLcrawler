package usf.edu.bronie.sqlcrawler.stats;

import usf.edu.bronie.sqlcrawler.io.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatisticCalculator {

    private Connection mConnection = DBConnection.getConnection();

    private Map<String, Model> mMap = new HashMap();

    public void calcStats() {
        calcAllUniqueProjects();

        try {
            Statement stmt = mConnection.createStatement();

            Iterator it = mMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String name = (String) pair.getKey();
                Model value = (Model) pair.getValue();

                String sql1 = "select * from project_specs where project_name='" + name + "';";

                ResultSet rs = stmt.executeQuery(sql1);
                rs.next();
                String last_commit = rs.getString("last_commit_date");
                String forked_from = rs.getString("forked_from");
                int fork_count = rs.getInt("fork_count");
                int watch_count = rs.getInt("watch_count");
                int star_count = rs.getInt("star_count");
                int total_commit = rs.getInt("total_commit");
                int total_branch = rs.getInt("total_branch");
                int total_release = rs.getInt("total_release");
                int total_contr = rs.getInt("total_contr");

                String sql = "INSERT INTO PROJECT_STATS (project_name, param_count, param_and_concat_count, concat_count," +
                        " hardcode_count, fork_count, watch_count, star_count, last_commit_date, forked_from, total_commit," +
                        " total_branch, total_release, total_contr) VALUES ('" + name + "', " + value.param + ", "
                        + value.param_and_concat + ", " + value.concat + ", " + value.hardcode + ", " + fork_count +
                        ", " + watch_count + ", " + star_count + ", '" + last_commit + "', '" + forked_from +
                        "', " + total_commit + ", " + total_branch + ", " + total_release + ", " + total_contr + ");";

//                String sql = "INSERT INTO pstat (project_name, param_count, param_and_concat_count, concat_count," +
//                        " hardcode_count) VALUES ('" + name + "', " + value.param + ", " + value.param_and_concat + "," +
//                        " " + value.concat + ", " + value.hardcode + ");";

                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calcAllUniqueProjects() {
        try {
            Statement stmt = mConnection.createStatement();
            String sql = "select distinct project_name from code_specs where sql_usage='PARAMATIZED_QUERY';";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("project_name");
                Model model = mMap.get(name);
                if (model == null) {
                    model = new Model();
                }

                model.param++;
                mMap.put(name, model);
            }

            sql = "select distinct project_name from code_specs where sql_usage='PARAMATIZED_QUERY_AND_CONCAT';";

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("project_name");
                Model model = mMap.get(name);
                if (model == null) {
                    model = new Model();
                }

                model.param_and_concat++;
                mMap.put(name, model);
            }

            sql = "select distinct project_name from code_specs where sql_usage='STRING_CONCAT';";

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("project_name");
                Model model = mMap.get(name);
                if (model == null) {
                    model = new Model();
                }

                model.concat++;
                mMap.put(name, model);
            }

            sql = "select distinct project_name from code_specs where sql_usage='HARDCODED';";

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("project_name");
                Model model = mMap.get(name);
                if (model == null) {
                    model = new Model();
                }

                model.hardcode++;
                mMap.put(name, model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Model {
        int param = 0;
        int param_and_concat = 0;
        int concat = 0;
        int hardcode = 0;
    }
}
