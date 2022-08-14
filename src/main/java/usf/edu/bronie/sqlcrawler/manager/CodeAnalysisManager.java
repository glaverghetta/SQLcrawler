package usf.edu.bronie.sqlcrawler.manager;

import usf.edu.bronie.sqlcrawler.analyze.*;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;

public class CodeAnalysisManager {
    // private Connection mConnection = DBConnection.getConnection();

    //List of analyzers to run. Can remove or add new analzyers to list without issue 
    List<CodeAnalyzer> analyzers = List.of(
        new SQLCodeAnalyzer(),
        new GroupOrderByCodeAnalyzer(),
        new ColumnNameCodeAnalyzer(),
        new TableNameCodeAnalyzer(),
        new LikeCodeAnalyzer(),
        new TableNameLowerCodeAnalyzer(),
        new ViewNameCodeAnalyzer(),
        new ProcNameCodeAnalyzer(),
        new FunNameCodeAnalyzer(),
        new EventNameCodeAnalyzer(),
        new TriggerNameCodeAnalyzer(),
        new IndexNameCodeAnalyzer(),
        new DBNameCodeAnalyzer(),
        new ServerNameCodeAnalyzer(),
        new TableSpaceNameCodeAnalyzer()
    );

    // private PreparedStatement mSelectStatement = mConnection.prepareStatement("select project_name, commit_date, " +
    //         "sql_usage, like_usage, api_type, file_url, raw_url from code_specs where project_name = ?");

    // public void analyzeCode(int min, int max) {
    //     List<GitDataDTO> allUniqueProjects = getAllUniqueProjects();
    //     int size = allUniqueProjects.size();
    //     for (int j = min; j < max; j++) {
    //         GitDataDTO gitDataDTO = allUniqueProjects.get(j);
    //         List<CodeSpecDTO> files = getAllFilesByProjectName(gitDataDTO.getRepoName());
    //         int fileSize = files.size();
    //         for (int i = 0; i < fileSize; i++) {
    //             CodeSpecDTO file = files.get(i);
    //             System.out.print("\rAnalyzing -- project #: " + j + " out of " + size +
    //                     " ==||== file #: " + i + " out of " + fileSize);
    //             processFile(file);
    //         }
    //     }
    // }

    public Analysis processFile(File f) {

        // SQLUsage
        // LikeUsage

        String code = f.getCode();

        if(code == null)
        {
            System.out.println(String.format("Unknown error receiving code for file ID %d", f.getId()));
            System.exit(-1);
        }

        Analysis result = new Analysis(f.getProject(), f.getId());

        List stringLiterals = RegexUtils.findAllStringLiteral(code);

        //Determine SQLUsage, which always runs
        SQLCodeAnalyzer sqlCodeAnalyzer = new SQLCodeAnalyzer();
        result.setSql_usage(sqlCodeAnalyzer.analyzeCode(code, stringLiterals));

        //Determine API type, which always runs
        ApiTypeAnalyzer apiTypeAnalyzer = new ApiTypeAnalyzer();
        result.setApi_type(apiTypeAnalyzer.analyzeCode(code));

        //Run the remaining analyzers specified in the list
        if (!SQLType.NONE.equals(result.getSql_usage())) {
            for (CodeAnalyzer analyzer : analyzers) {
                result.set(analyzer.getDBField(), analyzer.analyzeCode(code, stringLiterals));
            }
        }

        return result;
    }

    // Update to match new format
    // private void processFile(CodeSpecDTO codeSpecDTO) {

    //     GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder analysisBuilder = new GitFileAnalysisResultDto.GitFileAnalysisResultDtoBuilder();

    //     analysisBuilder.setProjectName(codeSpecDTO.getProjectName());
    //     analysisBuilder.setFileUrl(codeSpecDTO.getFileUrl());
    //     analysisBuilder.setSQLUsage(codeSpecDTO.getSQLUsage());
    //     analysisBuilder.setLikeUsage(codeSpecDTO.getLikeUsage());
    //     analysisBuilder.setApiType(codeSpecDTO.getApiType());
    //     analysisBuilder.setCommitDate(codeSpecDTO.getCommitDate());

    //     String rawUrl = codeSpecDTO.getRawUrl();

    //     String code = HttpConnection.get(rawUrl);

    //     if (code == null)
    //         return;

    //     if (!SQLType.NONE.equals(codeSpecDTO.getSQLUsage())) {
    //         List stringLiterals = RegexUtils.findAllStringLiteral(code);

    //         SQLType sqlType = mSQLCodeAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setSQLUsageLower(sqlType);

    //         SQLType orderByType = mOrderByCodeAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setOrderGroupByUsage(orderByType);

    //         SQLType columnSQLType = mColumnNameCodeAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setColumnUsage(columnSQLType);

    //         SQLType tableNameType = mTableNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setTableUsage(tableNameType);

    //         SQLType tableLowerSqlType = mTableNameLowerAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setTableUsageLower(tableLowerSqlType);

    //         SQLType viewSqlTypes = mViewNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setViewUsage(viewSqlTypes);

    //         SQLType procSqlType = mProcNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setProcUsage(procSqlType);

    //         SQLType funSqlType = mFunNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setFunUsage(funSqlType);

    //         SQLType eventSqlType = mEventNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setEventUsage(eventSqlType);

    //         SQLType triggerSqlType = mTriggerNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setTriggerUsage(triggerSqlType);

    //         SQLType indexSqlType = mIndexNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setIndexUsage(indexSqlType);

    //         SQLType dbSqlType = mDBNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setDBUsage(dbSqlType);

    //         SQLType serverSqlType = mServerNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setServerUsage(serverSqlType);

    //         SQLType tSpaceSqlType = mTSpaceNameAnalyzer.analyzeCode(code, stringLiterals);
    //         analysisBuilder.setTableSpaceUsage(tSpaceSqlType);
    //     }

    //     saveResults(analysisBuilder.createGitFileAnalysisResultDto());
    // }

    // Don't need
    // private void saveResults(GitFileAnalysisResultDto dto) {
    //     try {
    //         mUpdateStatement.setString(1, dto.getProjectName());
    //         mUpdateStatement.setString(2, dto.getCommitDate());
    //         mUpdateStatement.setInt(3, dto.getSQLUsage().toInt());
    //         mUpdateStatement.setInt(4, dto.getSQLUsageLower().toInt());
    //         mUpdateStatement.setInt(5, dto.getOrderGroupByUsage().toInt());
    //         mUpdateStatement.setInt(6, dto.getLikeUsage().toInt());
    //         mUpdateStatement.setInt(7, dto.getColumnUsage().toInt());
    //         mUpdateStatement.setInt(8, dto.getTableUsage().toInt());
    //         mUpdateStatement.setInt(9, dto.getTableUsageLower().toInt());
    //         mUpdateStatement.setInt(10, dto.getViewUsage().toInt());
    //         mUpdateStatement.setInt(11, dto.getProcUsage().toInt());
    //         mUpdateStatement.setInt(12, dto.getFunUsage().toInt());
    //         mUpdateStatement.setInt(13, dto.getEventUsage().toInt());
    //         mUpdateStatement.setInt(14, dto.getTriggerUsage().toInt());
    //         mUpdateStatement.setInt(15, dto.getIndexUsage().toInt());
    //         mUpdateStatement.setInt(16, dto.getDBUsage().toInt());
    //         mUpdateStatement.setInt(17, dto.getServerUsage().toInt());
    //         mUpdateStatement.setInt(18, dto.getTableSpaceUsage().toInt());
    //         mUpdateStatement.setString(19, dto.getApiType().toString());
    //         mUpdateStatement.setString(20, dto.getFileUrl());
    //         mUpdateStatement.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

}
