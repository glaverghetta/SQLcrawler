import crawlerLogAnalyzer.LogFile as LogFile
import dateutil.parser


class TimedLogFile(LogFile.LogFile):
    """A basic implementation for the timed log files being analyzed. Don't use directly, instead call one of the subclasses"""

    globalCumulativeRunTime = 0
    """The total runtime for all timed log files"""

    def __init__(self, filename):
        self.firstTimerStart = None
        self.firstTimerEnd = None
        self.lastTimerStart = None
        self.lastTimerEnd = None

        self.cumulativeRunTime = 0
        """The total runtime in this log file so far in ms"""
        super().__init__(filename)
    
    def analyze(self):
        super().analyze()
        TimedLogFile.globalCumulativeRunTime += self.cumulativeRunTime
    
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

    def print_instance(self):
        """Prints out all of the data gathered, useful for debugging"""
        super().print_instance()
        print("-#-Run time information-#-")
        print(f"First file start time: {self.displayAccurateDate(self.firstTimerStart)}")
        print(f"First file end time: {self.displayAccurateDate(self.firstTimerEnd)}")
        print(f"Last file start time: {self.displayAccurateDate(self.lastTimerStart)}")
        print(f"Last file end time: {self.displayAccurateDate(self.lastTimerEnd)}")
        display = LogFile.biggestTimeUnit(self.cumulativeRunTime)
        print(f"Total time running of all lines in this file: {self.cumulativeRunTime} (ms) / {display[0]} ({display[1]})")
        display = LogFile.biggestTimeUnit(self.cumulativeRunTime/self.currentLineNumber())
        print(f"Average run time per line: {self.cumulativeRunTime/self.currentLineNumber()} (ms) / {display[0]} ({display[1]})")

    def print():
        display = LogFile.biggestTimeUnit(TimedLogFile.globalCumulativeRunTime)
        print(f"Total run time for all timed log files: {TimedLogFile.globalCumulativeRunTime} (ms) / {display[0]} ({display[1]})")