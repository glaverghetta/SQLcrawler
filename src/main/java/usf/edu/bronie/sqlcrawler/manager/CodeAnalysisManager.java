package usf.edu.bronie.sqlcrawler.manager;

import usf.edu.bronie.sqlcrawler.analyze.*;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.CodeSpecDTO;
import usf.edu.bronie.sqlcrawler.model.GitDataDTO;
import usf.edu.bronie.sqlcrawler.model.GitFileAnalysisResultDto;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;
import usf.edu.bronie.sqlcrawler.utils.SQLUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CodeAnalysisManager {
    private Connection mConnection = DBConnection.getConnection();

    private CodeAnalyzer mSQLCodeAnalyzer;

    private CodeAnalyzer mOrderByCodeAnalyzer;

    private CodeAnalyzer mColumnNameCodeAnalyzer;

    private CodeAnalyzer mTableNameAnalyzer;

    private CodeAnalyzer mTableNameLowerAnalyzer;

    private CodeAnalyzer mViewNameAnalyzer;

    private CodeAnalyzer mProcNameAnalyzer;

    private CodeAnalyzer mFunNameAnalyzer;

    private CodeAnalyzer mEventNameAnalyzer;

    private CodeAnalyzer mTriggerNameAnalyzer;

    private CodeAnalyzer mIndexNameAnalyzer;

    private CodeAnalyzer mDBNameAnalyzer;

    private CodeAnalyzer mServerNameAnalyzer;

    private CodeAnalyzer mTSpaceNameAnalyzer;

    private PreparedStatement mSelectStatement = mConnection.prepareStatement("select project_name, commit_date, " +
            "sql_usage, like_usage, api_type, file_url, raw_url from code_specs where project_name = ?");

    private PreparedStatement mUpdateStatement = mConnection.prepareStatement("INSERT INTO code_specs2 (project_name," +
            " commit_date, sql_usage, sql_usage_lower, order_group_usage, like_usage, column_usage, table_usage, " +
            "table_usage_lower, view_usage, proc_usage," +
            " fun_usage, event_usage, trig_usage, index_usage, db_usage, server_usage, tspace_usage, api_type, url)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

    public CodeAnalysisManager() throws SQLException {

        mSQLCodeAnalyzer = new SQLCodeAnalyzer();

        mOrderByCodeAnalyzer = new GroupOrderByCodeAnalyzer();

        mColumnNameCodeAnalyzer = new ColumnNameCodeAnalyzer();

        mTableNameAnalyzer = new TableNameCodeAnalyzer();

        mTableNameLowerAnalyzer= new TableNameLowerCodeAnalyzer();

        mViewNameAnalyzer = new ViewNameCodeAnalyzer();

        mProcNameAnalyzer = new ProcNameCodeAnalyzer();

        mFunNameAnalyzer = new FunNameCodeAnalyzer();

        mEventNameAnalyzer = new EventNameCodeAnalyzer();

        mTriggerNameAnalyzer = new TriggerNameCodeAnalyzer();

        mIndexNameAnalyzer = new IndexNameCodeAnalyzer();

        mDBNameAnalyzer = new DBNameCodeAnalyzer();

        mServerNameAnalyzer = new ServerNameCodeAnalyzer();

        mTSpaceNameAnalyzer = new TableSpaceNameCodeAnalyzer();
    }

    public void analyzeCode(int min, int max) {
        List<GitDataDTO> allUniqueProjects = getAllUniqueProjects();
        int size = allUniqueProjects.size();
        for (int j = min; j < max; j++) {
            GitDataDTO gitDataDTO = allUniqueProjects.get(j);
            List<CodeSpecDTO> files = getAllFilesByProjectName(gitDataDTO.getRepoName());
            int fileSize = files.size();
            for (int i = 0; i < fileSize; i++) {
                CodeSpecDTO file = files.get(i);
                System.out.print("\rAnalyzing -- project #: " + j + " out of " + size +
                        " ==||== file #: " + i + " out of " + fileSize);
                processFile(file);
            }
        }
    }

    private void processFile(CodeSpecDTO codeSpecDTO) {

        GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder analysisBuilder =
                new GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder();

        analysisBuilder.setProjectName(codeSpecDTO.getProjectName());
        analysisBuilder.setFileUrl(codeSpecDTO.getFileUrl());
        analysisBuilder.setSQLUsage(codeSpecDTO.getSQLUsage());
        analysisBuilder.setLikeUsage(codeSpecDTO.getLikeUsage());
        analysisBuilder.setApiType(codeSpecDTO.getApiType());
        analysisBuilder.setCommitDate(codeSpecDTO.getCommitDate());

        String rawUrl = codeSpecDTO.getRawUrl();
//        rawUrl = "https://raw.githubusercontent.com/vzmc/K016A1743/537959ca6e397b4a00c364786c7d2d41ce79e32b/h2/src/test/org/h2/test/db/TestIndexHints.java";

        String code = HttpConnection.get(rawUrl);

        if (code == null) return;

        if (!SQLType.NONE.equals(codeSpecDTO.getSQLUsage())) {
            List stringLiterals = RegexUtils.findAllStringLiteral(code);

            SQLType sqlType = mSQLCodeAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setSQLUsageLower(sqlType);

            SQLType orderByType = mOrderByCodeAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setOrderGroupByUsage(orderByType);

            SQLType columnSQLType = mColumnNameCodeAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setColumnUsage(columnSQLType);

            SQLType tableNameType = mTableNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setTableUsage(tableNameType);

            SQLType tableLowerSqlType = mTableNameLowerAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setTableUsageLower(tableLowerSqlType);

            SQLType viewSqlTypes = mViewNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setViewUsage(viewSqlTypes);

            SQLType procSqlType = mProcNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setProcUsage(procSqlType);

            SQLType funSqlType = mFunNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setFunUsage(funSqlType);

            SQLType eventSqlType = mEventNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setEventUsage(eventSqlType);

            SQLType triggerSqlType = mTriggerNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setTriggerUsage(triggerSqlType);

            SQLType indexSqlType = mIndexNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setIndexUsage(indexSqlType);

            SQLType dbSqlType = mDBNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setDBUsage(dbSqlType);

            SQLType serverSqlType = mServerNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setServerUsage(serverSqlType);

            SQLType tSpaceSqlType = mTSpaceNameAnalyzer.analyzeCode(code, stringLiterals);
            analysisBuilder.setTableSpaceUsage(tSpaceSqlType);
        }


        saveResults(analysisBuilder.createGitFileAnalysisResultDto());
    }

    private void saveResults(GitFileAnalysisResultDto dto) {
        try {
            mUpdateStatement.setString(1, dto.getProjectName());
            mUpdateStatement.setString(2, dto.getCommitDate());
            mUpdateStatement.setInt(3, dto.getSQLUsage().toInt());
            mUpdateStatement.setInt(4, dto.getSQLUsageLower().toInt());
            mUpdateStatement.setInt(5, dto.getOrderGroupByUsage().toInt());
            mUpdateStatement.setInt(6, dto.getLikeUsage().toInt());
            mUpdateStatement.setInt(7, dto.getColumnUsage().toInt());
            mUpdateStatement.setInt(8, dto.getTableUsage().toInt());
            mUpdateStatement.setInt(9, dto.getTableUsageLower().toInt());
            mUpdateStatement.setInt(10, dto.getViewUsage().toInt());
            mUpdateStatement.setInt(11, dto.getProcUsage().toInt());
            mUpdateStatement.setInt(12, dto.getFunUsage().toInt());
            mUpdateStatement.setInt(13, dto.getEventUsage().toInt());
            mUpdateStatement.setInt(14, dto.getTriggerUsage().toInt());
            mUpdateStatement.setInt(15, dto.getIndexUsage().toInt());
            mUpdateStatement.setInt(16, dto.getDBUsage().toInt());
            mUpdateStatement.setInt(17, dto.getServerUsage().toInt());
            mUpdateStatement.setInt(18, dto.getTableSpaceUsage().toInt());
            mUpdateStatement.setString(19, dto.getApiType().toString());
            mUpdateStatement.setString(20, dto.getFileUrl());
            mUpdateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<CodeSpecDTO> getAllFilesByProjectName(String repoName) {
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
