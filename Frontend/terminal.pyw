import tkinter as tk
import threading
import subprocess
import re
from vosk import Model, KaldiRecognizer
import pyaudio
import pygetwindow as gw
from pywinauto.application import Application
import pyperclip
import pyautogui
import pythoncom,win32console,win32gui,win32api
import pyttsx3
import os

PID = 0
line = 0
ParentPID = os.getpid()


window = tk.Tk()
window.title("CMD GUI")
window.geometry("800x400")


def speak(text):
    engine = pyttsx3.init()
    voices = engine.getProperty('voices')
    engine.setProperty('voice', voices[1].id)
    engine.say(text)
    engine.runAndWait()

def exitSoftware():
    PID = ParentPID
    startProcedure("taskkill /F /T /PID " + str(PID))

def listen_for_command():
    #Get the Environment Variable
    model = Model(os.environ["VOSK_MODEL"])
    rec = KaldiRecognizer(model, 16000)
    
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paInt16, channels=1, rate=16000, input=True, frames_per_buffer=8000)
    stream.start_stream()
    
    while True:
        data = stream.read(4000, exception_on_overflow=False)
        if rec.AcceptWaveform(data):
            result = rec.Result()
            print(result)
            return result

def main():
    print("Listening for command...")
    try:
        command = listen_for_command()
    except Exception as e:
        print(f"Failed to listen for command: {str(e)}")
        main()

    if "close" in command or "exit" in command:
        speak("Exiting Software")
        exitSoftware()

    if "hide" in command:
        speak("Hiding window")
        window.withdraw()

    if "get" in command:
        speak("Showing window")
        window.deiconify()
    
    #Recall the main function
    main()

#Start the main function in a new thread
thread = threading.Thread(target=main)
thread.start()

def insertLine(string):
    global line
    output.insert(tk.END, str(line) + ": " + string + "\n")
    output.see(tk.END)
    line += 1
    
def clear():
    global line
    entry_var.set("")
    output.delete(1.0, tk.END)
    line = 0

def filter_special_format(text):
    print(text)
    pattern = r'\x1b\[\d+m'
    filtered_text = re.sub(pattern, '', text)
    return filtered_text
    
def startProcedure(command):
    thread = threading.Thread(target=runCommand, args=(command,))
    thread.start()

def runCommand(command):
    global PID
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.PIPE)
    PID = process.pid
    print("PID: ", PID)
    while True:
        output = process.stdout.readline().decode("utf-8")
        output = filter_special_format(output)

        if output == "" and process.poll() is not None:
            break
        if output:
            insertLine(output.strip())
    process.wait()

def crashCurrentThread():
    clear()
    if PID == 0:
        return
    startProcedure("taskkill /F /T /PID " + str(PID))

frame = tk.Frame(window)
frame.pack(fill=tk.BOTH, expand=True)

label = tk.Label(frame, text="Enter your command:", font=("Segoe UI", 12))
label.pack(anchor=tk.W, padx=6, pady=6)

entry_var = tk.StringVar()

entry = tk.Entry(frame, textvariable=entry_var, font=("Consolas", 12))
entry.pack(fill=tk.X, padx=10, pady=4)

output = tk.Text(frame, font=("Consolas", 12))
output.pack(fill=tk.BOTH, expand=True, padx=10, pady=6)

#Change default close button behavior
def on_closing():
    exitSoftware()

window.protocol("WM_DELETE_WINDOW", on_closing)

def onEnter(event):
    command = entry_var.get()
    insertLine(command)
    if command == "cls":
        clear()
    elif command == "crash":
        crashCurrentThread()
    else:
        crashCurrentThread()
        startProcedure(command)

entry.bind("<Return>", onEnter)

entry.focus_set()
window.mainloop()
