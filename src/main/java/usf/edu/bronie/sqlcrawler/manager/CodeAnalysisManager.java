package usf.edu.bronie.sqlcrawler.manager;

import usf.edu.bronie.sqlcrawler.analyze.*;
import usf.edu.bronie.sqlcrawler.crawler.GithubPageCrawler;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.io.GDriveConnection;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.ApiType;
import usf.edu.bronie.sqlcrawler.model.GitDataDTO;
import usf.edu.bronie.sqlcrawler.model.GitFileAnalysisResultDto;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.SQLUtils;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CodeAnalysisManager {
    private Connection mConnection = DBConnection.getConnection();

    private CodeAnalyzer mSQLCodeAnalyzer;

    private CodeAnalyzer mOrderByCodeAnalyzer;

    private CodeAnalyzer mGroupByCodeAnalyzer;

    private CodeAnalyzer mLikeCodeAnalyzer;

    private ApiTypeAnalyzer mApiTypeAnalyzer;

    private GithubPageCrawler mPageCrawler;

    PreparedStatement mSelectStatement = mConnection.prepareStatement("select * from gdata where frepo_name= ?");

    PreparedStatement mUpdateStatement = mConnection.prepareStatement("INSERT INTO CODE_SPECS (project_name," +
            " commit_date, sql_usage, orderby_usage, group_usage, like_usage, api_type, file_hash, file_url, " +
            " raw_url)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

    public CodeAnalysisManager() throws SQLException {

        mSQLCodeAnalyzer = new SQLCodeAnalyzer();

        mOrderByCodeAnalyzer = new OrderByCodeAnalyzer();

        mGroupByCodeAnalyzer = new GroupByCodeAnalyzer();

        mLikeCodeAnalyzer = new LikeCodeAnalyzer();

        mApiTypeAnalyzer = new ApiTypeAnalyzer();

        mPageCrawler = new GithubPageCrawler();
    }

    public void analyzeCode() {
        List<GitDataDTO> allUniqueProjects = getAllUniqueProjects();
        int size = allUniqueProjects.size();
        for (int j = 15; j < size; j++) {
            GitDataDTO gitDataDTO = allUniqueProjects.get(j);
            List<GitDataDTO> files = getAllFilesByProjectName(gitDataDTO.getRepoName());
            int fileSize = files.size();
            for (int i = 0; i < fileSize; i++) {
                GitDataDTO file = files.get(i);
                System.out.print("\rAnalyzing -- project #: " + j + " out of " + size +
                        " ==||== file #: " + i + " out of " + fileSize);
                processFile(file);
            }
        }
    }

    private void processFile(GitDataDTO file) {
        String gitUrl = UrlUtils.createGithubUrl(file.getRepoName(), file.getRef(), file.getPath());
        mPageCrawler.loadUrl(gitUrl);
        
        GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder analysisBuilder =
                new GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder();

        analysisBuilder.setProjectName(file.getRepoName());
        analysisBuilder.setFileUrl(gitUrl);

//        String rawUrl = UrlUtils.createGithubRawUrl(file.getRepoName(), file.getRef(), file.getPath());
        String rawUrl = "https://raw.githubusercontent.com/InnovaCo/java-driver/1.0/driver-core/src/main/java/com/datastax/driver/core/QueryTrace.java";
        analysisBuilder.setRawUrl(rawUrl);

        String sha256 = UrlUtils.sha256(rawUrl);
        analysisBuilder.setFileHash(sha256);

        String code = HttpConnection.get(rawUrl);

        SQLType sqlCodeType = mSQLCodeAnalyzer.analyzeCode(code);
        analysisBuilder.setSQLUsage(sqlCodeType);

        if (!SQLType.NONE.equals(sqlCodeType)) {
            SQLType orderByType = mOrderByCodeAnalyzer.analyzeCode(code);
            analysisBuilder.setOrderByUsage(orderByType);

            SQLType groupByType = mGroupByCodeAnalyzer.analyzeCode(code);
            analysisBuilder.setGroupByUsage(groupByType);

            SQLType likeType = mLikeCodeAnalyzer.analyzeCode(code);
            analysisBuilder.setLikeUsage(likeType);

            ApiType apiType = mApiTypeAnalyzer.analyzeCode(code);
            analysisBuilder.setApiType(apiType);
        }

        GDriveConnection.uploadData(code, sha256 + ".java");

        analysisBuilder.setCommitDate(mPageCrawler.getFileCommitDate());
        saveResults(analysisBuilder.createGitFileAnalysisResultDto());
    }

    private void saveResults(GitFileAnalysisResultDto dto) {
        try {
            mUpdateStatement.setString(1, dto.getProjectName());
            mUpdateStatement.setString(2, dto.getCommitDate());
            mUpdateStatement.setString(3, dto.getSQLUsage().toString());
            mUpdateStatement.setString(4, dto.getOrderByUsage().toString());
            mUpdateStatement.setString(5, dto.getGroupByUsage().toString());
            mUpdateStatement.setString(6, dto.getLikeUsage().toString());
            mUpdateStatement.setString(7, dto.getApiType().toString());
            mUpdateStatement.setString(8, dto.getFileHash());
            mUpdateStatement.setString(9, dto.getFileUrl());
            mUpdateStatement.setString(10, dto.getRawUrl());
            mUpdateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<GitDataDTO> getAllFilesByProjectName(String repoName) {
        List l = new ArrayList();
        try {
            mSelectStatement.setString(1, repoName);
            ResultSet resultSet = mSelectStatement.executeQuery();
            SQLUtils.createGitData(resultSet, l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return l;
    }


    private List<GitDataDTO> getAllUniqueProjects() {
        try {
            List l = new ArrayList();
            Statement stmt = mConnection.createStatement();
            String sql = "select project_name from project_specs where last_commit_date!=0;";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("project_name");

                l.add(new GitDataDTO(name, null, null));
            }

            return l;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
