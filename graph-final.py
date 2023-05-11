import glob
from mysql.connector import connect, Error
from multiprocessing import Process
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
from scipy import stats

SINGLE_COLUMN_SIZE = 3.3374
HEIGHT_SIZE = 2
DOUBLE_COLUMN_SIZE = 7 # Don't think this one works


matplotlib.use("pgf")
matplotlib.rcParams.update({
    "pgf.texsystem": "pdflatex",
    'font.family': 'serif',
    'text.usetex': True,
    'pgf.rcfonts': False,
})


class DBInfo:
  def __init__(self, user, password, dbname):
    self.user = user
    self.password = password
    self.dbname = dbname

# Database information
java = DBInfo("kevin", "Super1Password", "crawler")
cs_executescalar = DBInfo("root", "pass", "crawler-scalar")
cs_executereader = DBInfo("root", "pass", "crawler-scalar")
cs_executenonquery = DBInfo("root", "pass", "crawlernq")
cs_total_db = DBInfo("csharpCrawler", "Super1Pass", "crawlernq")
php = DBInfo("phpCrawler", "Super1Pass", "crawlerphp")

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

# Queries is_parameterized, sqlidia usage
def query_isparameterized_and_sqlidia_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    query = '''SELECT is_parameterized, order_group_usage, like_usage, column_usage, table_usage, view_usage, proc_usage, fun_usage, event_usage, trig_usage, 
     index_usage, db_usage, server_usage, tspace_usage from files, analyses WHERE files.id = analyses.file;'''
    cursor.execute(query)
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
            data.append((size, 1))
        else:
            data.append((size, 0))
    return data

# Queries stargazerCount, sql_usage
def query_starcount_and_sql_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT stargazerCount, sql_usage from unique_analyses, repo_info WHERE unique_analyses.project = repo_info.project;")
    result = cursor.fetchall()
    print("ERR")
    print(len(result))
    return result

def query_filesize_and_sql_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize, sql_usage from unique_analyses, files WHERE unique_analyses.file = files.id;")
    result = cursor.fetchall()
    print("ERR")
    print(len(result))
    return result

# Queries fileSize only
def query_file_size(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize from files;")
    result = cursor.fetchall()
    return result

# Queries fileSize, sql_usage
def query_file_size_and_sql_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize, sql_usage  from files, unique_analyses WHERE files.id = unique_analyses.file;")
    result = cursor.fetchall()
    return result

# Queries fileSize, table_usage
def query_file_size_and_table_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, table_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# Queries fileSize, column_usage
def query_file_size_and_column_usage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, column_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

def query_file_size_and_sqlidia(dbinfo):  # KEVIN USE THIS ONE
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    query = '''SELECT fileSize, order_group_usage, like_usage, column_usage, table_usage, view_usage, proc_usage, fun_usage, event_usage, trig_usage, 
     index_usage, db_usage, server_usage, tspace_usage from files, unique_analyses WHERE files.id = unique_analyses.file;'''
    cursor.execute(query)
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
            data.append((size, 1))
        else:
            data.append((size, 0))
    return data

def query_star_count_and_sqlidia(dbinfo):  # KEVIN USE THIS ONE
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    query = '''SELECT stargazerCount, order_group_usage, like_usage, column_usage, table_usage, view_usage, proc_usage, fun_usage, event_usage, trig_usage, 
     index_usage, db_usage, server_usage, tspace_usage from repo_info, unique_analyses WHERE repo_info.project = unique_analyses.project;'''
    cursor.execute(query)
    result = cursor.fetchall()
    data=[]
    for star, order, like, column, table, view, proc, fun, event, trig, index, db, server, tspace in result:
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
            data.append((star, 1))
        else:
            data.append((star, 0))
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

def build_sql_usage_vs_file_size_graph():  # HERE KEVIN
    raw_datas_sql = []
    raw_datas_sql.append(query_file_size_and_sql_usage(java))

    # for cs specifically, need to combine into one list
    # cs_scalar = query_file_size_and_sql_usage(cs_executescalar)
    # cs_reader = query_file_size_and_sql_usage(cs_executereader)
    # cs_nonquery = query_file_size_and_sql_usage(cs_executenonquery)
    # cs_total = cs_scalar + cs_reader + cs_nonquery
    cs_total = query_file_size_and_sql_usage(cs_total_db)
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(query_file_size_and_sql_usage(php))

    sanitized_data_sql = []
    for data in raw_datas_sql:
        sanitized_data_sql.append(sanitizeIfVulnerable(data))

    # Build graph
    buildGraph_FileSize_Vulnerable(sanitized_data_sql, range=1000, maxrange=40000, title="", ylabel="Percentage of files with concatenation")

def build_sqlidia_vs_file_size_graph():  # HERE KEVIN
    raw_datas_sql = []
    raw_datas_sql.append(query_file_size_and_sqlidia(java))

    cs_total = query_file_size_and_sqlidia(cs_total_db)
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(query_file_size_and_sqlidia(php))

    # Build graph
    buildGraph_FileSize_Vulnerable(raw_datas_sql, range=1000, maxrange=40000, title="", ylabel="Files with ID concat (\%)")


def build_sql_usage_vs_starcount():  # KEVIN
    raw_datas_sql = []
    raw_datas_sql.append(query_starcount_and_sql_usage(java))

    # for cs specifically, need to combine into one list
    # cs_scalar = query_starcount_and_sql_usage(cs_executescalar)
    # cs_reader = query_starcount_and_sql_usage(cs_executereader)
    # cs_nonquery = query_starcount_and_sql_usage(cs_executenonquery)
    cs_total = query_starcount_and_sql_usage(cs_total_db)
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(query_starcount_and_sql_usage(php))

    sanitized_data_sql = []
    for data in raw_datas_sql:
        sanitized_data_sql.append(sanitizeIfVulnerable(data))  
    
    buildGraph_StargazerCount_Vulnerable(sanitized_data_sql, range=100, maxrange=2000, ylabel="Percentage of files with concatenation")
    pass

def build_sqlidia_vs_starcount():  # KEVIN
    raw_datas_sql = []
    raw_datas_sql.append(query_star_count_and_sqlidia(java))

    cs_total = query_star_count_and_sqlidia(cs_total_db)
    raw_datas_sql.append(cs_total)
    raw_datas_sql.append(query_star_count_and_sqlidia(php))
    
    buildGraph_StargazerCount_Vulnerable(raw_datas_sql, range=100, maxrange=2000, ylabel="Files with ID concat (\%)")
    pass

# Given array of data in form (starcount, vulnerable)
# Build a graph with 3 lines
def buildGraph_StargazerCount_Vulnerable(datas, range=10000, maxrange=200000, title="Vulnerabilities vs. Stargazer Count", ylabel=""):
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
            bins.append(lower_bound)
            count = 0
            vuln_count = 0
            for starcount, vuln in dataset:
                if starcount == None:
                    error_count=error_count+1
                    continue
                if starcount < upper_bound and starcount > lower_bound:
                    count = count+1
                    if vuln == 1:
                        vuln_count = vuln_count + 1
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
    fig, ax = plt.subplots(figsize=(8, 4))
    fig.set_size_inches(w=SINGLE_COLUMN_SIZE, h=HEIGHT_SIZE)
    plt.plot(bins, datasets[0], label='Java', linestyle="-", color="orange",)
    plt.plot(bins, datasets[2], label='PHP', linestyle=":", color="green")
    plt.plot(bins, datasets[1], label='C\\#', linestyle="--", color="blue")
    plt.legend(loc="upper left", fontsize="x-small")

    plt.xlabel("Stargazer Count")
    plt.ylabel(ylabel)
    plt.show()

    plt.tight_layout()
    plt.savefig('stargazer_count.pgf')
    return 

# Given array of data in form (size, vulnerable)
# Build a graph with 3 lines
def buildGraph_FileSize_Vulnerable(datas, range=10000, maxrange=200000, title="Vulnerabilities vs. File Size", ylabel=""):  # KEVIN HERE
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
    fig, ax = plt.subplots(figsize=(8, 4))
    fig.set_size_inches(w=SINGLE_COLUMN_SIZE, h=HEIGHT_SIZE)
    plt.plot(bins, datasets[0], label='Java', linestyle="-", color="orange",)
    plt.plot(bins, datasets[2], label='PHP', linestyle=":", color="green")
    plt.plot(bins, datasets[1], label='C\\#', linestyle="--", color="blue")
    plt.legend(loc="upper left", fontsize="x-small")

    plt.xlabel("File Size (in MB)")
    plt.ylabel(ylabel)
    plt.show()

    plt.tight_layout()
    plt.savefig('filesize_count.pgf')
    return 

def build_file_size_distribution_graph():  # KEVIN HERE
    # NOTE: NEED TO DO
    n_bins = 1000
    fig, ax = plt.subplots(figsize=(8, 4))
    fig.set_size_inches(w=SINGLE_COLUMN_SIZE, h=HEIGHT_SIZE)
    java_data = query_file_size(java)
    sanitized_java_data = []
    for d in java_data:
        # the second condition can be removed if you want all files
        if d[0] == None: #or d[0] > 100000:
            continue
        sanitized_java_data.append(d[0]/1000)
    php_data = query_file_size(php)
    sanitized_php_data = []
    for d in php_data:
        # the second condition can be removed if you want all files
        if d[0] == None: #or d[0] > 100000:
            continue
        sanitized_php_data.append(d[0]/1000)
    cs_data = query_file_size(cs_total_db)
    sanitized_cs_data = []
    for d in cs_data:
        # the second condition can be removed if you want all files
        if d[0] == None: #or d[0] > 100000:
            continue
        sanitized_cs_data.append(d[0]/1000)
        
    arr = np.array(sanitized_java_data)
    ax.hist(arr, n_bins, density=True, histtype='step', cumulative=True, 
                             label='Java', linestyle="-", color="orange", linewidth=1.5)
    arr = np.array(sanitized_php_data)
    ax.hist(arr, n_bins, density=True, histtype='step', cumulative=True, 
                             label='PHP', linestyle=":", color="green", linewidth=1.5)
    arr = np.array(sanitized_cs_data)
    ax.hist(arr, n_bins, density=True, histtype='step', cumulative=True, 
                             label='C\\#', linestyle="--", color="blue", linewidth=1.5)

    ax.grid(True)
    plt.legend(loc="lower right", fontsize="x-small")
    # ax.set_title('File Size Histogram')
    ax.set_xlabel('File Size (MB)')
    ax.set_xlim(1, 40)
    ax.set_ylabel('Percentage of Files')
    plt.show()
    plt.grid(visible=False)

    plt.tight_layout()
    plt.savefig('histogram.pgf')
    

    pass

if __name__ == '__main__':

    build_file_size_distribution_graph()
    # build_sqlidia_vs_file_size_graph()
    # build_sqlidia_vs_starcount()

