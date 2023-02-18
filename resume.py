import glob
import subprocess

CMD = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar"]

def findArguments(filename):
    with open(filename) as file:
        line = file.readline().rstrip()
        args = line.split("Arguments provided: ")[1].split(" ~ ")
    return args

def findDetails(filename):
    with open(filename) as file:
        while (line := file.readline().rstrip()):
            vals = line.split(" ~ ")
            if len(vals) < 7:
                print(f"Not enough values in {filename}")
                exit(-1)
            callType = vals[4]
            if callType != "search":
                continue
            page = vals[5]
            frameInfo = vals[6].split(" ")[-1].replace("size:", "").split("..")
            if(len(frameInfo) < 2):
                # Only a single byte frame
                frameStart = frameInfo[0]
                frameEnd = frameInfo[0]
            else:
                frameStart = frameInfo[0]
                frameEnd = frameInfo[1]
    return page, frameStart, frameEnd

if __name__ == '__main__':
    filename = glob.glob("logs/Final*")[-1]
    print(f"Most recent Final file {filename}")
    args = findArguments(filename)

    if(args[0] != "optimize"):
        print(f"Can't resume with last command {args[0]}")
        exit(-1)

    filename = filename.replace("logs/Final", "logs/GithubAPI")  # Get the most recent GithubAPI log file
    filename = filename.replace("logs\Final", "logs\GithubAPI")  # This line is for Windows :)
    print(f"Most recent GithubAPI file {filename}")

    page, start, end = findDetails(filename)

    newArgs = CMD
    ignore = False
    for i in args:
        if ignore:
            ignore = False
            continue
        if i == "--start" or i == "--end" or i == "--start-page":
            # Ignore the next operand as well
            ignore = True
            continue
        newArgs.append(i)

    newArgs.append("--start")
    newArgs.append(start)
    newArgs.append("--end")
    newArgs.append(end)
    newArgs.append("--start-page")
    newArgs.append(page)

    print(f"Restarting with frame {start}-{end}, page {page}")
    print(f"Running the following command: ", ' '.join(newArgs))
    print("But first... rebuild!")
    mvn = subprocess.run(["mvn", "package"], shell=True, stderr=subprocess.STDOUT)
    if mvn.returncode == 0:
        subprocess.run(newArgs, shell=True, stderr=subprocess.STDOUT)
        