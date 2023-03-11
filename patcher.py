import subprocess
import glob
from crawlerLogAnalyzer import GithubAPILog


def nextFile():
    files = sorted(glob.glob("logs/GithubAPI*.log"))

    finishedFiles = open("ignore.txt", "r").readlines()
    for f in files:
        if f not in finishedFiles:
            return f
    return None

def runHoles():
    filename = nextFile()
    while filename is not None:
        gh = GithubAPILog.GithubAPILog(filename)
        gh.analyze()

        for frame in gh.missedFrames:
            s = frame.split("..")
            start = s[0]
            end = s[1]
            command = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar",
                    gh.language, f"{int(end)+1}", "--start", start, "--end", end]

            with subprocess.Popen(' '.join(command), shell=True, stdout=subprocess.PIPE, bufsize=1,
                                universal_newlines=True, stderr=subprocess.STDOUT, encoding='utf-8') as p:
                for line in p.stdout:
                    print(line, end='')

        for i in gh.singleByteHoles:
            command = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar",
                    gh.language, f"{i+1}", "--start", f"{i}", "--end", f"{i}"]

            with subprocess.Popen(' '.join(command), shell=True, stdout=subprocess.PIPE, bufsize=1,
                                universal_newlines=True, stderr=subprocess.STDOUT, encoding='utf-8') as p:
                for line in p.stdout:
                    print(line, end='')
        
        with open("ignore.txt", "a") as f:
            f.write(filename + "\n")
        filename = nextFile()
    

if __name__ == '__main__':
    print("First... rebuild!")
    mvn = subprocess.run("mvn package", shell=True, stderr=subprocess.STDOUT, encoding='utf-8')
    if mvn.returncode != 0:
        print("Failed to build!")
        exit(-1)
    runHoles()
