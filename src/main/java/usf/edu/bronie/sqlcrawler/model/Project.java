package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.io.DBConnection;
import java.sql.*;


/**
 * Represents a project as stored in the database
 */

public class Project {

    private Connection mConnection = DBConnection.getConnection();

    private int id;
    private String name;
    private String url;
    private String source;
    //private String date_added;  // TODO: Implement datetime var... I don't want to play with time in Java at the moment

    //Array of recognized sources with additional repo info
    private String[] sources = {"github", "bitbucket", "gitlab"};

    //Retrieves a project from DB by id
    //TODO: Currently just returns an empty object if it fails
    public Project(int id) {
        Connection mConnection = DBConnection.getConnection();
        PreparedStatement statement;
        try{
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE id=?");
            statement.setInt(1, id);          
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                this.id = resultSet.getInt("id");
                this.name = resultSet.getString("name");
                this.url = resultSet.getString("url");
                this.source = resultSet.getString("source");
                return;
            }
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    //Retrieves a project from DB by url
    public Project(String repo_url) {

    }
    
    //Creates a new project with the specified values
    public Project(String name, String repo_url) {
        this.name = name;
        this.url = repo_url;

        
        for(String s : this.sources){
            if(repo_url.contains(s))
            {
                this.source = s;
                break;
            }
        }
    }

    //Return project id for a given repo
    static public int idFromRepo(String repo_url){
        Connection mConnection = DBConnection.getConnection();
        PreparedStatement statement;
        try{
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE url=?");
            statement.setString(1, repo_url);          
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("id");  
            }
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        return 0;
    }

    //Checks if the give repo already exists
    static public Boolean checkIfExists(String repo_url){
        if(idFromRepo(repo_url) > 0)
        {
            return true;
        }

        return false;
    }

    //Saves the project to the database, if it does not already exist
    public void save(){
        PreparedStatement statement;

        //If the project already exists, do nothing
        if(checkIfExists(this.url))
        {
            return;
        }

        try{
            statement = mConnection.prepareStatement("INSERT INTO projects (name, url, source, date_added) VALUES (?, ?, ?, ?)");
            statement.setString(1, this.name);
            statement.setString(2, this.url);
            statement.setString(3, this.source);
            java.util.Date date = new java.util.Date();  //Get current time
            statement.setTimestamp(4, new Timestamp(date.getTime()));            
            statement.executeUpdate();
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // Returns the repo details for this project, if applicable
    // public RepoInfo getRepoInfo(){

    // }

    //Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    
}
