from crawlerLogAnalyzer import TimedLogFile
import crawlerLogAnalyzer.LogFile as LogFile
import dateutil

class NetworkLog(TimedLogFile.TimedLogFile):
    
    def analyzeLine(self, line):
        vals = super().analyzeLine(line)
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found (7 for NetworkLog)

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 6:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)
    
    def valsURL(self, vals):
        """Return the url"""
        return vals[4]

    def valsCode(self, vals):
        """Return the HTTP status code"""
        return vals[5]