# This DB combiner reads in the analyses.sql file and changes the primary key to the next available ID.

import re


id = 772049

def ignoreFileId(id):
    if id < 672925:
        return True
    return False

def handleLine(line : str):
    global id
    outputString = "INSERT INTO `analyses` VALUES "
    if not line.startswith("INSERT"):
        return line # Not a data line

    length = len(outputString)
    if line[0:length] != outputString:
        print("Mismatch in insert beginning")
        exit(-1) 

    skip = True
    for i in re.finditer(r"\([^)]*\)", line):
        currentString = ""
        if skip:
            skip = False
        else:
            currentString += ","  # Add a comma before match

        match = re.match(r"\(([^,]*),([^,]*),([^,]*),(.*)\)", i.group(0))

        currentString += f"({id},{match.group(2)},{match.group(3)},{match.group(4)})"
        if not ignoreFileId(int(match.group(3))):
            outputString += currentString
            id += 1
    return outputString + ";\n"
        
    

def processFile(fileName):
    with open(fileName, "r") as inFile, open(f"modified-{fileName}", "w") as outFile:
        for line in inFile.readlines():
            outFile.write(handleLine(line))

processFile("php_analysis_fix.sql")