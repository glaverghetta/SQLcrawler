import crawlerLogAnalyzer.LogFile as LogFile

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