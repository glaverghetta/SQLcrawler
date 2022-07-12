package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.io.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;  


/**
 * Represents a project as stored in the database
 */

public class File {

    private Connection mConnection = DBConnection.getConnection();

    private int id;
    private int project;
    private String filename;
    private String path;
    private String url;
    private String hash;
    private String commit;
    //private String date_added;  // TODO: Implement datetime var... I don't want to play with time in Java at the moment

    private String repo_url;

    //Retrieves a file from DB by id
    public File(int id) {
        Connection mConnection = DBConnection.getConnection();
        PreparedStatement statement;
        try{
            statement = mConnection.prepareStatement("SELECT * FROM Files WHERE id=?");
            statement.setInt(1, id);          
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                this.id = resultSet.getInt("id");
                this.project = resultSet.getInt("project");
                this.filename = resultSet.getString("filename");
                this.path = resultSet.getString("path");
                this.url = resultSet.getString("url");
                this.hash = resultSet.getString("hash");
                this.commit = resultSet.getString("commit");
                return;
            }
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    //Retrieves a file from DB by project and filename
    //TODO: It's reasonable for a project to have files with the same name, different path
    //  Need to figure out if we want this or how to modify
    public File(int project, String filename) {

    }
    
    //Creates a new project with the specified values
    //TODO: Add commit dates
    public File(String filename, String path, String url, String hash, String commit) {
        this.filename = filename;
        this.url = url;
        this.path = path;
        this.hash = hash;
        this.commit = commit;
    }

    //Returns all files for a given Project id
    static public List<File> filesFromProject(int project){
        List<File> list = new ArrayList<File>();

        Connection mConnection = DBConnection.getConnection();
        PreparedStatement statement;
        try{
            statement = mConnection.prepareStatement("SELECT * FROM Files WHERE project=?");
            statement.setInt(1, project);          
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                File a = new File(
                    resultSet.getString("name"),
                    resultSet.getString("path"),
                    resultSet.getString("url"),
                    resultSet.getString("hash"),
                    resultSet.getString("commit")
                );
                list.add(a);  
            }
        }
        catch(SQLException e){
            //Todo: For now, just print error and quit. Might want to add more complicated solution in the future
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        
        return list;
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
            // statement.setString(1, this.name);
            // statement.setString(2, this.url);
            // statement.setString(3, this.source);
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

    public int getProject() {
        return project;
    }

    public void setProject(int project) {
        this.project = project;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getRepo_url() {
        return repo_url;
    }

    public void setRepo_url(String repo_url) {
        this.repo_url = repo_url;
    }

}
