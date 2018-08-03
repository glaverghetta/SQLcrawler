package usf.edu.bronie.sqlcrawler.manager;

import org.apache.commons.lang3.math.NumberUtils;
import usf.edu.bronie.sqlcrawler.crawler.GithubProjectCrawler;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.model.GitDataDTO;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GitProjectAnalysisManager {

    private Connection mConnection = DBConnection.getConnection();

    private GithubProjectCrawler mProjectCrawler = new GithubProjectCrawler();

    PreparedStatement mInsertStatement = mConnection.
            prepareStatement("INSERT INTO PROJECT_SPECS (project_name, fork_count, watch_count, star_count, " +
                    "last_commit_date, forked_from, total_commit, total_branch, total_release, total_contr) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    public GitProjectAnalysisManager() throws SQLException {
    }

    public void analyzeProjects() {
        try {
            List<GitDataDTO> projects = getAllUniqueProjects();
            int size = projects.size();
            for (int j = 30000; j < size; j++) {
                GitDataDTO gd = projects.get(j);
                System.out.print("\rAnalyzing -- file number: " + j + " out of " + size);
                String url = UrlUtils.createGithubProjectUrl(gd.getRepoName(), gd.getRef());
                GithubFileSpec fileSpec = mProjectCrawler.getFileSpecByUrl(url);

                if (fileSpec != null) {
//                    saveFileSpec(fileSpec, gd.getRepoName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFileSpec(GithubFileSpec fileSpec, String projectName) {
        try {
            mInsertStatement.setString(1, projectName);
            mInsertStatement.setInt(2, NumberUtils.toInt(fileSpec.getForkCount(), 0));
            mInsertStatement.setInt(3, NumberUtils.toInt(fileSpec.getWatchCount(), 0));
            mInsertStatement.setInt(4, NumberUtils.toInt(fileSpec.getStarCount(), 0));
            mInsertStatement.setInt(5, NumberUtils.toInt(fileSpec.getCommitDate(), 0));
            mInsertStatement.setString(6, fileSpec.getForkedFrom());
            mInsertStatement.setInt(7, NumberUtils.toInt(fileSpec.getTotalCommit(), 0));
            mInsertStatement.setInt(8, NumberUtils.toInt(fileSpec.getTotalBranch(), 0));
            mInsertStatement.setInt(9, NumberUtils.toInt(fileSpec.getTotalRelease(), 0));
            mInsertStatement.setInt(10, NumberUtils.toInt(fileSpec.getTotalContribution(), 0));
            mInsertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<GitDataDTO> getAllUniqueProjects() throws SQLException {
        List l = new ArrayList();
        Statement stmt = mConnection.createStatement();
//        String sql = "select distinct frepo_name, fref from GDATA";
        String sql = "select distinct frepo_name, fref from GDATA";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            //Retrieve by column name
            String name = rs.getString("frepo_name");
            String ref = rs.getString("fref");

            l.add(new GitDataDTO(name, ref, null));
        }

        return l;
    }

}
