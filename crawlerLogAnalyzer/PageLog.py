import crawlerLogAnalyzer.LogFile as LogFile
import dateutil

class GithubThrottlingLog(LogFile.LogFile):
    
    def analyzeLine(self, line):
        vals = super().analyzeLine(line)
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found (7 for GithubThrottlingLog)

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 7:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)
    
    def valsPage(self, vals):
        """Return the page number"""
        return vals[4]

    def valsFrameStart(self, vals):
        """Return the start of the frame the page belongs to"""
        return vals[5]
    
    def valsFrameEnd(self, vals):
        """Return the end of the frame the page belongs to"""
        return vals[6]
    
    def valsResults(self, vals):
        """Return the number of results in the page"""
        return vals[7]