package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.model.ProjectStats.NoStatsException;

import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a project as stored in the database
 */

public class Project {
    private static final Logger log = LogManager.getLogger(ProjectStats.class);

    private int id;
    private String gh_id;
    private String owner;
    private String name;
    private String url;
    private String source;
    private ProjectStats stats;

    // private String date_added; // TODO: Implement datetime var... I don't want to
    // play with time in Java at the moment

    // Array of recognized sources with additional repo info
    private String[] sources = { "github", "bitbucket", "gitlab" };

    // Retrieves a project from DB by id
    // TODO: Currently just exits or returns an empty object if it fails
    public Project(int id) {
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE id=?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                this.id = resultSet.getInt("id");
                this.gh_id = resultSet.getString("gh_id");
                this.owner = resultSet.getString("owner");
                this.name = resultSet.getString("name");
                this.url = resultSet.getString("url");
                this.source = resultSet.getString("source");
            }
            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            System.out.println("Error retrieving a project by ID");
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Retrieves a project from DB by url
    public Project(String repo_url) {

    }

    // Creates a new project with the specified values
    public Project(String gh_id, String owner, String name, String repo_url) {
        this.gh_id = gh_id;
        this.owner = owner;
        this.name = name;
        this.url = repo_url;

        for (String s : this.sources) {
            if (repo_url.contains(s)) {
                this.source = s;
                break;
            }
        }
    }

    // Return project id for a given repo
    static public int idFromRepo(String repo_url) throws noProjectFound  {
        int result = 0;
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE url=?");
            statement.setString(1, repo_url);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt("id");
            }
            else{
                statement.close();
                mConnection.close();
                throw new noProjectFound(repo_url);
            }

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            System.out.println("Error retrieving a project by repo");
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }

        return result;
    }

    static public int idFromGH_ID(String gh_id) throws noProjectFound  {
        int result = 0;
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE gh_id=?");
            statement.setString(1, gh_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt("id");
            }
            else{
                statement.close();
                mConnection.close();
                throw new noProjectFound(gh_id);
            }

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            System.out.println("Error retrieving a project by repo");
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }

        return result;
    }

    static public int idFromOwnerName(String owner, String name) throws noProjectFound {
        int result = 0;
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement("SELECT * FROM Projects WHERE owner=? and name=?");
            statement.setString(1, owner);
            statement.setString(2, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt("id");
            }
            else{
                statement.close();
                mConnection.close();
                throw new noProjectFound(owner, name);
            }

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving a project by owner name: {} {} ", owner, name, e);
            System.exit(-1);
        }

        return result;
    }

    public boolean hasStats() {
        try {
            if (this.stats == null)
                this.getStats();
        } catch (NoStatsException e) {
            // Getting stats returned nothing
            return false;
        }

        return this.stats != null;
    }

    public void getStats() throws NoStatsException {
        ProjectStats a = new ProjectStats(id);
        this.stats = a;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Project other = (Project) obj;
        return this.getId() == other.getId();
    }

    // Checks if the give repo already exists
    static public Boolean checkIfExists(String repo_url) {
        try{
            if (idFromRepo(repo_url) > 0) {
                return true;
            }
            return false;
        }catch(Project.noProjectFound e){
            return false;
        }
    }

    // Saves the project to the database, if it does not already exist
    public boolean save() {

        // If the project already exists, do nothing
        if (checkIfExists(this.url)) {
            return false;
        }

        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();

            statement = mConnection
                    .prepareStatement("INSERT INTO projects (gh_id, owner, name, url, source, date_added) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, this.gh_id);
            statement.setString(2, this.owner);
            statement.setString(3, this.name);
            statement.setString(4, this.url);
            statement.setString(5, this.source);
            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(6, new Timestamp(date.getTime()));
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error saving a project", e);
            System.exit(-1);
        }

        return true;
    }

    // Returns the repo details for this project, if applicable
    // public RepoInfo getRepoInfo(){

    // }

    // Getters and setters

    public int getId() {
        if (id == 0){

            try{
                setId(idFromRepo(this.url));
            }catch(Project.noProjectFound e){
                log.error("No ID exists for this project", e);
                System.exit(-1);
            }
        }
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public static class noProjectFound extends Exception {
        private String url;
        private String name;
        private String owner;

        public noProjectFound(String name, String owner) {
            this.name = name;
            this.owner = owner;
        }

        public noProjectFound(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            if(owner == null){
                return "Could not find a project with the url " + url;
            }else{
                return "Could not find a project with the owner/name " + owner + "/" + name;
            }
        }

    }

}
