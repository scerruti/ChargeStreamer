import subprocess
import sys
import os

def clear_screen():
    """Clears the terminal screen for better UI experience."""
    os.system('cls' if os.name == 'nt' else 'clear')

def adb_command(keyevent):
    """Sends ADB keyevent commands to the emulator."""
    try:
        # Run the ADB command
        subprocess.run(f"adb shell input keyevent {keyevent}", shell=True, check=True)
        print(f"Sent keyevent: {keyevent}")
    except subprocess.CalledProcessError as e:
        print("Error: Failed to send ADB command.")
        print(f"Details: {e}")

def display_menu():
    """Displays the interactive menu."""
    print("\n--- Media Control Menu ---")
    print("[1] Play")
    print("[2] Pause")
    print("[3] Next Track")
    print("[4] Previous Track")
    print("[5] Fast Forward")
    print("[6] Rewind")
    print("[0] Exit")
    print("--------------------------")

def handle_choice(choice):
    """Processes user's menu choice."""
    keyevents = {
        "1": "KEYCODE_MEDIA_PLAY",
        "2": "KEYCODE_MEDIA_PAUSE",
        "3": "KEYCODE_MEDIA_NEXT",
        "4": "KEYCODE_MEDIA_PREVIOUS",
        "5": "KEYCODE_MEDIA_FAST_FORWARD",
        "6": "KEYCODE_MEDIA_REWIND"
    }

    if choice in keyevents:
        adb_command(keyevents[choice])
    elif choice == "0":
        print("Exiting...")
        sys.exit(0)
    else:
        print("Invalid choice. Please select again.")

def main():
    """Main function to run the application."""
    while True:
        clear_screen()
        display_menu()
        choice = input("Select an option: ").strip()
        handle_choice(choice)

if __name__ == "__main__":
    main()
