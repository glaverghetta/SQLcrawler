from collections import namedtuple
import crawlerLogAnalyzer.TimedLogFile as TimedLogFile
import crawlerLogAnalyzer.LogFile as LogFile

class GithubAPILog(TimedLogFile.TimedLogFile):

    QueryString = namedtuple("QueryString", ["query", "language", "frame"])
    totalSearchCalls = 0
    totalGraphCalls = 0
    totalSearchResults = 0
    totalSearchBytes = 0
    totalGraphResults = 0
    totalGraphBytes = 0
    totalFrames = {}
    totalSingleBytesHoles = []
    totalMissedFrames = []
    numAPIFiles = 0

    def __init__(self, filename, ignoreFrames=False):
        self.searchCalls = 0
        self.graphCalls = 0
        self.totalSearchResults = 0
        self.totalSearchBytes = 0
        self.totalGraphResults = 0
        self.totalGraphBytes = 0
        self.ignoreFrames = False
        self.query = None
        self.language = None
        self.frames = {}
        """Dictionary mapping frames (i.e., 500-1000) to pages (1,2,3)"""
        self.singleByteHoles = []

        self.missedFrames = []
        self.lastSize = None
        self.shrinks = 0
        self.ignoreFrames = ignoreFrames
        super().__init__(filename)

    def analyzeLine(self, line):
        vals = super().analyzeLine(line)

        if self.valsAPIEndpoint(vals) == "search":
            self.searchCalls += 1
            self.totalSearchResults += self.valsNumResults(vals)
            self.totalSearchBytes += self.valsResponseSize(vals)
            GithubAPILog.totalSearchCalls += 1
            GithubAPILog.totalSearchResults += self.valsNumResults(vals)
            GithubAPILog.totalSearchBytes += self.valsResponseSize(vals)

            self.query, self.language, frame = self.valsQueryString(vals)
            if not self.ignoreFrames:
                if len(frame) == 1:
                    # This is from an error in old version, mark as a hole to patch
                    #TODO:Mark as hole to patch
                    start = frame[0]
                    end = frame[0]
                    self.singleByteHoles.append(start)
                    GithubAPILog.totalSingleBytesHoles.append(start)
                else:
                    start = frame[0]
                    end = frame[1]

                if(self.lastSize == None):
                    self.lastSize = end
                
                if end < self.lastSize:
                    # Must of shrunk
                    self.shrinks += 1
                    self.lastSize = end
                elif end > self.lastSize:
                    # Make sure the next start is only one byte above
                    if self.lastSize + 1 != start and f"{self.lastSize+1}..{start-1}" not in self.missedFrames:
                        # Found a hole
                        self.missedFrames.append(f"{self.lastSize+1}..{start-1}")
                        GithubAPILog.totalMissedFrames.append(f"{self.lastSize+1}..{start-1}")
                self.lastSize = end
                
                page = self.valsPage(vals)
                range = str(start) + "-" + str(end)
                if range in self.frames:
                    if page not in self.frames[range]:
                        self.frames[range].append(page)
                        GithubAPILog.totalFrames[range].append(page)
                else:
                    self.frames[range] = []
                    GithubAPILog.totalFrames[range] = []
                    self.frames[range].append(page)
                    GithubAPILog.totalFrames[range].append(page)
        else:
            self.graphCalls += 1
            self.totalGraphResults += self.valsNumResults(vals)
            self.totalGraphBytes += self.valsResponseSize(vals)

            GithubAPILog.totalGraphCalls += 1
            GithubAPILog.totalGraphResults += self.valsNumResults(vals)
            GithubAPILog.totalGraphBytes += self.valsResponseSize(vals)
    
    def analyze(self):
        GithubAPILog.numAPIFiles += 1
        super().analyze()
    
    def valsAPIEndpoint(self, vals):
        """Return the API endpoint"""
        return vals[4]

    def valsPage(self, vals):
        """Return the page number if search API call, -1 if graphQL"""
        if self.valsAPIEndpoint(vals) == "search":
            return int(vals[5])
        return -1
    
    def valsQueryString(self, vals):
        """Return a tuple containing the three parts of the query string if search API call, None if graphQL"""
        if self.valsAPIEndpoint(vals) == "search":
            a = vals[6].split(" ")
            query = a[0]
            language = a[1].split(":")[1]
            frame = a[2].split(":")[1]
            if ".." in frame:
                sizes = frame.split("..")
                frame = (int(sizes[0]), int(sizes[1]))
            else:
                frame = (int(frame),)  # Single-byte frame, this denotes an error! (Fixed in updated version)
            return self.QueryString(query=query,language=language, frame=frame)
        return None

    def valsResponseSize(self, vals):
        """Return the size in bytes of the API response"""
        return int(vals[7])

    def valsNumResults(self, vals):
        """Return the number of results in the API response"""
        return int(vals[8])
    
    def validate(self, vals):
        """Ensures that the correct number of values have been found (9 for GithubAPILog)

        Args:
            vals (List): The list of values to check
        """
        if len(vals) < 9:
            print(f"Too few values on line {self.currentLineNumber()} of {self.getFilename()}")
            exit(-1)

    def print_instance(self):
        """Prints out all of the data gathered, useful for debugging"""
        super().print_instance()
        #TODO: Stuff
        if len(self.frames) == 0:
            "No frames in file (log is associated with repo command only)"
            return 
        if not self.ignoreFrames:
            print("-#-Frame/Page estimates (see frame or page log for more accurate information)-#-")
            print(f"Total number of Frames: {len(self.frames)}")
            numPages = 0
            possibleNumShrinks = 0
            for _, i in self.frames.items():
                numPages += len(i)
            print(f"Total number of Pages: {numPages}")
            print(f"Total number of shrinks: {self.shrinks}")
            print(f"Average number of Pages per Frame: {numPages/len(self.frames)}")
            # print(f"Possible number of shrinks: {possibleNumShrinks}")
            print(f"Found Frames: {self.frames}")
            print(f"Single byte holes: {self.singleByteHoles}")
            print(f"Missed frames (Growth holes): {self.missedFrames}")
    
    def print():
        print(f"Total number of GithubAPI files analyzed: {GithubAPILog.numAPIFiles}")
        print(f"Total number of search calls: {GithubAPILog.totalSearchCalls}")
        print(f"Total number of search results: {GithubAPILog.totalSearchResults}")
        a = LogFile.biggestBytesUnit(GithubAPILog.totalSearchBytes)
        print(f"Total number of search bytes: {GithubAPILog.totalSearchBytes} (bytes) / {a[0]} ({a[1]})")
        print(f"Total number of graphQL calls: {GithubAPILog.totalGraphCalls}")
        print(f"Total number of graphQL results: {GithubAPILog.totalGraphResults}")
        a = LogFile.biggestBytesUnit(GithubAPILog.totalGraphBytes)
        print(f"Total number of graphQL bytes: {GithubAPILog.totalGraphBytes} (bytes) / {a[0]} ({a[1]})")
        print(f"All single-byte holes found across all files: {GithubAPILog.totalSingleBytesHoles}")
        print(f"All multi-byte holes found across all files: {GithubAPILog.totalMissedFrames}")
        