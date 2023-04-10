import json
import subprocess
import glob
from crawlerLogAnalyzer import GithubAPILog
import imaplib
import traceback
import yagmail
import keyring
import email
import time
import datetime
import sys

MYEMAIL = "acdcprovenance@gmail.com"

# Override the print calls in this file with a special version :)
normalPrint = print
def print(*args):
    normalPrint("[PATCHER] - ", end="")
    normalPrint(*args)

def sendEmail(subject, content):
    yagmail.SMTP('acdcprovenance').send(MYEMAIL, subject, content)


def check_for_reply_from_self(subject):
    try:
        mail = imaplib.IMAP4_SSL("imap.gmail.com")
        mail.login(MYEMAIL, keyring.get_password("yagmail", MYEMAIL))
        mail.select('inbox', readonly=True)

        data = mail.search(None, 'FROM', MYEMAIL)
        mail_ids = data[1]
        id_list = mail_ids[0].split()
        first_email_id = int(id_list[0])
        latest_email_id = int(id_list[-1])

        for i in range(latest_email_id, first_email_id, -1):
            data = mail.fetch(str(i), '(RFC822)')
            for response_part in data:
                arr = response_part[0]
                if isinstance(arr, tuple):
                    msg = email.message_from_string(str(arr[1], 'utf-8'))
                    email_subject = msg['subject']
                    email_from = msg['from']
                    if "Re:" in email_subject and subject in email_subject:
                        return True
        return False
    except Exception as e:
        traceback.print_exc()
        print(str(e))

firstRun = True
def nextFile():
    global firstRun
    files = sorted(glob.glob("logs/GithubAPI*.log"))

    if firstRun:
        try:
            with open("resume.txt", "r") as f:
                data = json.load(f)
                return data["file"], data["completed"]
        except IOError:
            # No resume, ignore
            pass
        firstRun = False

    try:
        with open("ignore.txt", "r") as file:
                finishedFiles = file.read().splitlines()
    except IOError:
        finishedFiles = []

    for f in files:
        if f not in finishedFiles:
            return f, None
    return None, None

def writeProgress(gh):
    if not hasattr(gh, 'scannedMissedFrames'):
        gh.scannedMissedFrames = []
    if not hasattr(gh, 'scannedSingleByteHoles'):
        gh.scannedSingleByteHoles = []
    if not hasattr(gh, 'scannedLowPerformingPages'):
        gh.scannedLowPerformingPages = [] 
    output = {
        "file": gh.fileName,
        "completed": {
            "scannedMissedFrames": gh.scannedMissedFrames,
            "scannedSingleByteHoles": gh.scannedSingleByteHoles,
            "scannedLowPerformingPages": gh.scannedLowPerformingPages
        }
    }

    with open("resume.txt", "w") as f:
        json.dump(output, f)

def loadProgress(gh, progress):
    if progress is None:
        return
    gh.scannedMissedFrames = progress["scannedMissedFrames"]
    gh.scannedSingleByteHoles = progress["scannedSingleByteHoles"]
    gh.scannedLowPerformingPages = progress["scannedLowPerformingPages"]


def runCrawler(args):
    lastLines = []
    with subprocess.Popen(' '.join(args), shell=True, stdout=subprocess.PIPE, bufsize=1,
                          universal_newlines=True, stderr=subprocess.STDOUT, encoding='utf-8') as p:
        for line in p.stdout:
            normalPrint(line, end='')
            lastLines.append(line)
            if len(lastLines) > 100:
                lastLines.pop(0)
    return (p.returncode, lastLines)


def runMissedFrames(gh:GithubAPILog.GithubAPILog):
    if not hasattr(gh, 'scannedMissedFrames'):
        gh.scannedMissedFrames = []  # Create list to save already scanned frames
    results = (0, "")  # Used in the odd scenario where all frames have already been scanned
    for frame in gh.missedFrames:
        if frame in gh.scannedMissedFrames:
            # We already did this frame in a past run
            continue
        s = frame.split("..")
        start = s[0]
        end = s[1]
        command = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar", "optimize",
                    gh.language, f"{int(end)+1}", "--start", start, "--end", end]
        print(f"Starting new run for file {gh.fileName} with {start}-{end}")
        results = runCrawler(command)
        if results[0] == 0:
            gh.scannedMissedFrames.append(frame)  # Completed, keep going
            writeProgress(gh)
            continue
        else:
            return results  # Failed, return results
    return results


def runHoles(gh:GithubAPILog.GithubAPILog):
    if not hasattr(gh, 'scannedSingleByteHoles'):
        gh.scannedSingleByteHoles = []  # Create list to save already scanned holes
    results = (0, "")  # Used in the odd scenario where all holes have already been scanned
    for i in gh.singleByteHoles:
        if i in gh.scannedSingleByteHoles:
            # We already did this hole in a past run
            continue
        command = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar", "optimize",
                   gh.language, f"{i+1}", "--start", f"{i}", "--end", f"{i}"]
        print(f"Starting new run for file {gh.fileName} with {i}-{i}")
        results = runCrawler(command)
        if results[0] == 0:
            gh.scannedSingleByteHoles.append(i)  # Completed, keep going
            writeProgress(gh)
            continue
        else:
            return results  # Failed, return results
    return results

def runLowPerformingPages(gh:GithubAPILog.GithubAPILog):
    if not hasattr(gh, 'scannedLowPerformingPages'):
        gh.scannedLowPerformingPages = []  # Create list to save already scanned holes
    results = (0, "")  # Used in the odd scenario where all holes have already been scanned
    for frame, pages in gh.lowPerformingPages.items():
        start = int(frame.split("-")[0])
        end = int(frame.split("-")[1])
        for page in pages:
            page = int(page)  # Script will also rerun all pages after the first one
            if (frame + f":{page}") in gh.scannedLowPerformingPages:
                # We already did these pages
                continue
            command = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar", "optimize",
                    gh.language, f"{end+1}", "--start", f"{start}", "--end", f"{end}", "--start-page", f"{page}", "--onePage"]
            print(f"Starting new run for file {gh.fileName} with {start}-{end}, page {page}")
            results = runCrawler(command)
            if results[0] == 0:
                gh.scannedLowPerformingPages.append(frame + f":{page}")  # Completed, keep going
                writeProgress(gh)
                continue
            else:
                return results  # Failed, return results
    return results


def mvn_package():
    print("First... rebuild!")
    mvn = subprocess.run("mvn package", shell=True, stderr=subprocess.STDOUT, encoding='utf-8')
    if mvn.returncode != 0:
        print("Failed to build!")
        exit(-1)


def patchHoles(tag, types):
    stopPoint = sorted(glob.glob("logs/GithubAPI*.log"))[-1]
    filename, progress = nextFile()
    while filename is not None:
        gh = GithubAPILog.GithubAPILog(filename)
        gh.analyze()
        loadProgress(gh, progress)

        if "missed" in types:
            print(f"Patching missing frames in {gh.fileName}")
            runWithEmailRestore(tag, runMissedFrames, gh)
            print(f"Finished patching missing frames in {gh.fileName}")
        if "single" in types:
            print(f"Patching single-byte holes in {gh.fileName}")
            runWithEmailRestore(tag, runHoles, gh)
            print(f"Finished patching single-byte holes in {gh.fileName}")
        if "low" in types:
            print(f"Rerunning low performing pages in {gh.fileName}")
            runWithEmailRestore(tag, runLowPerformingPages, gh)
            print(f"Finished rerunning low performing pages in {gh.fileName}")

        with open("ignore.txt", "a") as f:
            print(f"Added {gh.fileName} to ignore list")
            f.write(filename + "\n")
        if filename == stopPoint:
            break  # Did the last file, remaining ones are new from running patcher scripts
        filename, progress = nextFile()

def runWithEmailRestore(tag, func, *args):
    while True:
        (exitCode, lastLines) = func(*args)
        if exitCode == 0:
            return
        print("Crashed? Wait a minute, then email the boss")
        start = datetime.datetime.now()
        time.sleep(60)  # Wait a minute in case we crashed due to network instability
        fail = True
        subject = tag + " " + str(start)
        sendEmail(subject, ''.join(lastLines))
        print(f"Sent last 100 lines to myself with subject {start}")
        time.sleep(120)  # We JUST sent the email, so wait two minutes before first check
        while((datetime.datetime.now() - start).days == 0):
            if(check_for_reply_from_self(subject)):
                fail = False
                break
            print(f"No email yet at {datetime.datetime.now()}. Check again in 5.")
            time.sleep(60*5)  # Check email again in 5 minutes
        if fail:
            exit(-1)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Please provide a tag/name for the subject line as the first argument!")
        exit(-1)
    
    if len(sys.argv) < 3:
        print("Please provide at least one type of patch to run (single, missed, low)")
        print("Example: python3 patcher.py tag low missed")
        exit(-1)

    print(f"Using '{sys.argv[1]}' as part of the subject line")
    tag = sys.argv[1]

    types = []
    for i in sys.argv[2:]:
        if i not in ["single", "missed", "low"]:
            print(f"Unknown patch type: {i}")
            exit(-1)
        else:
            types.append(i)

    mvn_package()

    print("Running the following patch types: ", *types)
    patchHoles(tag, types)
    
