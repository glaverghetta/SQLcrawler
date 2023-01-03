package usf.edu.bronie.sqlcrawler.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usf.edu.bronie.sqlcrawler.io.DBConnection;

public class Analysis {
    Map<String, SQLType> results = new HashMap<String, SQLType>();
    int projectID;
    int fileID;
    SQLType sql_usage;
    ApiType api_type;

    public ApiType getApi_type() {
        return api_type;
    }

    public void setApi_type(ApiType api_type) {
        this.api_type = api_type;
    }

    public SQLType getSql_usage() {
        return sql_usage;
    }

    public void setSql_usage(SQLType sql_usage) {
        this.sql_usage = sql_usage;
    }

    public Analysis(int project, int fileID) {
        this.projectID = project;
        this.fileID = fileID;
    }

    public void set(String field, SQLType st){
        this.results.put(field, st);
    }

    public void save(){

        //TODO: Will have an error if the file/project don't exist, should handle that in the future

        try{
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();

            String sql = "INSERT INTO analyses (project, file, analysis_date, sql_usage, api_type";
            String sql_end = ") VALUES (?, ?, ?, ?, ?";

            //Dynamically add the column names and placeholders to the sql statement
            int j = 0;
            List<Integer> values = new ArrayList<Integer>();
            for (Map.Entry<String, SQLType> entry : results.entrySet()){
                values.add(entry.getValue().toInt());
                sql += String.format(", %s", entry.getKey());
                sql_end += ", ?";
            }
            sql += sql_end + ")"; 

            statement = mConnection.prepareStatement(sql);
            
            //Set the hardcoded values
            statement.setInt(1, this.projectID);
            statement.setInt(2, this.fileID);
            java.util.Date date = new java.util.Date();  //Get current time
            statement.setTimestamp(3, new Timestamp(date.getTime()));
            statement.setInt(4, this.sql_usage.toInt());
            statement.setString(5, this.api_type.toString());
            
            int i = 6;
            for(int val : values){
                statement.setInt(i, val);
                i++;
            }
            
            statement.executeUpdate();
            statement.close();
            mConnection.close();
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println("Error saving analysis");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}
