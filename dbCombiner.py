import json
from mysql.connector import connect, Error
import pymysql.cursors
import time

# Only YOU can prevent data fires!
# I highly recommend making the "from" database account be READ-ONLY (only select accesss)
#   and making a backup of the databases before continuing
def fromDbConnection():
    # The database to pull from
    try:
        mydb = pymysql.connect(
            host="127.0.0.1",
            user="csharpCrawler",
            password="Super1Pass",
            database="crawler-reader"
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb

def toDbConnection():
    # The database to save to
    try:
        mydb = pymysql.connect(
            host="127.0.0.1",
            user="csharpCrawler",
            password="Super1Pass",
            database="crawlernq"
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb

# From Python Cookbook
def fields(cursor):
    """ Given a DB API 2.0 cursor object that has been executed, returns
    a dictionary that maps each field name to a column index; 0 and up. """
    results = {}
    column = 0
    for d in cursor.description:
        results[d[0]] = column
        column = column + 1

    return results

class File():

    def __init__(self, sqlResult, field_map):
        """Creates a new Github File object based on results from database

        Args:
            sqlResult (List): Contains the results from the database
            field_map (Dictionary): Maps column name to the index in sqlResult
        """
        self.value = lambda n : sqlResult[field_map[n]]
        self.originalFileID = self.value("fileID")
        self.originalProjectID = self.value("projectID")
        self.newProjectID = -1
        self.newFileID = -1
        self.gh_id = self.value("gh_id")
    
    def process(self, toDB, fromDB):
        self.checkIfProjectExists()

        if self.newProjectID == -1:
            print(f"Adding project with ID {self.originalProjectID} in FROM database")
            self.saveProject()
            self.saveRepo()
            print(f"Project with {self.originalProjectID} in FROM database saved as {self.newProjectID} in TO database")
        else:
            print(f"Found existing project with ID {self.newProjectID} in TO database")
        
        self.checkIfFileExists()

        if self.newFileID == -1:
            print(f"Adding file with ID {self.originalFileID} in FROM database")
            self.saveFile()
            self.saveAnalysis() 
            print(f"File with {self.originalFileID} in FROM database saved as {self.newFileID} in TO database")
        else:
            print(f"Found existing file with ID {self.newFileID} in TO database")
        
        # Commit in main after all 100 are done, not here
    
    def saveProject(self):
        with File.toDB.cursor() as cursor:
            sql = "INSERT INTO projects (`gh_id`, `owner`, `name`, `url`, `source`, `date_added`) VALUES (%s, %s, %s, %s, %s, %s);"
            v = self.value
            vals = (v("gh_id"), v("owner"), v("name"), v("projectURL"), v("source"), v("projectAdded"))
            cursor.execute(sql, vals)
            self.newProjectID = cursor.lastrowid
    
    def saveRepo(self):        
        v = self.value
        if v("repoProject") is None:
            return # No repo_info available for this project
        
        print("Saving repo info")
        with File.toDB.cursor() as cursor:
            sql = ("INSERT INTO repo_info (`project`, `gh_id`, `description`, "
                   "`releasesCount`, `LRName`, `LRCreated`, `LRUpdated`, `stargazerCount`, `forkCount`, "
                   "`watchersCount`, `createdAt`, `updatedAt`, `pushedAt`, `date_added`) "
                   "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            vals = (self.newProjectID, v("gh_id"), v("description"), 
                    v("releasesCount"), v("LRName"), v("LRCreated"), v("LRUpdated"), v("stargazerCount"), v("forkCount"),
                    v("watchersCount"),v("createdAt"),v("updatedAt"),v("pushedAt"),v("repoDate"))
            cursor.execute(sql, vals)
    
    def saveFile(self):
        with File.toDB.cursor() as cursor:
            sql = ("INSERT INTO files (`project`, `filename`, `lang`, `path`, `url`, `hash`, "
                   "`fileSize`, `date_added`, `commit_date`, `commit`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            v = self.value
            vals = (self.newProjectID, v("filename"), v("lang"), v("path"), v("fileURL"), v("hash"),
                    v("fileSize"), v("fileAdded"), v("commit_date"), v("commit"))
            cursor.execute(sql, vals)
            self.newFileID = cursor.lastrowid
    
    def saveAnalysis(self):
        v = self.value
        if v("analysisID") is None:
            return # No analysis available for this file

        print("Saving analysis")
        with File.toDB.cursor() as cursor:
            sql = ("INSERT INTO analyses (`project`, `file`, `analysis_date`, `sql_usage`, `is_parameterized`, `api_type`, "
                   "`sql_usage_lower`, `order_group_usage`, `like_usage`, `column_usage`, `table_usage`, `table_usage_lower`, "
                   "`view_usage`, `proc_usage`, `fun_usage`, `event_usage`, `trig_usage`, `index_usage`, `db_usage`, "
                   "`server_usage`, `tspace_usage`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            vals = (self.newProjectID, self.newFileID, v("analysis_date"), v("sql_usage"), v("is_parameterized"), v("api_type"), 
                    v("sql_usage_lower"), v("order_group_usage"), v("like_usage"), v("column_usage"), v("table_usage"), v("table_usage_lower"),
                    v("view_usage"), v("proc_usage"), v("fun_usage"), v("event_usage"), v("trig_usage"), v("index_usage"), v("db_usage"), 
                    v("server_usage"), v("tspace_usage"))
            cursor.execute(sql, vals)
        

    def checkIfProjectExists(self):
        with File.toDB.cursor() as cursor:
            sql = "SELECT p.id, p.gh_id FROM projects p WHERE p.gh_id = %s;"
            cursor.execute(sql, (self.gh_id,))

            field_map = fields(cursor)
            result = cursor.fetchone()
            if result is None:
                self.newProjectID = -1
            else:
                self.newProjectID = result[field_map["id"]]
        return self.newProjectID

    def checkIfFileExists(self):
        with File.toDB.cursor() as cursor:
            sql = "SELECT f.id FROM files f WHERE f.project = %s AND f.filename = %s AND f.path = %s;"
            cursor.execute(sql, (self.newProjectID, self.value("filename"), self.value("path")))

            field_map = fields(cursor)
            result = cursor.fetchone()
            if result is None:
                self.newFileID = -1
            else:
                self.newFileID = result[field_map["id"]]
        return self.newFileID

def get100Files(dbConnection, offset=0):
    with dbConnection.cursor() as cursor:
        print(f"Pulling 100 files at offset {offset}")
        sql = """SELECT f.id as fileID, f.project as projectID, f.filename, f.lang, f.path, f.url as fileURL, f.hash, f.fileSize, 
                f.date_added as fileAdded, f.commit_date, f.commit, 
                
                p.gh_id, p.owner, p.name, p.url as projectURL, p.source, p.date_added as projectAdded, 
                
                r.project as repoProject, r.description, r.releasesCount, r.LRName, r.LRCreated, r.LRUpdated, r.stargazerCount, 
                r.forkCount, r.watchersCount, r.createdAt, r.updatedAt, r.pushedAt, r.date_added as repoDate, 
                
                a.id as analysisID, a.analysis_date, a.sql_usage, a.is_parameterized, a.api_type, a.sql_usage_lower, a.order_group_usage, a.like_usage, a.column_usage, a.table_usage, a.table_usage_lower,
                a.view_usage, a.proc_usage, a.fun_usage, a.event_usage, a.trig_usage, a.index_usage, a.db_usage, a.server_usage, a.tspace_usage
                
                FROM files f
                LEFT JOIN projects p ON f.project = p.id 
                LEFT JOIN repo_info r ON p.id = r.project
                LEFT JOIN analyses a ON f.project = a.project AND f.id = a.file
                ORDER BY f.id
                LIMIT 100 OFFSET %s;"""
        cursor.execute(sql, (offset,))

        field_map = fields(cursor)
        files = []
        for item in cursor:
            files.append(File(item, field_map))
    return files


if __name__ == '__main__':
    fromDB = fromDbConnection()
    toDB = toDbConnection()
    File.toDB = toDB
    File.fromDB = fromDB

    offset = 0
    with open("resume.txt", "r") as f:
            data = json.load(f)
            offset = data["offset"]

    files = get100Files(fromDB, offset)
    while len(files) > 0:
        start = time.time()
        for file in files:
            file.process(toDB, fromDB)
        offset += 100
        toDB.commit()
        with open("resume.txt", "w") as f:
            json.dump({"offset": offset}, f)
        
        print(f"------ 100 files in {time.time() - start}")
        files = get100Files(fromDB, offset)

    fromDB.close()
    toDB.close()
