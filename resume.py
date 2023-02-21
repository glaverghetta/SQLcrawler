import glob
import subprocess
import imaplib
import traceback
import yagmail
import keyring
import email
import time
import datetime
import sys

MYEMAIL = "acdcprovenance@gmail.com"

def sendEmail(subject, content):
    yagmail.SMTP('acdcprovenance').send(MYEMAIL, subject, content)

def check_for_reply_from_self(subject):
    try:
        mail = imaplib.IMAP4_SSL("imap.gmail.com" )
        mail.login(MYEMAIL, keyring.get_password("yagmail", MYEMAIL))
        mail.select('inbox', readonly=True)

        data = mail.search(None, 'FROM', MYEMAIL)
        mail_ids = data[1]
        id_list = mail_ids[0].split()   
        first_email_id = int(id_list[0])
        latest_email_id = int(id_list[-1])

        for i in range(latest_email_id,first_email_id, -1):
            data = mail.fetch(str(i), '(RFC822)' )
            for response_part in data:
                arr = response_part[0]
                if isinstance(arr, tuple):
                    msg = email.message_from_string(str(arr[1],'utf-8'))
                    email_subject = msg['subject']
                    email_from = msg['from']
                    if "Re:" in email_subject and subject in email_subject:
                        return True
        return False
    except Exception as e:
        traceback.print_exc() 
        print(str(e))

def findArguments(filename):
    with open(filename) as file:
        line = file.readline().rstrip()
        args = line.split("Arguments provided: ")[1].split(" ~ ")
    return args

def findDetails(filename):
    with open(filename) as file:
        while (line := file.readline().rstrip()):
            vals = line.split(" ~ ")
            if len(vals) < 7:
                print(f"Not enough values in {filename}")
                exit(-1)
            callType = vals[4]
            if callType != "search":
                continue
            page = vals[5]
            frameInfo = vals[6].split(" ")[-1].replace("size:", "").split("..")
            if(len(frameInfo) < 2):
                # Only a single byte frame
                frameStart = frameInfo[0]
                frameEnd = frameInfo[0]
            else:
                frameStart = frameInfo[0]
                frameEnd = frameInfo[1]
    return page, frameStart, frameEnd

def resume():
    filename = glob.glob("logs/Final*")[-1]
    print(f"Most recent Final file {filename}")
    args = findArguments(filename)

    if(args[0] != "optimize"):
        print(f"Can't resume with last command {args[0]}")
        exit(-1)

    filename = filename.replace("logs/Final", "logs/GithubAPI")  # Get the most recent GithubAPI log file
    filename = filename.replace("logs\Final", "logs\GithubAPI")  # This line is for Windows :)
    print(f"Most recent GithubAPI file {filename}")

    page, start, end = findDetails(filename)

    newArgs = ["java", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar"]
    ignore = False
    for i in args:
        if ignore:
            ignore = False
            continue
        if i == "--start" or i == "--end" or i == "--start-page":
            # Ignore the next operand as well
            ignore = True
            continue
        newArgs.append(i)

    newArgs.append("--start")
    newArgs.append(start)
    newArgs.append("--end")
    newArgs.append(end)
    newArgs.append("--start-page")
    newArgs.append(page)

    print(f"Restarting with frame {start}-{end}, page {page}")
    print(f"Running the following command: ", ' '.join(newArgs))

    lastLines = []
    with subprocess.Popen(' '.join(newArgs), shell=True, stdout=subprocess.PIPE, bufsize=1,
           universal_newlines=True, stderr=subprocess.STDOUT, encoding='utf-8') as p:
        for line in p.stdout:
            print(line, end='')
            lastLines.append(line)
            if len(lastLines) > 100:
                lastLines.pop(0)
    return lastLines

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Please provide a tag/name for the subject line as the first argument!")
        exit(-1)
    print(f"Using '{sys.argv[1]}' as part of the subject line")
    tag = sys.argv[1]
    print("First... rebuild!")
    mvn = subprocess.run("mvn package", shell=True, stderr=subprocess.STDOUT, encoding='utf-8')
    if mvn.returncode != 0:
        print("Failed to build!")
        exit(-1)

    while True:
        lastLines = resume()
        print("Crashed? Wait a minute, then email the boss")
        start = datetime.datetime.now()
        time.sleep(60)  # Wait a minute in case we crashed due to network instability
        fail = True
        subject = tag + " " + str(start)
        sendEmail(subject, ''.join(lastLines))
        print(f"Sent last 100 lines to myself with subject {start}")
        while((datetime.datetime.now() - start).days == 0):
            if(check_for_reply_from_self(subject)):
                fail = False
                break
            print(f"No email yet at {datetime.datetime.now()}. Check again in 5.")
            time.sleep(60*5) # Check email again in 5 minutes
        if fail:
            exit(-1)
    
        
        