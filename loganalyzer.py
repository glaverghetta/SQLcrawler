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
from crawlerLogAnalyzer import FileLog, LogFile, GithubAPILog, TimedLogFile
       
if __name__ == '__main__':
    f = FileLog.FileLog(sorted(glob.glob("logs/File*"))[0])
    # f.analyze()
    # f.print()

    # f = GithubAPILog(sorted(glob.glob("logs/GithubAPI*"))[0])
    # f.analyze()
    # f.print()

    i = 0
    files = sorted(glob.glob("logs/GithubAPI*"))
    # files = files[0:len(files)-2]
    for f in files:
        if i > 15:
            break
        i += 1
        a = GithubAPILog.GithubAPILog(f)
        a.analyze()
        print()
        print(statistics.median(a.getAll(TimedLogFile.valsRunningTime)))
        exit()
        a.print()
    print("\n--GLOBAL LOGFILE--")
    LogFile.LogFile.print()
    print("\n--GLOBAL GITHUBAPI--")
    GithubAPILog.GithubAPILog.print()