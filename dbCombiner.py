from mysql.connector import connect, Error

# Only YOU can prevent data fires!
# I highly recommend making the "from" database account be READ-ONLY (only select accesss)
#   and making a backup of the databases before continuing
def fromDbConnection():
    # The database to pull from
    try:
        mydb = connect(
            host="localhost",
            user="crawlerReadOnly",
            password="Super1Pass",
            database="crawler"
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb

def toDbConnection():
    # The database to save to
    try:
        mydb = connect(
            host="localhost",
            user="newCrawlerWrite",
            password="Super1Pass",
            database="crawlerDUPLICATE"
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
        value = lambda n : sqlResult[field_map[n]]
        self.gh_id = value("gh_id")
        self.originalFileID = value("id")
        self.originalProjectID = value("project")
        self.filename = value("filename")
        self.lang = value("lang")
        self.path = value("path")
        self.url = value("url")
        self.hash = value("hash")
        self.fileSize = value("fileSize")
        self.date_added = value("date_added")
        self.commit_date = value("commit_date")
        self.commit = value("commit")
    
    def process(self, toDB, fromDB):
        self.toDB = toDB
        self.fromDB = fromDB
        self.checkIfProjectExists()

        if self.newProjectID == -1:
            self.saveProject()
            self.saveRepo()
        
        self.checkIfFileExists()

        if self.newFileID == -1:
            self.saveFile()
            self.saveAnalysis()
        
        self.toDB.commit()
    
    def saveProject(self):
        with self.fromDB.cursor(prepared=True) as cursor:
            sql = "SELECT p.* FROM projects p WHERE p.gh_id = %s;"
            cursor.execute(sql, (self.gh_id,))

            field_map = fields(cursor)
            result = cursor.fetchone()
        with self.toDB.cursor(prepared=True) as cursor:
            sql = "INSERT INTO projects (`gh_id`, `owner`, `name`, `url`, `source`, `date_added`) VALUES (%s, %s, %s, %s, %s, %s);"
            v = lambda n: result[field_map[n]]
            vals = (v("gh_id"), v("owner"), v("name"), v("url"), v("source"), v("date_added"))
            cursor.execute(sql, vals)
            self.newProjectID = cursor.lastrowid
    
    def saveRepo(self):
        with self.fromDB.cursor(prepared=True) as cursor:
            sql = "SELECT r.* FROM repo_info r WHERE r.project = %s;"
            cursor.execute(sql, (self.originalProjectID,))
            field_map = fields(cursor)
            result = cursor.fetchone()
        
        if result is None:
            return # No repo_info available for this project
        
        with self.toDB.cursor(prepared=True) as cursor:
            sql = ("INSERT INTO repo_info (`project`, `gh_id`, `description`, "
                   "`releasesCount`, `LRName`, `LRCreated`, `LRUpdated`, `stargazerCount`, `forkCount`, "
                   "`watchersCount`, `createdAt`, `updatedAt`, `pushedAt`, `date_added`) "
                   "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            v = lambda n: result[field_map[n]]
            vals = (self.newProjectID, v("gh_id"), v("description"), 
                    v("releasesCount"), v("LRName"), v("LRCreated"), v("LRUpdated"), v("stargazerCount"), v("forkCount"),
                    v("watchersCount"),v("createdAt"),v("updatedAt"),v("pushedAt"),v("date_added"))
            cursor.execute(sql, vals)
    
    def saveFile(self):
        with self.toDB.cursor(prepared=True) as cursor:
            sql = ("INSERT INTO files (`project`, `filename`, `lang`, `path`, `url`, `hash`, "
                   "`fileSize`, `date_added`, `commit_date`, `commit`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            vals = (self.newProjectID, self.filename, self.lang, self.path, self.url, self.hash,
                    self.fileSize, self.date_added, self.commit_date, self.commit)
            cursor.execute(sql, vals)
            self.newFileID = cursor.lastrowid
    
    def saveAnalysis(self):
        with self.fromDB.cursor(prepared=True) as cursor:
            sql = "SELECT a.* FROM analyses a WHERE a.project = %s and a.file = %s;"
            cursor.execute(sql, (self.originalProjectID, self.originalFileID))

            field_map = fields(cursor)
            result = cursor.fetchone()
        
        if result is None:
            return # No analysis available for this file

        with self.toDB.cursor(prepared=True) as cursor:
            sql = ("INSERT INTO analyses (`project`, `file`, `analysis_date`, `sql_usage`, `is_parameterized`, `api_type`, "
                   "`sql_usage_lower`, `order_group_usage`, `like_usage`, `column_usage`, `table_usage`, `table_usage_lower`, "
                   "`view_usage`, `proc_usage`, `fun_usage`, `event_usage`, `trig_usage`, `index_usage`, `db_usage`, "
                   "`server_usage`, `tspace_usage`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);")
            v = lambda n: result[field_map[n]]
            vals = (self.newProjectID, self.newFileID, v("analysis_date"), v("sql_usage"), v("is_parameterized"), v("api_type"), 
                    v("sql_usage_lower"), v("order_group_usage"), v("like_usage"), v("column_usage"), v("table_usage"), v("table_usage_lower"),
                    v("view_usage"), v("proc_usage"), v("fun_usage"), v("event_usage"), v("trig_usage"), v("index_usage"), v("db_usage"), 
                    v("server_usage"), v("tspace_usage"))
            cursor.execute(sql, vals)
        

    def checkIfProjectExists(self):
        with self.toDB.cursor(prepared=True) as cursor:
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
        with self.toDB.cursor(prepared=True) as cursor:
            sql = "SELECT f.id FROM files f WHERE f.project = %s AND f.filename = %s AND f.path = %s;"
            cursor.execute(sql, (self.newProjectID, self.filename, self.path))

            field_map = fields(cursor)
            result = cursor.fetchone()
            if result is None:
                self.newFileID = -1
            else:
                self.newFileID = result[field_map["id"]]
        return self.newFileID

def get100Files(dbConnection, offset=0):
    with dbConnection.cursor(prepared=True) as cursor:
        sql = "SELECT p.gh_id, f.* FROM files f JOIN projects p ON p.id = f.project ORDER BY f.id LIMIT 100 OFFSET %s;"
        cursor.execute(sql, (offset,))

        field_map = fields(cursor)
        files = []
        for item in cursor:
            files.append(File(item, field_map))
    
    return files


if __name__ == '__main__':
    fromDB = fromDbConnection()
    toDB = toDbConnection()

    files = get100Files(fromDB, 0)

    files[0].process(toDB, fromDB)

    fromDB.close()
