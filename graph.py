import glob
from mysql.connector import connect, Error
from multiprocessing import Process
import matplotlib.pyplot as plt

class DBInfo:
  def __init__(self, user, password, db):
    self.user = user
    self.password = password
    self.db = db

def createDBConnection(dbinfo):
    try:
        mydb = connect(
            host="localhost",
            user=dbinfo.user,
            password=dbinfo.password,
            database=dbinfo.db
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb

# Queries sql_usage column of analyses table
def fetchFileSizeAndSqlUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize, column_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# Queries table_usage column of analyses table
def fetchFileSizeAndTableUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, table_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# Queries column_usage column of analyses table
def fetchFileSizeAndColumnUsage(dbinfo):
    mydb = createDBConnection(dbinfo)
    cursor = mydb.cursor()
    cursor.execute("SELECT fileSize, column_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# returns a list of tuples of data: (size, vulnerable) from (size, usage)
def sanitizeDataStringConcatAndInterp(data):
    newdata=[]
    for size, sql in data:
        if sql=="STRING_CONCAT" or sql=="STRING_INTERP":
            newdata.append((size, 1))
        else:
            newdata.append((size, 0))
    return newdata

# Given array of data in form (size, vulnerable)
def buildGraph_CodeByFileSize(datas, range=20000, maxrange=200000):
    # sanitize data
    sanitized = []
    for data in datas:
        # If you wanted to just string concat or just interp - this is interchangeable functional call
        sanitized.append(sanitizeDataStringConcatAndInterp(data))

    datasets = []
    bins=[]
    # Go through each dataset
    for dataset in sanitized:
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
                    continue
                if size < upper_bound and size > lower_bound:
                    count = count+1
                    if vuln == 1:
                        vuln_count = vuln_count + 1
            if count != 0:
                counts.append(vuln_count / count)
            else:
                counts.append(0)
            lower_bound = lower_bound + range
            upper_bound = upper_bound + range
        datasets.append(counts)

    # Assuming 3 lines 
    plt.plot(bins, datasets[0], label="Java", linestyle="-")
    plt.plot(bins, datasets[1], label="C#", linestyle="--")
    plt.plot(bins, datasets[2], label="Php", linestyle="-.")
    plt.legend()

    plt.xlabel("File Size(in MB)")
    plt.ylabel("Percentage of concatenated")
    plt.title("Vulnerabilities vs. File Size")
    plt.show()
    exit(-1)
    plt.bar(bins, counts, color ='maroon',
        width = 0.8)
    plt.xlabel("File sizes")
    plt.ylabel("Percentage files vulnerable")
    plt.show()    

if __name__ == '__main__':
    # insert info for databases
    java = DBInfo("root", "pass", "crawler")
    cs_executescalar = DBInfo("root", "pass", "crawler-scalar")
    cs_executereader = DBInfo("root", "pass", "crawler-scalar")
    cs_executenonquery = DBInfo("root", "pass", "crawlernq")
    php = DBInfo("root", "pass", "php_11")

    # fetch data
    raw_datas = []
    raw_datas.append(fetchFileSizeAndSqlUsage(java))

    # for cs specifically, need to combine into one list
    # TODO: would this be better to combine in python, and query one cs db? due to duplicates?
    cs_scalar = fetchFileSizeAndSqlUsage(cs_executescalar)
    cs_reader = fetchFileSizeAndSqlUsage(cs_executereader)
    cs_nonquery = fetchFileSizeAndSqlUsage(cs_executenonquery)
    cs_total = cs_scalar + cs_reader + cs_nonquery
    raw_datas.append(cs_total)
    raw_datas.append(fetchFileSizeAndSqlUsage(php))

    buildGraph_CodeByFileSize(raw_datas)