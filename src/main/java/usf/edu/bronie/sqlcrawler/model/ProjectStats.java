package usf.edu.bronie.sqlcrawler.model;

import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.model.Project.noProjectFound;

import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class ProjectStats {
    private static final Logger log = LogManager.getLogger(ProjectStats.class);

    private int project;

    public int getProject() throws noProjectFound {
        if (project == 0) {
            this.project = Project.idFromOwnerName(this.owner, this.name);
        }
        log.error("Got the following project {}", this.project);
        return project;
    }

    public void setProject(int project) {
        this.project = project;
    }

    private Timestamp createdAt;
    private int stargazerCount;
    private Timestamp updatedAt;
    private Timestamp pushedAt;
    private String owner;
    private String name;
    private String url;
    private String gh_id;
    private String description;
    private String LRName;
    private Timestamp LRCreated;
    private Timestamp LRUpdated;
    private Timestamp date_added;  
    private int forkCount;
    private int watchersCount;
    private int releasesCount;

    private static Timestamp iso8601ToTimestamp(String strDate) {
        try {
            java.util.Date date = Date.from(Instant.parse(strDate));
            Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (DateTimeParseException e) {
            log.error("Error parsing timestamp: {}", strDate);
            System.exit(-1);
            return null;
        }
    }

    public static void makeNullEntry(String url) throws noProjectFound {
        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            String insertSQL = "INSERT INTO repo_info (project, date_added) VALUES (?, ?) as new ";
            insertSQL += "ON DUPLICATE KEY UPDATE project=new.project, date_added=new.date_added";

            statement = mConnection.prepareStatement(insertSQL);
            statement.setInt(1, Project.idFromRepo(url));

            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(2, new Timestamp(date.getTime()));
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error saving a null project", e);
            System.exit(-1);
        }
    }

    public static void makeNullEntry(int project) throws noProjectFound{
        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            String insertSQL = "INSERT INTO repo_info (project, date_added) VALUES (?, ?) as new ";
            insertSQL += "ON DUPLICATE KEY UPDATE project=new.project, date_added=new.date_added";

            statement = mConnection.prepareStatement(insertSQL);
            statement.setInt(1, project);

            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(2, new Timestamp(date.getTime()));
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error saving a null project", e);
            System.exit(-1);
        }
    }

    public ProjectStats(JSONObject data) {
        this.createdAt = iso8601ToTimestamp(data.getString("createdAt"));
        this.stargazerCount = data.getInt("stargazerCount");
        this.updatedAt = iso8601ToTimestamp(data.getString("updatedAt"));
        this.pushedAt = iso8601ToTimestamp(data.getString("pushedAt"));
        this.name = data.getString("name");
        this.owner = data.getJSONObject("owner").getString("login");
        this.url = data.getString("url");
        this.gh_id = data.getString("id");

        if (data.has("description") && !data.isNull("description")) {
            this.description = data.getString("description");
        } else {
            description = null;
        }

        if (data.has("latestRelease") && !data.isNull("latestRelease")) {
            if (data.getJSONObject("latestRelease").has("name") && !data.getJSONObject("latestRelease").isNull("name")) {
                this.LRName = data.getJSONObject("latestRelease").getString("name");
            } else {
                this.LRName = null;
            }
            this.LRCreated = iso8601ToTimestamp(data.getJSONObject("latestRelease").getString("createdAt"));
            this.LRUpdated = iso8601ToTimestamp(data.getJSONObject("latestRelease").getString("updatedAt"));
        } else {
            this.LRName = null;
            this.LRCreated = null;
            this.LRUpdated = null;
        }

        this.forkCount = data.getInt("forkCount");
        this.watchersCount = data.getJSONObject("watchers").getInt("totalCount");
        this.releasesCount = data.getJSONObject("releases").getInt("totalCount");
    }

    public ProjectStats(int projectID) throws NoStatsException {
        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            statement = mConnection.prepareStatement(
                    "SELECT r.*, p.owner, p.name, p.url FROM crawler.repo_info r LEFT JOIN projects p ON r.project=p.id WHERE project=?");
            statement.setInt(1, projectID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                this.project = resultSet.getInt("project");
                this.owner = resultSet.getString("owner");
                this.name = resultSet.getString("name");
                this.url = resultSet.getString("url");

                this.gh_id = resultSet.getString("gh_id");
                this.description = resultSet.getString("description");
                this.LRName = resultSet.getString("LRName");

                this.stargazerCount = resultSet.getInt("stargazerCount");
                this.forkCount = resultSet.getInt("forkCount");
                this.watchersCount = resultSet.getInt("watchersCount");
                this.releasesCount = resultSet.getInt("releasesCount");

                // TODO: Pull datetimes from database; right now not needed
            }
            else{
                statement.close();
                mConnection.close();
                throw new NoStatsException(projectID);
            }
            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            throw new NoStatsException(projectID);
        }
    }

    public void save() throws noProjectFound {
        try {
            PreparedStatement statement;
            Connection mConnection = DBConnection.getConnection();
            String insertSQL = "INSERT INTO repo_info (project, gh_id, description, releasesCount, LRName, LRCreated, LRUpdated, stargazerCount,";
            insertSQL += "forkCount, watchersCount, createdAt, updatedAt, pushedAt, date_added)";
            insertSQL += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) as new ";
            insertSQL += "ON DUPLICATE KEY UPDATE gh_id=new.gh_id, releasesCount=new.releasesCount, LRName=new.LRName, LRName=new.LRName, LRCreated=new.LRCreated, ";
            insertSQL += "LRUpdated=new.LRUpdated, stargazerCount=new.stargazerCount, forkCount=new.forkCount, watchersCount=new.watchersCount, ";
            insertSQL += "createdAt=new.createdAt, updatedAt=new.updatedAt, pushedAt=new.pushedAt, date_added=new.date_added";

            statement = mConnection.prepareStatement(insertSQL);
            statement.setInt(1, Project.idFromGH_ID(this.gh_id));
            statement.setString(2, this.gh_id);

            statement.setString(3, this.description);

            if (this.description != null) {
                statement.setString(3, this.description);
            } else {
                statement.setNull(3, java.sql.Types.NULL);
            }

            statement.setInt(4, this.releasesCount);

            if (this.LRName != null) {
                statement.setString(5, this.LRName);
            } else {
                statement.setNull(5, java.sql.Types.NULL);
            }
            if (this.LRCreated != null) {
                statement.setTimestamp(6, this.LRCreated);
            } else {
                statement.setNull(6, java.sql.Types.NULL);
            }
            if (this.LRUpdated != null) {
                statement.setTimestamp(7, this.LRUpdated);
            } else {
                statement.setNull(7, java.sql.Types.NULL);
            }

            statement.setInt(8, this.stargazerCount);
            statement.setInt(9, this.forkCount);
            statement.setInt(10, this.watchersCount);
            statement.setTimestamp(11, this.createdAt);
            statement.setTimestamp(12, this.updatedAt);
            statement.setTimestamp(13, this.pushedAt);

            java.util.Date date = new java.util.Date(); // Get current time
            statement.setTimestamp(14, new Timestamp(date.getTime()));
            statement.executeUpdate();

            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error saving a project: {} {}", owner, name, e);
            System.exit(-1);
        }
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public int getStargazerCount() {
        return stargazerCount;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public Timestamp getPushedAt() {
        return pushedAt;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getGh_id() {
        return gh_id;
    }

    public String getDescription() {
        return description;
    }

    public String getLRName() {
        return LRName;
    }

    public Timestamp getLRCreated() {
        return LRCreated;
    }

    public Timestamp getLRUpdated() {
        return LRUpdated;
    }

    public Timestamp getDate_added() {
        return date_added;
    }

    public int getForkCount() {
        return forkCount;
    }

    public int getWatchersCount() {
        return watchersCount;
    }

    public int getReleasesCount() {
        return releasesCount;
    }

    public class NoStatsException extends Exception {
        private int id;

        public NoStatsException(int id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Could not find stats for Project " + this.id;
        }

    }
}
