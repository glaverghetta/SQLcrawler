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
    normalPrint("[ANALYZE] - ", end="")
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


def mvn_package():
    print("First... rebuild!")
    mvn = subprocess.run("mvn package", shell=True, stderr=subprocess.STDOUT, encoding='utf-8')
    if mvn.returncode != 0:
        print("Failed to build!")
        exit(-1)


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


def analyze():
    command = ["java", "-Xss8M", "-jar", "target/sqlcrawler-1.0-jar-with-dependencies.jar", "analyze", "new"]
    print(f"Starting new Analysis run")
    return runCrawler(command)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Please provide a tag/name for the subject line as the first argument!")
        exit(-1)
        
    mvn_package()
    runWithEmailRestore(sys.argv[1], analyze)
    
