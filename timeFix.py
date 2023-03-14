import glob
from crawlerLogAnalyzer import FileLog, FinalLog, GithubThrottlingLog, LogFile, GithubAPILog, TimedLogFile
from typing import List
import datetime


def getFixTime(logFile: LogFile.LogFile, vals: List[str]) -> str:
    if issubclass(type(logFile), TimedLogFile.TimedLogFile):
        return logFile.valsTimerEnd(vals)
    if issubclass(type(logFile), GithubThrottlingLog.GithubThrottlingLog):
        return logFile.valsContinueTime(vals) - datetime.timedelta(minutes=1)
    raise NotImplementedError()


def replaceTime(vals: List[str], toFix: datetime.datetime, correctTime: datetime.datetime, file, line):
    if (toFix.year == correctTime.year and toFix.month == correctTime.month
            and toFix.day == correctTime.day and toFix.minute == correctTime.minute
            and toFix.second == correctTime.second and toFix.microsecond == correctTime.microsecond):
        # Check if everything but the hour is the same; in that case, just use correctTime
        toFix = datetime.datetime(toFix.year, toFix.month, toFix.day, correctTime.hour, toFix.minute, toFix.second, toFix.microsecond, toFix.tzinfo)
        vals[0] = correctTime.strftime(f"%Y-%m-%dT%H:%M:%S.{correctTime.microsecond//1000:03d}%z")
    elif correctTime.hour > 12:
        hour = (toFix.hour+12) % 24
        toFix = datetime.datetime(toFix.year, toFix.month, toFix.day, hour, toFix.minute, toFix.second, toFix.microsecond, toFix.tzinfo)
        vals[0] = toFix.strftime(f"%Y-%m-%dT%H:%M:%S.{toFix.microsecond//1000:03d}%z")
    elif correctTime.hour == 0 and toFix.hour == 12:
        toFix = datetime.datetime(toFix.year, toFix.month, toFix.day, 0, toFix.minute, toFix.second, toFix.microsecond, toFix.tzinfo)
        vals[0] = toFix.strftime(f"%Y-%m-%dT00:%M:%S.{toFix.microsecond//1000:03d}%z")
    if toFix < correctTime:
        print(f"After altering, logTime is earlier than endTime! Line {line} in {file}")
        exit(-1)

    return vals

def fix(logFile: LogFile.LogFile):
    newFileName = logFile.getFilename().replace("logs", "logs_fixed")

    with open(logFile.getFilename(), "r") as oldFile, open(newFileName, "w") as newFile:
        lineNumber = 0
        while (line := oldFile.readline().rstrip()):
            lineNumber += 1
            vals = logFile.splitLine(line)
            toFix = logFile.valsLogTime(vals)
            correctTime = getFixTime(logFile, vals)
            vals = replaceTime(vals, toFix, correctTime, logFile.getFilename(), lineNumber)
            # print(vals)
            newFile.write(" ~ ".join(vals) + "\n")

if __name__ == '__main__':
    fileTypes = ["logs/Analyzer*", "logs/File*", "logs/Frame*", "logs/GithubAPI*", "logs/Network*", "logs/Page*"]
    
    LIMIT = 2

    for fileType in fileTypes:
        files = sorted(glob.glob(fileType))
        i = 0
        for file in files:
            if i > LIMIT:
                break
            log = TimedLogFile.TimedLogFile(file)
            fix(log)
            i += 1
    
    # Final, GithubThrottling are special
    files = sorted(glob.glob("logs/GithubThrottling*"))
    i = 0
    for file in files:
        if i > LIMIT:
            break
        log = GithubThrottlingLog.GithubThrottlingLog(file)
        fix(log)
        i += 1
    
    # Need something special for Final
    # files = sorted(glob.glob("logs/Final*"))
    # i = 0
    # for file in files:
    #     if i > LIMIT:
    #         break
    #     log = FinalLog.FinalLog(file)
    #     fix(log)
    #     i += 1
