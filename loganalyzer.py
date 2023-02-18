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

import dateutil.parser
import glob
import datetime

def biggestTimeUnit(ms):
    """Takes a time in milliseconds (ms) and converts to the best largest unit (ms, seconds, minutes, hours)

    Args:
        ms (int): Milliseconds to convert

    Returns:
        tuple: Element 0 is the numerical value, element 1 is a string denoting the unit type (ms, s, m, h)
    """
    if ms < 1000:
        return (ms, "ms")
    
    ms = ms / 1000 # To seconds
    if ms < 60:
        return (ms, "s")
    
    ms = ms / 60 # To minutes
    if ms < 60:
        return (ms, "m")
    
    ms = ms / 60 # To hours
    return (ms, "h")

def biggestBytesUnit(bytes):
    """Takes an int representing a number of bytes and converts to the best largest unit (bytes, kilo, mega, giga) (divides by 1000, not 1024)

    Args:
        bytes (int): Bytes to convert

    Returns:
        tuple: Element 0 is the numerical value, element 1 is a string denoting the unit type (b, kb, mb, gb)
    """
    if bytes < 1000:
        return (bytes, "b")
    
    bytes = bytes / 1000 # To kb
    if bytes < 1000:
        return (bytes, "kb")
    
    bytes = bytes / 1000 # To mb
    if bytes < 1000:
        return (bytes, "mb")
    
    bytes = bytes / 1000 # To gb
    return (bytes, "gb")

class LogFile():
    """A basic implementation for the log files being analyzed. Don't use directly, instead call one of the subclasses"""
    fileName = None
    """The name of the log file"""
    numLines = 0
    """The number of lines in the log file"""
    date = None
    """The date string for the current log file"""
    firstLogTime = None
    """The first log time recorded in the file"""
    lastLogTime = None
    """The last log time recorded in the file"""

    def __init__(self, filename):
        self.fileName = filename
        # dateutil parser doesn't actually get our format, so do it manually
        datestring = filename.split("_")[1].split(".")[0]
        date = [ int(i) for i in datestring.split("-")]
        self.date = datetime.datetime(date[0], date[1], date[2], date[3], date[4], date[5])
    
    def getDate(self):
        return self.date
    
    def displaySimpleDate(self, date):
        return date.strftime("%b/%d/%Y %I:%M:%S %p")
    
    def displayAccurateDate(self, date):
        return date.isoformat(sep=' ', timespec='milliseconds')

    def splitLine(self, line):
        """Splits a log line into its values

        Args:
            line (String): The log line to split

        Returns:
            List: List containing the values as Strings
        """
        vals = line.split(" ~ ")
        self.validate(vals)
        return vals
    
    def analyze(self):
        """Loops through each line of the file, extracting info"""
        with open(self.getFilename()) as file:
            while (line := file.readline().rstrip()):
                self.numLines += 1
                self.analyzeLine(line)
    
    def currentLineNumber(self):
        return self.numLines
    
    def getFilename(self):
        """Return the name of the file being analyzed"""
        return self.fileName
    
    def valsLogTime(self, vals):
        """Returns the time the log line was written"""
        return dateutil.parser.parse(vals[0])
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 1:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)
    
    def analyzeLine(self, line):
        """Generic analysis, things like total runtime for this file"""
        vals = self.splitLine(line)  # splitLine calls validate for us
        if self.firstLogTime is None:
            self.firstLogTime = self.valsLogTime(vals)
        self.lastLogTime = self.valsLogTime(vals)
        return vals
    
    def print(self):
        """Prints out all of the data gathered, useful for debugging"""
        print(f"Logfile: {self.getFilename()}")
        print(f"Date/Time: {self.displaySimpleDate(self.getDate())}")
        print(f"Log type: {type(self).__name__}")
        print(f"Total number of lines: {self.numLines}")
        print(f"First log time: {self.displayAccurateDate(self.firstLogTime)}")
        print(f"Last log time: {self.displayAccurateDate(self.lastLogTime)}")
        print(f"Total time of session (based on this file): {self.lastLogTime - self.firstLogTime})")
        

class TimedLogFile(LogFile):
    """A basic implementation for the timed log files being analyzed. Don't use directly, instead call one of the subclasses"""
    
    firstTimerStart = None
    firstTimerEnd = None
    lastTimerStart = None
    lastTimerEnd = None

    cumulativeRunTime = 0
    """The total runtime so far in ms"""

    def __init__(self, filename):
        super().__init__(filename)
    
    def analyzeLine(self, line):
        """Generic analysis, things like total runtime for this file"""
        vals = super().analyzeLine(line)
        if self.firstTimerStart is None:
            self.firstTimerStart = self.valsTimerStart(vals)
            self.firstTimerEnd = self.valsTimerEnd(vals)
        self.lastTimerStart = self.valsTimerStart(vals)  
        self.lastTimerEnd = self.valsTimerEnd(vals)
        self.cumulativeRunTime += self.valsRunningTime(vals)
        return vals
    
    def valsTimerStart(self, vals):
        """Returns the start time"""
        return dateutil.parser.parse(vals[1])
    
    def valsTimerEnd(self, vals):
        """Returns the end time"""
        return dateutil.parser.parse(vals[2])
    
    def valsRunningTime(self, vals):
        """Returns the running time in ms"""
        return int(vals[3])

    def validate(self, vals):
        """Ensures that the correct number of values have been found for this log type

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 3:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)

    def print(self):
        """Prints out all of the data gathered, useful for debugging"""
        super().print()
        print("-#-Run time information-#-")
        print(f"First file start time: {self.displayAccurateDate(self.firstTimerStart)}")
        print(f"First file end time: {self.displayAccurateDate(self.firstTimerEnd)}")
        print(f"Last file start time: {self.displayAccurateDate(self.lastTimerStart)}")
        print(f"Last file end time: {self.displayAccurateDate(self.lastTimerEnd)}")
        display = biggestTimeUnit(self.cumulativeRunTime)
        print(f"Total time running File: {self.cumulativeRunTime} (ms) / {display[0]} ({display[1]})")
        display = biggestTimeUnit(self.cumulativeRunTime/self.currentLineNumber())
        print(f"Average run time per file: {self.cumulativeRunTime/self.currentLineNumber()} (ms) / {display[0]} ({display[1]})")


class FileLog(TimedLogFile):
    
    cumulativeBytes = 0
    """The total number of bytes of code recorded in this log file"""

    ignoreFrames = False
    frames = {}
    """Dictionary mapping frames (i.e., 500-1000) to pages (1,2,3)"""

    def __init__(self, filename, ignoreFrames=False):
        self.ignoreFrames = ignoreFrames
        super().__init__(filename)
    
    def analyzeLine(self, line):
        # Generic runtime analysis handled by TimedLogFile class
        vals = super().analyzeLine(line)
        # Stuff specific to this file
        self.cumulativeBytes += self.valsNumBytes(vals)

        if not self.ignoreFrames:
            start = self.valsFrameStart(vals)
            end = self.valsFrameEnd(vals)
            page = self.valsPage(vals)
            range = str(start) + "-" + str(end)
            if range in self.frames:
                if page not in self.frames[range]:
                    self.frames[range].append(page)
            else:
                self.frames[range] = []
                self.frames[range].append(page)

    def valsPage(self, vals):
        """The page number on this line"""
        return int(vals[6])

    def valsFrameStart(self, vals):
        """The start of the frame on this line"""
        return int(vals[7])
    
    def valsFrameEnd(self, vals):
        """The end of the frame on this line"""
        return int(vals[8])
    
    def valsNumBytes(self, vals):
        """Returns the size of the file in bytes"""
        return int(vals[5])
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found (9 for FileLog)

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 9:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)
    
    def print(self):
        """Prints out all of the data gathered, useful for debugging"""
        super().print()
        print("-#-File log information-#-")
        print(f"Total number of new files added in this log file: {self.currentLineNumber()}")
        display = biggestBytesUnit(self.cumulativeBytes)
        print(f"Total size of all files combined: {self.cumulativeBytes} (bytes) / {display[0]} ({display[1]})")
        display = biggestBytesUnit(self.cumulativeBytes/self.currentLineNumber())
        print(f"Average size of files: {self.cumulativeBytes/self.currentLineNumber()} (bytes) / {display[0]} ({display[1]})")
        if not self.ignoreFrames:
            print("-#-Frame/Page estimates (see frame or page log for more accurate information)-#-")
            print(f"Total number of Frames: {len(self.frames)}")
            numPages = 0
            possibleNumShrinks = 0
            for _, i in self.frames.items():
                numPages += len(i)
                if len(i) == 1:
                    possibleNumShrinks += 1
            print(f"Total number of Pages: {numPages}")
            print(f"Average number of Pages per Frame: {numPages/len(self.frames)}")
            print(f"Possible number of shrinks: {possibleNumShrinks}")
            print("Found Frames: ", self.frames)


class GithubThrottlingLog(LogFile):
    
    def analyzeLine(self, line):
        # Stuff specific to this file

        # Generic analysis handled by LogFile class
        super().analyzeLine(line)
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found (7 for GithubThrottlingLog)

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 7:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)


if __name__ == '__main__':
    f = FileLog(glob.glob("logs/File*")[0])
    f.analyze()

    f.print()