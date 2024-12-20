import datetime 
import dateutil.parser
import os

def biggestTimeUnit(ms : int):
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

def biggestBytesUnit(bytes : int):
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
    globalNumFiles = 0
    """The total number of log files that have been read"""
    globalFirstLogTime = None
    """The first log time recorded in any file analyzed so far"""
    globalLastLogTime = None
    """The last log time recorded in any file analyzed so far"""
    globalNumLines = 0

    def __init__(self, filename : str):
        self.print = self.print_instance 
        self.fileName = filename
        """The name of the log file"""
        LogFile.globalNumFiles += 1
        self.numLines = 0
        """The number of lines in the log file"""
        self.firstLogTime = None
        """The first log time recorded in the file"""
        self.lastLogTime = None
        """The last log time recorded in the file"""

        # dateutil parser doesn't actually get our format, so do it manually
        datestring = os.path.basename(filename).split("_")[1].split(".")[0]
        date = [ int(i) for i in datestring.split("-")]
        self.date = datetime.datetime(date[0], date[1], date[2], date[3], date[4], date[5])
        """The date string for the current log file"""
    
    def getDate(self):
        return self.date
    
    def displaySimpleDate(self, date):
        return date.strftime("%b/%d/%Y %I:%M:%S %p")
    
    def displayAccurateDate(self, date):
        return date.isoformat(sep=' ', timespec='milliseconds')

    def getAll(self, method):
        """Calls a function on every line in the log file.
        Very useful for custom aggregation. 
        
        For example, the following returns the median runtime in a timed log file

        statistics.median(myLog.getAll(TimedLogFile.TimedLogFile.valsRunningTime))

        Args:
            method (function): The function to call on every line; passes self as first arg, vals on the line as second arg

        Returns:
            List: Contains all of the results from calling the function on every line
        """
        items = []
        with open(self.getFilename()) as file:
            while (line := file.readline().rstrip()):
                x = method(self, self.splitLine(line))
                if x is not None:
                    items.append(x)
        return items

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
                LogFile.globalNumLines += 1
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
        logTime = self.valsLogTime(vals)
        if self.firstLogTime is None:
            self.firstLogTime = logTime
        self.lastLogTime = logTime

        if LogFile.globalFirstLogTime is None or logTime < LogFile.globalFirstLogTime:
           LogFile.globalFirstLogTime = logTime
        if LogFile.globalLastLogTime is None or logTime > LogFile.globalLastLogTime:
           LogFile.globalLastLogTime = logTime
        return vals
    
    def print_instance(self):
        """Prints out all of the data gathered, useful for debugging"""
        print(f"Logfile: {self.getFilename()}")
        print(f"Date/Time: {self.displaySimpleDate(self.getDate())}")
        print(f"Log type: {type(self).__name__}")
        print(f"Total number of lines: {self.numLines}")
        print(f"First log time: {self.displayAccurateDate(self.firstLogTime)}")
        print(f"Last log time: {self.displayAccurateDate(self.lastLogTime)}")
        print(f"Total time of session (based on this file): {self.lastLogTime - self.firstLogTime})")
    
    def print():
        print(f"Analyzed a total of {LogFile.globalNumFiles} files")
        print(f"First recorded log time at {LogFile.globalFirstLogTime}")
        print(f"Last recorded log time at {LogFile.globalLastLogTime}")
        print(f"Difference between last log time and first: {LogFile.globalLastLogTime - LogFile.globalFirstLogTime}")
        print(f"Total number of lines: {LogFile.globalNumLines}")