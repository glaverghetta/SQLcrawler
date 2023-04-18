import glob
from mysql.connector import connect, Error
from multiprocessing import Process
import matplotlib.pyplot as plt

class DBInfo:
  def __init__(self, user, password, dbname):
    self.user = user
    self.password = password
    self.dbname = dbname

# Database information
java = DBInfo("root", "pass", "crawler")
cs_executescalar = DBInfo("root", "pass", "crawler-scalar")
cs_executereader = DBInfo("root", "pass", "crawler-scalar")
cs_executenonquery = DBInfo("root", "pass", "crawlernq")
php = DBInfo("root", "pass", "php_11")

def createDBConnection(dbinfo):
    try:
        mydb = connect(
            host="localhost",
            user=dbinfo.user,
            password=dbinfo.password,
            database=dbinfo.dbname
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb

# Queries fileSize, sql_usage
def fetchFileSizeAndSqlUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize, sql_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# Queries fileSize, table_usage
def fetchFileSizeAndTableUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, table_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# Queries fileSize, column_usage
def fetchFileSizeAndColumnUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, column_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

def fetchFileSizeAndSQLIDIAUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    query = '''SELECT fileSize, order_group_usage, like_usage, column_usage, table_usage, view_usage, proc_usage, fun_usage, event_usage, trig_usage, 
     index_usage, db_usage, server_usage, tspace_usage from files, analyses WHERE files.id = analyses.file;'''
    cursor.execute()
    result = cursor.fetchall()
    data=[]
    for size, order, like, column, table, view, proc, fun, event, trig, index, db, server, tspace in result:
        if (order in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            like in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            column in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            table in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            view in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            proc in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            fun in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            event in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            trig in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            index in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            db in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            server in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"] or
            tspace in ["STRING_CONCAT", "STRING_INTERP", "STRING_CONCAT_LIST"]):
            data.append(size, 1)
        else:
            data.append(size, 0)
    return data

# returns a list of tuples of data: (size, vulnerable) from (size, usage)
def sanitizeIfVulnerable(data):
    newdata=[]
    for size, sql in data:
        if sql=="STRING_CONCAT" or sql=="STRING_INTERP" or sql=="STRING_CONCAT_LIST":
            newdata.append((size, 1))
        else:
            newdata.append((size, 0))
    return newdata

# Given array of data in form (size, vulnerable)
# Build a graph with 3 lines
def buildGraph_FileSize_Vulnerable(datas, range=10000, maxrange=200000, title="Vulnerabilities vs. File Size"):
    # data is considered sanitized in form (size, vuln)
    datasets = []
    bins=[]
    error_count = 0
    # Go through each dataset
    for dataset in datas:
        counts = []
        bins=[]
        lower_bound = 0
        upper_bound = range
        # Go through each range, and count the percentage vulnerable
        while upper_bound < maxrange:
            bins.append(lower_bound/1000)
            count = 0
            vuln_count = 0
            for size, vuln in dataset:
                if size == None:
                    # This is an error where there is no size in the data base
                    # Since we don't have that data - skip this example
                    error_count=error_count+1
                    continue
                if size < upper_bound and size > lower_bound:
                    count = count+1
                    if vuln == 1:
                        vuln_count = vuln_count + 1
            #print("size range: ", lower_bound, "-", upper_bound)
            #print("count: ", count)
            #print("vuln:", vuln_count)
            if count != 0:
                val = vuln_count / count
                counts.append(val)
            else:
                counts.append(0)
            lower_bound = lower_bound + range
            upper_bound = upper_bound + range
        datasets.append(counts)

    # print("Error count of files missing sizes: ", error_count)

    # Assuming 3 datasets 
    plt.plot(bins, datasets[0], label="Java", linestyle="-")
    plt.plot(bins, datasets[1], label="C#", linestyle="--")
    plt.plot(bins, datasets[2], label="Php", linestyle=":")
    plt.legend()

    plt.xlabel("File Size(in MB)")
    plt.ylabel("Percentage of concatsenated/interpolated")
    plt.title(title)
    plt.show()
    return 

# Given array of data in form (size, vulnerable)
# Build a graph with 3 lines
def buildGraph_FileSize_Vulnerable_Two_Sets(datas, sql_data, range=10000, maxrange=200000, title="Vulnerabilities vs. File Size"):
    # data is considered sanitized in form (size, vuln)
    datasets = []
    bins=[]
    error_count = 0
    # Go through each dataset
    for dataset in datas:
        counts = []
        bins=[]
        lower_bound = 0
        upper_bound = range
        # Go through each range, and count the percentage vulnerable
        while upper_bound < maxrange:
            bins.append(lower_bound/1000)
            count = 0
            vuln_count = 0
            for size, vuln in dataset:
                if size == None:
                    # This is an error where there is no size in the data base
                    # Since we don't have that data - skip this example
                    error_count=error_count+1
                    continue
                if size < upper_bound and size > lower_bound:
                    count = count+1
                    if vuln == 1:
                        vuln_count = vuln_count + 1
            #print("size range: ", lower_bound, "-", upper_bound)
            #print("count: ", count)
            #print("vuln:", vuln_count)
            if count != 0:
                val = vuln_count / count
                counts.append(val)
            else:
                counts.append(0)
            lower_bound = lower_bound + range
            upper_bound = upper_bound + range
        datasets.append(counts)

    # print("Error count of files missing sizes: ", error_count)

    # Assuming 3 datasets 
    plt.plot(bins, datasets[0], label="Java Column", linestyle="-", color="orange")
    plt.plot(bins, datasets[1], label="C# Column", linestyle="--", color="blue")
    plt.plot(bins, datasets[2], label="Php Column", linestyle=":", color="green")

    sql_datasets = []

    for dataset in sql_data:
        counts = []
        lower_bound = 0
        upper_bound = range
        # Go through each range, and count the percentage vulnerable
        while upper_bound < maxrange:
            count = 0
            vuln_count = 0
            for size, vuln in dataset:
                if size == None:
                    # This is an error where there is no size in the data base
                    # Since we don't have that data - skip this example
                    error_count=error_count+1
                    continue
                if size < upper_bound and size > lower_bound:
                    count = count+1
                    if vuln == 1:
                        vuln_count = vuln_count + 1
            #print("size range: ", lower_bound, "-", upper_bound)
            #print("count: ", count)
            #print("vuln:", vuln_count)
            if count != 0:
                val = vuln_count / count
                counts.append(val)
            else:
                counts.append(0)
            lower_bound = lower_bound + range
            upper_bound = upper_bound + range
        sql_datasets.append(counts)

    # print("Error count of files missing sizes: ", error_count)

    # Assuming 3 datasets 
    plt.plot(bins, sql_datasets[0], label="Java SQL", linestyle="-", color="orange")
    plt.plot(bins, sql_datasets[1], label="C# SQL", linestyle="--", color="blue")
    plt.plot(bins, sql_datasets[2], label="Php SQL", linestyle=":", color="green")
   

    plt.xlabel("File Size(in MB)")
    plt.ylabel("Percentage of concatsenated/interpolated")
    plt.title(title)
    plt.show()
    return 

def build_sql_usage_graph():
    raw_datas_sql = []
    raw_datas_sql.append(fetchFileSizeAndSqlUsage(java))

    # for cs specifically, need to combine into one list
    cs_scalar = fetchFileSizeAndSqlUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndSqlUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndSqlUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(fetchFileSizeAndSqlUsage(php))

    sanitized_data_sql = []
    for data in raw_datas_sql:
        sanitized_data_sql.append(sanitizeIfVulnerable(data))

    # Build graph
    buildGraph_FileSize_Vulnerable(sanitized_data_sql, title="")

def build_column_usage_graph():
    raw_datas_col = []
    raw_datas_col.append(fetchFileSizeAndColumnUsage(java))

    # for cs specifically, need to combine into one list
    cs_scalar = fetchFileSizeAndColumnUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndColumnUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndColumnUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas_col.append(cs_total)
    raw_datas_col.append(fetchFileSizeAndColumnUsage(php))

    sanitized_data_col = []
    for data in raw_datas_col:
        sanitized_data_col.append(sanitizeIfVulnerable(data))

    # Build graph
    buildGraph_FileSize_Vulnerable(sanitized_data_col, title="File Size vs. Vulnerabilities in Column Usage")

def build_table_usage_graph():
    raw_datas_tbl = []
    raw_datas_tbl.append(fetchFileSizeAndTableUsage(java))

    # for cs specifically, need to combine into one list
    cs_scalar = fetchFileSizeAndTableUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndTableUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndTableUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas_tbl.append(cs_total)
    raw_datas_tbl.append(fetchFileSizeAndTableUsage(php))

    sanitized_data_tbl = []
    for data in raw_datas_tbl:
        sanitized_data_tbl.append(sanitizeIfVulnerable(data))

    # Build graph
    buildGraph_FileSize_Vulnerable(sanitized_data_tbl, title="File Size vs. Vulnerabilities in Table Usage")    

def build_column_and_sql_usage_graph():
    raw_datas_sql = []
    raw_datas_sql.append(fetchFileSizeAndSqlUsage(java))

    # for cs specifically, need to combine into one list
    cs_scalar = fetchFileSizeAndSqlUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndSqlUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndSqlUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(fetchFileSizeAndSqlUsage(php))

    sanitized_data_sql = []
    for data in raw_datas_sql:
        sanitized_data_sql.append(sanitizeIfVulnerable(data))

    raw_datas_col = []
    raw_datas_col.append(fetchFileSizeAndColumnUsage(java))

    # for cs specifically, need to combine into one list
    cs_scalar = fetchFileSizeAndColumnUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndColumnUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndColumnUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas_col.append(cs_total)
    raw_datas_col.append(fetchFileSizeAndColumnUsage(php))

    sanitized_data_col = []
    for data in raw_datas_col:
        sanitized_data_col.append(sanitizeIfVulnerable(data))    

    # Build graph
    buildGraph_FileSize_Vulnerable_Two_Sets(sanitized_data_col, sanitized_data_sql, title="File Size vs. Vulnerabilities in Column Usage and SQL Usage")  


#IDEA: graph showing vulnerabilities between concat, interp, and list
#IDEA: graph that takes in account what is using parameterized queries 
# number of files 

# how many files use parameterized queries -table
# how many files use parameterized queries and have sql idias - tables
# files without parameterized queries - pie graph

# distribution of file sizes - 80% in this file range - curve

# show number of files is super low at end

# show individual languages total vs. vulnerable
# in both bar graph form and line form

# General queries used for table of files per category
def printTableInformation(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()

    # Sql_usage
    cursor.execute("SELECT count(*) from analyses")
    result = cursor.fetchall()
    print("Total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses,files WHERE analyses.sql_usage = \"STRING_CONCAT\" AND files.id=analyses.file")
    result = cursor.fetchall()
    print("Sql_usage string concat total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.sql_usage = \"STRING_INTERP\"")
    result = cursor.fetchall()
    print("Sql_usage string interp total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.sql_usage = \"STRING_CONCAT_LIST\"")
    result = cursor.fetchall()
    print("Sql_usage string concat list total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.sql_usage = \"HARDCODED\"")
    result = cursor.fetchall()
    print("Sql_usage hardcoded total files: ", result[0])

    # Column_usage
    cursor.execute("SELECT count(*) from analyses,files WHERE analyses.column_usage = \"STRING_CONCAT\" AND files.id=analyses.file")
    result = cursor.fetchall()
    print("Column_usage string concat total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.column_usage = \"STRING_INTERP\"")
    result = cursor.fetchall()
    print("Column_usage string interp total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.column_usage = \"STRING_CONCAT_LIST\"")
    result = cursor.fetchall()
    print("Column_usage string concat list total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.column_usage = \"HARDCODED\"")
    result = cursor.fetchall()
    print("Column_usage hardcoded total files: ", result[0])

    # Table_usage
    cursor.execute("SELECT count(*) from analyses,files WHERE analyses.table_usage = \"STRING_CONCAT\" AND files.id=analyses.file")
    result = cursor.fetchall()
    print("Table_usage string concat total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.table_usage = \"STRING_INTERP\"")
    result = cursor.fetchall()
    print("Table_usage string interp total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.table_usage = \"STRING_CONCAT_LIST\"")
    result = cursor.fetchall()
    print("Table_usage string concat list total files: ", result[0])

    cursor.execute("SELECT count(*) from analyses WHERE analyses.table_usage = \"HARDCODED\"")
    result = cursor.fetchall()
    print("Table_usage hardcoded total files: ", result[0])

if __name__ == '__main__':
    # insert info for databases

    # code for table information
    # printTableInformation(java)

    # code for building SQL_usage graph
    # build_sql_usage_graph()

    # code for building column_usage graph
    # build_column_usage_graph()

    # code for building table_usage graph
    # build_table_usage_graph()

    # code for build sql usage and column usage graph
    build_column_and_sql_usage_graph()

