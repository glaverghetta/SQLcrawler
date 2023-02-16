import glob
from mysql.connector import connect, Error
from multiprocessing import Process

def createDBConnection():
    try:
        mydb = connect(
            host="localhost",
            user="kevin",
            password="Super1Password",
            database="crawler"
        )
    except Error as e:
        print(e)
        exit(-1)
    return mydb


def saveData(valsToAdd, mydb, name, threadNumber):
    # We are inserting a lot of data; combining rows into one transaction is a bit more effecient
    if len(valsToAdd) == 0:
        return
    
    # Line up data for prepared statement
    data = [(i[0], i[1],) for i in valsToAdd]
    
    with mydb.cursor(prepared=True) as cursor:
        try:
            sql = "UPDATE files SET fileSize = %s WHERE id = %s;"
            #print(f"Updating file {id} with fileSize {size}")
            cursor.executemany(sql, data)
            mydb.commit()
        except Error as e:
            print(f"Thread {threadNumber}: Encountered error when updating fileSize for some files in {name}: (id, size, line number)")
            print(valsToAdd)
            print(e)
            mydb.close()
            exit(-1)


def handleFiles(fileNames, threadNumber):
    mydb = createDBConnection()

    count = 0
    for name in fileNames:
        count += 1
        print(f"Thread {threadNumber}: Reading log file {count} of {len(fileNames)}, {name}")
        with open(name) as file:
            lineNumber = 0
            valsToAdd = []
            while (line := file.readline().rstrip()):
                lineNumber += 1
                vals = line.split(" ~ ")
                if len(vals) != 9:
                    print(f"Thread {threadNumber}: Failed to parse values on line {lineNumber} in {name}")
                    exit(-1)
                id = vals[4]
                size = vals[5]
                valsToAdd.append((id,size,lineNumber))

                if len(valsToAdd) >= 100:
                    saveData(valsToAdd, mydb, name, threadNumber)
                    valsToAdd = []
                
            if len(valsToAdd) > 0:
                saveData(valsToAdd, mydb, name, threadNumber)  # Save any remaining data
        print(f"Thread {threadNumber}: Completed log file {name}")
    print(f"Thread {threadNumber}: Completed all log files")
    mydb.close()

if __name__ == '__main__':
    files = glob.glob("logs/File*")

    NUMTHREADS = 1

    threads = []  # Technically, using multiprocessing to avoid GIL (use ALL the cores :D )
    for i in range(0, NUMTHREADS):
        size = len(files)//NUMTHREADS + 1  # Int division, rounded up for more even spread

        if i == NUMTHREADS - 1:
            # Last thread, handle whatever (possible smaller) group size is left 
            filesToHandle = files[i*size:]
        else:
            filesToHandle = files[i*size:i*size+size]
        
        print(f"Thread {i+1} has {len(filesToHandle)}: {filesToHandle}", filesToHandle)

        threads.append(Process(target=handleFiles, args=(filesToHandle, i+1)))

    for t in threads:
        t.start()

    for t in threads:
        t.join()

    print("Finished adding file size to database")