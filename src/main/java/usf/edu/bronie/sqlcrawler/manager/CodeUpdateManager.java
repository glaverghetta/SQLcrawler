package usf.edu.bronie.sqlcrawler.manager;

import usf.edu.bronie.sqlcrawler.crawler.GithubPageCrawler;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CodeUpdateManager {
    private Connection mConnection = DBConnection.getConnection();

    private GithubPageCrawler mPageCrawler;

    private PreparedStatement mPreparedStatement;

    public CodeUpdateManager() {

        mPageCrawler = new GithubPageCrawler();

        String sql = "UPDATE code_stats SET commit_date = ? WHERE url = ?";

        try {
            mPreparedStatement = mConnection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCommitDates() throws SQLException {
        List<String> l = getAllEmpty();

        for (String s: l) {
            mPageCrawler.loadUrl(s);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String fileCommitDate = mPageCrawler.getFileCommitDate();

            if (fileCommitDate == null) continue;

            mPreparedStatement.setString(1, fileCommitDate);
            mPreparedStatement.setString(2, s);
            mPreparedStatement.executeUpdate();
        }

    }

    private List<String> getAllEmpty() throws SQLException {
        Statement stmt = mConnection.createStatement();
        String sql = "select url from code_stats where commit_date is null;";
        List l = new ArrayList();

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            GithubFileSpec2.GithubFileSpec2Builder sb = new GithubFileSpec2.GithubFileSpec2Builder();
            String url = rs.getString("url");

            l.add(url);
        }

        return l;
    }
}
