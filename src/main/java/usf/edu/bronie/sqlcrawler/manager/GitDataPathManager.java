package usf.edu.bronie.sqlcrawler.manager;

import usf.edu.bronie.sqlcrawler.crawler.GithubPageCrawler;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.model.GitDataDTO;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GitDataPathManager {

    private Connection mConnection = DBConnection.getConnection();

    private GithubPageCrawler mGithubPageCrawler = new GithubPageCrawler();

//    private static final int MAX_COUNT = 379279;
    private static final int MAX_COUNT = 10;

    private static final int OFFSET_INC = 10;

    private int mOffset = 0;

    public void collectData() {
        while (hasNext()) {
            try {
                List<GitDataDTO> dataDTOList = getNextGitData();
                for (GitDataDTO gdd: dataDTOList) {
                    collectData(gdd);
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void collectData(GitDataDTO gdd) {
        String githubUrl = UrlUtils.createGithubUrl(gdd.getRepoName(), gdd.getRef(), gdd.getPath());
        String rawUrl = UrlUtils.createGithubRawUrl(gdd.getRepoName(), gdd.getRef(), gdd.getPath());
        GithubFileSpec fileSpecByUrl = mGithubPageCrawler.getFileSpecByUrl(githubUrl);

        saveData(githubUrl, rawUrl, fileSpecByUrl);
    }

    private void saveData(String githubUrl, String rawUrl, GithubFileSpec fileSpecByUrl) {

    }


    private List<GitDataDTO> getNextGitData() throws SQLException {
        List l = new ArrayList();
        Statement stmt = mConnection.createStatement();
        String sql = "select * from gdata limit " + OFFSET_INC + " OFFSET " + mOffset;

        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            //Retrieve by column name
            String name = rs.getString("frepo_name");
            String ref = rs.getString("fref");
            String path  = rs.getString("fpath");

            l.add(new GitDataDTO(name, ref, path));
        }

        mOffset += OFFSET_INC;

        return l;
    }

    private boolean hasNext() {
        return mOffset < MAX_COUNT;
    }
}
