from collections import namedtuple
import crawlerLogAnalyzer.TimedLogFile as TimedLogFile
import crawlerLogAnalyzer.LogFile as LogFile

class GithubAPILog(TimedLogFile.TimedLogFile):

    QueryString = namedtuple("QueryString", ["query", "language", "frame"])
    globalSearchCalls = 0
    globalGraphCalls = 0
    globalSearchResults = 0
    globalSearchBytes = 0
    globalGraphResults = 0
    globalGraphBytes = 0
    globalFrames = {}
    globalSingleBytesHoles = []
    globalMissedFrames = []
    globalLowPerformingPages = {}
    numAPIFiles = 0

    def __init__(self, filename, ignoreFrames=False, lowLevel=50):
        """fileName (string): The filename to open
           ignoreFrames (Bool): Whether frames should be analyzed
           lowLevel (int): A page is considered low "performing" when they had less than lowLevel results
        """
        self.searchCalls = 0
        self.graphCalls = 0
        self.searchResults = 0
        self.searchBytes = 0
        self.graphResults = 0
        self.graphBytes = 0
        self.ignoreFrames = False
        self.lowPerformingPages = {}
        self.lowLevel = lowLevel
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
            self.searchResults += self.valsNumResults(vals)
            self.searchBytes += self.valsResponseSize(vals)
            GithubAPILog.globalSearchCalls += 1
            GithubAPILog.globalSearchResults += self.valsNumResults(vals)
            GithubAPILog.globalSearchBytes += self.valsResponseSize(vals)

            self.query, self.language, frame = self.valsQueryString(vals)
            if not self.ignoreFrames:
                if len(frame) == 1:
                    # This is from an error in old version, mark as a hole to patch
                    start = frame[0]
                    end = frame[0]
                    self.singleByteHoles.append(start)
                    GithubAPILog.globalSingleBytesHoles.append(start)
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
                        GithubAPILog.globalMissedFrames.append(f"{self.lastSize+1}..{start-1}")
                self.lastSize = end
                
                page = self.valsPage(vals)
                range = str(start) + "-" + str(end)
                if range in self.frames:
                    if page not in self.frames[range]:
                        self.frames[range].append(page)
                        GithubAPILog.globalFrames[range].append(page)
                else:
                    self.frames[range] = []
                    GithubAPILog.globalFrames[range] = []
                    self.frames[range].append(page)
                    GithubAPILog.globalFrames[range].append(page)
                
                if self.valsNumResults(vals) < self.lowLevel:
                    if range in self.lowPerformingPages:
                        if page not in self.lowPerformingPages[range]:
                            self.lowPerformingPages[range].append(page)
                            GithubAPILog.globalLowPerformingPages[range].append(page)
                    else:
                        self.lowPerformingPages[range] = []
                        GithubAPILog.globalLowPerformingPages[range] = []
                        self.lowPerformingPages[range].append(page)
                        GithubAPILog.globalLowPerformingPages[range].append(page)

        else:
            self.graphCalls += 1
            self.graphResults += self.valsNumResults(vals)
            self.graphBytes += self.valsResponseSize(vals)

            GithubAPILog.globalGraphCalls += 1
            GithubAPILog.globalGraphResults += self.valsNumResults(vals)
            GithubAPILog.globalGraphBytes += self.valsResponseSize(vals)
    
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
        print("-#-Github API call information-#-")
        print(f"Total number of search calls: {self.searchCalls}")
        print(f"Total number of search results: {self.searchResults}")
        a = LogFile.biggestBytesUnit(self.searchBytes)
        print(f"Total number of search bytes: {self.searchBytes} (bytes) / {a[0]} ({a[1]})")
        print(f"Total number of graphQL calls: {self.graphCalls}")
        print(f"Total number of graphQL results: {self.graphResults}")
        a = LogFile.biggestBytesUnit(self.graphBytes)
        print(f"Total number of graphQL bytes: {self.graphBytes} (bytes) / {a[0]} ({a[1]})")
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
            print(f"Pages flagged as low-performing: {self.lowPerformingPages}")
    
    def print():
        print(f"Total number of GithubAPI files analyzed: {GithubAPILog.numAPIFiles}")
        print(f"Total number of search calls: {GithubAPILog.globalSearchCalls}")
        print(f"Total number of search results: {GithubAPILog.globalSearchResults}")
        a = LogFile.biggestBytesUnit(GithubAPILog.globalSearchBytes)
        print(f"Total number of search bytes: {GithubAPILog.globalSearchBytes} (bytes) / {a[0]} ({a[1]})")
        print(f"Total number of graphQL calls: {GithubAPILog.globalGraphCalls}")
        print(f"Total number of graphQL results: {GithubAPILog.globalGraphResults}")
        a = LogFile.biggestBytesUnit(GithubAPILog.globalGraphBytes)
        print(f"Total number of graphQL bytes: {GithubAPILog.globalGraphBytes} (bytes) / {a[0]} ({a[1]})")
        print(f"All single-byte holes found across all files: {GithubAPILog.globalSingleBytesHoles}")
        print(f"All multi-byte holes found across all files: {GithubAPILog.globalMissedFrames}")
        print(f"Pages flagged as low-performing: {GithubAPILog.globalLowPerformingPages}")
        