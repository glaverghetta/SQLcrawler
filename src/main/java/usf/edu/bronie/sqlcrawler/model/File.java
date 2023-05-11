package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.Project.noProjectFound;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Represents an individual file from a project to be analzyed as stored in the
 * database
 */

public class File {

    private static final Logger log = LogManager.getLogger( File.class );

    private int id = 0;
    private String repo_id;
    private int project = 0;
    private String filename;
    private String path;
    private String url;
    private String hash;
    private String commit;
    private Languages languageType;
    // private String date_added; // TODO: Implement datetime var... I don't want to
    // play with time in Java at the moment

    private String code = null;

    // Retrieves a file from DB by id
    public File(int id) {
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement = mConnection.prepareStatement("SELECT * FROM files WHERE id=?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                this.id = resultSet.getInt("id");
                this.project = resultSet.getInt("project");
                this.filename = resultSet.getString("filename");
                this.path = resultSet.getString("path");
                this.url = resultSet.getString("url");
                this.hash = resultSet.getString("hash");
                this.commit = resultSet.getString("commit");
                this.languageType = Languages.nameToLang(resultSet.getString("lang"));
            }
            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving a file by id: {}", id, e);
            System.exit(-1);
        }
    }

    // Retrieves a file from DB by project and filename
    // TODO: It's reasonable for a project to have files with the same name,
    // different path
    // Need to figure out if we want this or how to modify
    public File(int project, String filename) {

    }

    // Creates a new project with the specified values
    // TODO: Add commit dates
    public File(String repo_id, String filename, String path, String url, String hash, String commit, Languages lang) {
        this.repo_id = repo_id;
        this.filename = filename;
        this.url = url;
        this.path = path;
        this.hash = hash;
        this.commit = commit;
        this.languageType = lang;
    }

    // Returns all files for a given Project id
    static public List<File> filesFromProject(int project) {
        List<File> list = new ArrayList<File>();

        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT f.*, p.gh_id FROM Files f LEFT JOIN projects p on f.project=p.id WHERE project=?");
            statement.setInt(1, project);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                File a = new File( resultSet.getString("gh_id"),
                        resultSet.getString("name"),
                        resultSet.getString("path"),
                        resultSet.getString("url"),
                        resultSet.getString("hash"),
                        resultSet.getString("commit"),
                        Languages.nameToLang(resultSet.getString("lang"))); 
                list.add(a);
            }

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving files for Project-{}", project, e);
            System.exit(-1);
        }

        return list;
    }

    // Return file id for a given file
    static public int idFromFilename(int project, String filename, String path) {
        int result = 0;
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM files WHERE project=? AND filename=? AND path=?");
            statement.setInt(1, project);
            statement.setString(2, filename);
            statement.setString(3, path);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt("id");
            }

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving a file by name: Project-{} {} {}", project, filename, path, e);
            System.exit(-1);
        }

        return result;
    }

    // Checks if the given repo already exists
    static public Boolean checkIfExists(int project, String filename, String path) {
        return idFromFilename(project, filename, path) > 0;
    }

    // Saves the project to the database, if it does not already exist
    // Returns true if saved, false if already exists
    public boolean save() throws rawGitHubLinkInvalid {

        String repo = this.repo();
        // Check that the corresponding project exists
        try{
            if (!Project.checkIfExists(this.repo_id)) {
                // Create the new project
                String[] ownerName = repo.replace("https://github.com/", "").split("/");  //Extract owner and name
    
                new Project(this.repo_id, ownerName[0], ownerName[1], repo).save();
            } else if (checkIfExists(Project.idFromGH_ID(this.repo_id), this.filename, this.path)) {
                // TODO: Add a check to see if it's a new commit
                log.debug("Not saving existing file: Project-{} {} {}", Project.idFromGH_ID(this.repo_id), this.filename, this.path);
                return false;
            }
        } catch (noProjectFound e){
            //TODO: Handle
            log.error("No project found", e);
            System.exit(-1);
        } catch (ArrayIndexOutOfBoundsException e){
            //TODO: Handle
            log.error("Could not extract owner/name from {}", this.url, e);
            System.exit(-1);
        }

        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            statement = mConnection.prepareStatement(
                    "INSERT INTO files (project, filename, path, url, hash, commit, lang, date_added, fileSize) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)");
            try{
                statement.setInt(1, Project.idFromGH_ID(this.repo_id));
            }catch(noProjectFound e){
                log.error("Unable to find a project for a file");
                System.exit(-1);
            }
            statement.setString(2, this.filename);
            statement.setString(3, this.path);
            statement.setString(4, this.url);
            statement.setString(5, this.hash);
            statement.setString(6, this.commit);
            statement.setString(7, this.languageType.searchString());
            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(8, new Timestamp(date.getTime()));
            statement.setInt(9, this.getCodeSize());
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            try{
                log.error("Error saving a file: Project-{} {} {}", Project.idFromGH_ID(this.repo_id), this.filename, this.path, e);
            }catch(noProjectFound e2){
                log.error("Unable to find a project for a file", e2);
                System.exit(-1);
            }
            System.exit(-1);
        }

        return true;
    }

    private void setUnavailable(){
        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            statement = mConnection.prepareStatement("UPDATE files SET unavailable = 1 WHERE id = ?;");
            
            statement.setInt(1, this.id);
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error(e);
            System.exit(-1);
        }
    }

    // Returns the repo url from the raw url
    private String repo() {
        String[] temp = this.url.replace("https://github.com/", "").split("/");
        String owner = temp[0];
        String name = temp[1];

        int index = this.url.indexOf("/raw/");
        if(owner.equals("raw")){
            index = this.url.indexOf("/raw/", index + 1);
        }
        if(name.equals("raw")){
            index = this.url.indexOf("/raw/", index + 1);
        }
        return this.url.substring(0, index);
    }

    // Getters and setters
    public int getId() {
        if (this.id == 0){
            try{
                return this.id = idFromFilename(this.getProject(), this.filename, this.path);

            }catch(noProjectFound e){
                log.error("Could not find a project", e);
                System.exit(-1);
            }
        }
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProject() throws noProjectFound {
        // The project might not exist, need to handle that in the future
        if (this.project == 0)
            return this.project = Project.idFromGH_ID(this.repo_id);
        return this.project;
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

    public String getCode() throws rawGitHubLinkInvalid {
        if (this.code != null) {
            return this.code;
        }

        try{
            Response r = HttpConnection.getRequest(this.url);

            String contentType = r.headers().get("Content-Type");
            if(!contentType.startsWith("text/plain;")){
                this.setUnavailable();
                throw new rawGitHubLinkInvalid(this.url, this.id);
            }
            this.code = r.body().string();
            r.close();
            return  this.code;
        }
        catch(IOException e){
            log.error("Error retrieving {}", url, e);
            System.exit(-1);
        }

        return null;
    }

    public int getCodeSize() throws rawGitHubLinkInvalid {
        return this.getCode().length();
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public Languages getLanguageType() {
    	return this.languageType;
    }
    
    public void setLanguageType(Languages languageType) {
    	this.languageType = languageType;
    }

    public static class rawGitHubLinkInvalid extends Exception {
        private String url;
        private int fileID;

        public rawGitHubLinkInvalid(String url, int fileID) {
            this.url = url;
            this.fileID = fileID;
        }

        @Override
        public String toString() {
            return "The following raw GitHub url is no longer accessible: " + url + " (file ID " + fileID + ")";
        }

    }
}
