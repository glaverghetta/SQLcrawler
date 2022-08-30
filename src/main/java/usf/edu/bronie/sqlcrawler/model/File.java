package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an individual file from a project to be analzyed as stored in the
 * database
 */

public class File {

    private static final Logger log = LoggerFactory.getLogger( File.class );

    private int id = 0;
    private int project = 0;
    private String filename;
    private String path;
    private String url;
    private String hash;
    private String commit;
    // private String date_added; // TODO: Implement datetime var... I don't want to
    // play with time in Java at the moment

    private String code = null;

    // Retrieves a file from DB by id
    public File(int id) {
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement = mConnection.prepareStatement("SELECT * FROM Files WHERE id=?");
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
    public File(String filename, String path, String url, String hash, String commit) {
        this.filename = filename;
        this.url = url;
        this.path = path;
        this.hash = hash;
        this.commit = commit;
    }

    // Returns all files for a given Project id
    static public List<File> filesFromProject(int project) {
        List<File> list = new ArrayList<File>();

        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM Files WHERE project=?");
            statement.setInt(1, project);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                File a = new File(
                        resultSet.getString("name"),
                        resultSet.getString("path"),
                        resultSet.getString("url"),
                        resultSet.getString("hash"),
                        resultSet.getString("commit"));
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
            statement = mConnection.prepareStatement("SELECT * FROM Files WHERE project=? AND filename=? AND path=?");
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
    public boolean save() {

        String repo = this.repo();
        // Check that the corresponding project exists
        if (!Project.checkIfExists(repo)) {
            // Create the new project
            new Project(repo.replace("https://github.com/", ""), repo).save();
        } else if (checkIfExists(Project.idFromRepo(repo), this.filename, this.path)) {
            // TODO: Add a check to see if it's a new commit
            log.debug("Not saving existing file: Project-{} {} {}", Project.idFromRepo(repo), this.filename, this.path);
            return false;
        }

        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            statement = mConnection.prepareStatement(
                    "INSERT INTO files (project, filename, path, url, hash, commit, date_added) VALUES (?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, Project.idFromRepo(repo));
            statement.setString(2, this.filename);
            statement.setString(3, this.path);
            statement.setString(4, this.url);
            statement.setString(5, this.hash);
            statement.setString(6, this.commit);
            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(7, new Timestamp(date.getTime()));
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error saving a file: Project-{} {} {}", Project.idFromRepo(repo), this.filename, this.path, e);
            System.exit(-1);
        }

        return true;
    }

    // Returns the repo url from the raw url
    private String repo() {
        int index = this.url.indexOf("/raw/");
        return this.url.substring(0, index);
    }

    // Getters and setters
    public int getId() {
        if (this.id == 0)
            return this.id = idFromFilename(this.getProject(), this.filename, this.path);
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProject() {
        // The project might not exist, need to handle that in the future
        if (this.project == 0)
            return this.project = Project.idFromRepo(this.repo());
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

    public String getCode() {
        if (this.code != null) {
            return this.code;
        }

        return this.code = HttpConnection.get(this.url);
    }

    public void setCode(String code) {
        this.code = code;
    }

}
