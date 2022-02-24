package usf.edu.bronie.sqlcrawler.utils;

import usf.edu.bronie.sqlcrawler.model.CodeSpecDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLUtils {
    public static void createGitData(ResultSet resultSet, List l) throws SQLException {
        while (resultSet.next()) {
            //Retrieve by column name
            String name = resultSet.getString("project_name");
            String commit = resultSet.getString("commit_date");
            String sqlUsage = resultSet.getString("sql_usage");
            String likeUsage = resultSet.getString("like_usage");
            String apiType = resultSet.getString("api_type");
            String fileUrl = resultSet.getString("file_url");
            String rawUrl = resultSet.getString("raw_url");

            l.add(new CodeSpecDTO(commit, apiType, name, fileUrl, rawUrl, sqlUsage, likeUsage));
        }
    }
}
