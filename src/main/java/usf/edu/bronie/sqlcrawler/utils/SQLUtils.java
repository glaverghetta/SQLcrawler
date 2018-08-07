package usf.edu.bronie.sqlcrawler.utils;

import usf.edu.bronie.sqlcrawler.model.GitDataDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLUtils {
    public static void createGitData(ResultSet resultSet, List l) throws SQLException {
        while (resultSet.next()) {
            //Retrieve by column name
            String name = resultSet.getString("frepo_name");
            String ref = resultSet.getString("fref");
            String path = resultSet.getString("fpath");

            l.add(new GitDataDTO(name, ref, path));
        }
    }
}
