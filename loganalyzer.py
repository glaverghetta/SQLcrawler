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
# Java-executeQuery-YYYY-MM-DD.zip

class LogFile():
    """A basic implementation for the log files being analyzed
    """
    fileName = ""
    """The name of the log file"""
    numLines = 0
    """The number of lines in the log file"""
    date = ""
    """The date string for the current log file"""
    firstLogTime = ""
    """The first log time recorded in the file"""
    lastLogTime = ""
    """The last log time recorded in the file"""

    def __init__(self, filename):
        self.date = filename.split("_")[1].split(".")[0]  # Get just the date part of string

    def getDate(self):
        pass
    
    def analyze(self):
        pass
    
    def analyzeLine(self, line):
        pass


class FileLog(LogFile):
    
    cumulativeBytes = 0
    """The total number of bytes of code recorded in this log file"""

    def __init__(self, filename):
        super().__init__(filename)
    
    def analyzeLine(self, line):
        # Stuff specific to this file
        super().analyzeLine(line)


if __name__ == '__main__':
    pass