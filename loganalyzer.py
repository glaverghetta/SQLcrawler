# Things to analyze
# Total runtime
# Total runtime by feature (database, network, analyzer, by analyzer type)
# Min/Max times by feature
# Total number of bytes downloaded (File (This could be done in SQL), Network)
# Total number of API calls
# Total number of Secondary Rate Limits
# Total time waiting for secondary rate limits (just the total number times 60 seconds)
# Total number of other API Limits (I don't think these are ever hit)
# Total number of frames
# Average frame length
# Min/Max frame length
# Total number of pages
# Average number of pages in a frame
# Min/Max number of pages in a frame
# Files reported by Github vs number actually returned
# Min/Max files in a frame
# Avg files in a frame
# Any potential holes in frames (possibly to analyze later if these are not negligable)
#   These holes could be due to the growing being incorrect, or from GitHub returning 0 results
# Total times Github returned 0 results in a page
# Total number of times Github returned 0 results after retrying (and how many times retrying fixed it)
# The time of day for API blockage sort of things 
# 
# For DB: File size vs vulnerabilities
# TODO: FIRST FIX POTENTIAL GROW ISSUE  -- DONE :D
# TODO: Any files NOT analyzed?  -- Yes, a few, add a new main option to fix, similar to Github repo info
# TODO: Any projects NOT populated?  -- Yes, already have a tool for this :) 
# TODO: Files missing filesize, not in logs -- Yes, a couple; probably also missing analysis then. Add fix for this to analysis part
# TODO: Duplicate analyses?  -- Nope :) (Query looking for analyses with same File ID, but different analysis ID returns 0 rows)
# TODO: All single size frames need to be rerun; illformed API call.
# TODO: All first frames with size 0 need to be rerun; reset code didn't reset if first page failed.
# Java-executeQuery-YYYY-MM-DD.zip

# C# - Done, 3 separate batches (different by queryString)
# Parisa - PHP to 11000
# Bianca - Above that split across two computers
# Kevin - Three computers of java
# Left is NodeJS

import glob
import statistics

from matplotlib import pyplot as plt
import numpy as np
from crawlerLogAnalyzer import FileLog, FrameLog, GithubThrottlingLog, LogFile, GithubAPILog, TimedLogFile, PageLog

import datetime as dt
import matplotlib.dates as mdates
import matplotlib

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

def pageSizeAndTime(pageLog:PageLog.PageLog, vals):
    return (pageLog.valsTimerStart(vals), pageLog.valsResults(vals))

def pageSizesByTimeOfDay(pageLogs, data=None):
    if data is None:
        data = {"Sunday": {}, "Monday": {}, "Tuesday": {}, "Wednesday": {}, "Thursday": {}, "Friday": {}, "Saturday": {}}

        for _, i in data.items():
            for j in range(0, 24):
                i[f"{j:02d}"] = []
    
    for i in pageLogs:
        p = PageLog.PageLog(i)

        vals = p.getAll(pageSizeAndTime)

        for entry in vals:
            try:
                data[entry[0].strftime('%A')][f"{entry[0].hour:02d}"].append(int(entry[1]))
            except Exception as e:
                print(e)
                print(entry[0].strftime('%A'), entry[0].hour, entry)
                exit(-1)
    return data

def throttlesByTimeOfDay(ghTLogs, data=None):
    if data is None:
        data = {"Sunday": {}, "Monday": {}, "Tuesday": {}, "Wednesday": {}, "Thursday": {}, "Friday": {}, "Saturday": {}}

        for _, i in data.items():
            for j in range(0, 24):
                i[f"{j:02d}"] = []
    
    for i in ghTLogs:
        p = GithubThrottlingLog.GithubThrottlingLog(i)

        vals = p.getAll(GithubThrottlingLog.GithubThrottlingLog.valsContinueTime)

        for entry in vals:
            try:
                data[entry.strftime('%A')][f"{entry.hour:02d}"].append(1)
            except Exception as e:
                print(e)
                print(entry.strftime('%A'), entry.hour, entry)
                exit(-1)
    return data

def convert(day, time):
    # Just map to the first day of the year, as we will drop day/month/year
    weekday = {"Sunday": 1, "Monday": 2, "Tuesday": 3,"Wednesday": 4,"Thursday": 5,"Friday": 6,"Saturday": 7}

    return dt.datetime(year=2023, month=1, day=weekday[day], hour=int(time))

def graphByDate(java_data, php_data=None, c_data=None, y_label="", filename=""):
    # Convert your x-data into an appropriate format.

    # date_fmt is a string giving the correct format for your data. In this case
    # we are using 'YYYYMMDD.0' as your dates are actually floats.

    # Use a list comprehension to convert your dates into datetime objects.
    # In the list comp. strptime is used to convert from a string to a datetime
    # object.
    # dt_x = [dt.datetime.strptime(str(i), date_fmt) for i in raw_x]
    java_x = []
    java_y = []

    for day,times in java_data.items():
        for time, values in times.items():
            # times[time] = sum(values) / len(values)
            java_x.append(convert(day, time))
            java_y.append(values)
    
    # php_x = []
    # php_y = []

    # for day,times in php_data.items():
    #     for time, values in times.items():
    #         # times[time] = sum(values) / len(values)
    #         if values != 0:
    #             php_x.append(convert(day, time))
    #             php_y.append(values)
    #         else:
    #             php_x.append(convert(day, time))
    #             php_y.append(False)
    
    # c_x = []
    # c_y = []

    # for day,times in c_data.items():
    #     for time, values in times.items():
    #         # times[time] = sum(values) / len(values)
    #         c_x.append(convert(day, time))
    #         c_y.append(values)

    # Now to actually plot your data.
    fig, ax = plt.subplots()
    fig.set_size_inches(w=DOUBLE_COLUMN_SIZE, h=HEIGHT_SIZE)

    # Use plot_date rather than plot when dealing with time data.
    # ax.plot_date(x, y, 'bo-', label="Java", linestyle="-", )

    # plt.plot_date(java_x, java_y, label="Java", linestyle="-", color="orange")
    # plt.plot_date(php_x, php_y, label='PHP', linestyle=":", color="green")
    # plt.plot_date(c_x, c_y, label='C\\#', linestyle="--", color="blue")
    # plt.legend()

    plt.plot_date(java_x, java_y, linestyle="-", color="orange", ms=1.5)

    # Create a DateFormatter object which will format your tick labels properly.
    # As given in your question I have chosen "YYMMDD"
    date_formatter = mdates.DateFormatter('%a-%H')

    # Set the major tick formatter to use your date formatter.
    ax.xaxis.set_major_formatter(date_formatter)

    # This simply rotates the x-axis tick labels slightly so they fit nicely.
    fig.autofmt_xdate()

    plt.xlabel("Time of day (UTC/GMT -4)")
    plt.ylabel(y_label)
    plt.show()

    plt.tight_layout()
    plt.savefig(filename)


def graphPagesByDate():
    java_data = pageSizesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\java\\page\\Page*")))
    php_data = pageSizesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\php\\page\\Page*")))
    c_data = pageSizesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\csharp\\page\\Page*")))

    # Now average
    for day,times in php_data.items():
        for time, values in times.items():
            java_data[day][time] += values

    # Now average
    for day,times in c_data.items():
        for time, values in times.items():
            java_data[day][time] += values
        
    # Now average
    for day,times in java_data.items():
        for time, values in times.items():
                times[time] = sum(values) / len(values)
    # print(java_data)
    graphByDate(java_data, y_label="Avg results", filename='pageSizeByDate.pgf')

def graphThrottleByDate():
    java_data = throttlesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\java\\github_throttling\\GithubThrottling*")))
    php_data = throttlesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\php\\github_throttling\\GithubThrottling*")))
    c_data = throttlesByTimeOfDay(sorted(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\csharp\\github_throttling\\GithubThrottling*")))

    # Now average
    for day,times in php_data.items():
        for time, values in times.items():
            java_data[day][time] += values

    # Now average
    for day,times in c_data.items():
        for time, values in times.items():
            java_data[day][time] += values
        
    # Now average
    for day,times in java_data.items():
        for time, values in times.items():
                times[time] = sum(values)
    # print(java_data)
    graphByDate(java_data, y_label="Total limits", filename='throttleByDate.pgf')

def countLines(names):
    count = 0
    for f in names:
        p = LogFile.LogFile(f)

        count += sum(p.getAll(lambda a,b : 1))  # Just return one, counts number of lines
    return count

if __name__ == '__main__':
    # graphPagesByDate()
    graphThrottleByDate()

    # jPages = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\java\\page\\Page*"))
    # jFrames = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\java\\frame\\Frame*"))
    # pPages = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\php\\page\\Page*"))
    # pFrames = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\php\\frame\\Frame*"))
    # cPages = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\csharp\\page\\Page*"))
    # cFrames = countLines(glob.glob("C:\\Users\\kevin\\Desktop\\logs\\csharp\\frame\\Frame*"))
    # print(f"Java had {jPages} pages, {jFrames} frames")
    # print(f"PHP had {pPages} pages, {pFrames} frames")
    # print(f"C# had {cPages} pages, {cFrames} frames")
    # print(f"Total of {jPages + pPages + cPages} pages, {jFrames + pFrames + cFrames} frames")


    # f = FileLog.FileLog(sorted(glob.glob("logs/File*"))[0])
    # f.analyze()
    # f.print()

    # f = GithubAPILog(sorted(glob.glob("logs/GithubAPI*"))[0])
    # f.analyze()
    # f.print()

    # i = 0
    # files = sorted(glob.glob("/mnt/c/Users/kevin/Desktop/java-executeQuery-3-15-2023/logs/GithubT*"))
    # files = sorted(glob.glob("logs/GithubT*"))
    # # files = files[0:len(files)-2]
    # for f in files:
    #     i += 1
    #     a = LogFile.LogFile(f)
    #     a.analyze()
    #     # a.print()
    # LogFile.LogFile.print()
    # files = sorted(glob.glob("/mnt/c/Users/kevin/Desktop/java-executeQuery-3-15-2023/logs/Frame*"))
    # files = sorted(glob.glob("logs/Frame*"))
    # # files = files[0:len(files)-2]
    # for f in files:
    #     i += 1
    #     a = TimedLogFile.TimedLogFile(f)
    #     a.analyze()
    #     # a.print()
    # TimedLogFile.TimedLogFile.print()
    # print("\n--GLOBAL LOGFILE--")
    # print("\n--GLOBAL GITHUBAPI--")