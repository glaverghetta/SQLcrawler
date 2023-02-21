import glob
from mysql.connector import connect, Error
from multiprocessing import Process
import matplotlib.pyplot as plt

def createDBConnection():
    try:
        mydb = connect(
            host="localhost",
            user="root",
            password="pass",
            database="crawlernq"
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb


def fetchData():
    mydb = createDBConnection()
    cursor = mydb.cursor()
    # Might need do double check this SQL statement
    cursor.execute("SELECT fileSize, sql_usage  from files, analyses WHERE files.id = analyses.file;")
    result = cursor.fetchall()
    return result

# returns a list of the sizes of data that are vulnerable
def sanitizeDataX(data):
    newdata = []
    for size, sql in data:
        if sql=="STRING_CONCAT":
            newdata.append(size)
        
    return newdata

# returns a list of tuples of data, (size, vulnerable)
def sanitizeDataXY(data):
    newdata=[]
    for size, sql in data:
        if sql=="STRING_CONCAT":
            newdata.append((size, 1))
        else:
            newdata.append((size, 0))
    return newdata

# motivation: want vulnerabilities for vulnerable file size / how many files in that file size
def buildGraph(data, range=20000):
    # sanitize data
    data = sanitizeDataXY(data)
    # build counts
    counts = []
    bins=[]
    lower_bound = 0
    upper_bound = range
    while upper_bound < 200000:
        bins.append(lower_bound/1000)
        count = 0
        vuln_count = 0
        for size, vuln in data:
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
    print(counts)

    plt.step(bins, counts, where='post')
    plt.xlabel("File ranges (in thousands)")
    plt.ylabel("Percentage of vulnerable files")
    plt.title("Vulnerabilities vs. File Size")
    plt.show()
    exit(-1)
    plt.bar(bins, counts, color ='maroon',
        width = 0.8)
    plt.xlabel("File sizes")
    plt.ylabel("Percentage files vulnerable")
    plt.show()    

# A simple histogram showing vulnerable files
def buildGraphSimpleHistogram(data):
    data = sanitizeDataX(data)
    plt.hist(data)
    plt.show()


def buildGraphFromData(data):
    mydb = createDBConnection()


if __name__ == '__main__':
    # fetch data from db
    raw_data = fetchData()
    # buildGraphSimpleHistogram(raw_data)
    buildGraph(raw_data)

    
