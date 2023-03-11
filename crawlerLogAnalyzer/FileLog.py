import crawlerLogAnalyzer.TimedLogFile as TimedLogFile
import crawlerLogAnalyzer.LogFile as LogFile

class FileLog(TimedLogFile.TimedLogFile):

    def __init__(self, filename, ignoreFrames=False):
        self.cumulativeBytes = 0
        """The total number of bytes of code recorded in this log file"""

        self.frames = {}
        """Dictionary mapping frames (i.e., 500-1000) to pages (1,2,3)"""
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
        display = LogFile.biggestBytesUnit(self.cumulativeBytes)
        print(f"Total size of all files combined: {self.cumulativeBytes} (bytes) / {display[0]} ({display[1]})")
        display = LogFile.biggestBytesUnit(self.cumulativeBytes/self.currentLineNumber())
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
            # print(f"Possible number of shrinks: {possibleNumShrinks}")
            print("Found Frames: ", self.frames)