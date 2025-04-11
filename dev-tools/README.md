## **Developer Tools**

### **ADB Media Controls**
The `adb_media_controls.py` script is an interactive terminal application designed to help developers test media playback controls for Android Automotive applications. It allows you to send ADB (Android Debug Bridge) key events to an emulator or connected device to simulate playback actions like play, pause, skip, and rewind.

### **Features**
- Interactive menu-driven interface for a better terminal experience.
- Supports common media key events such as Play, Pause, Next, Previous, Fast Forward, and Rewind.
- Useful for testing **MediaSessionCompat** callbacks without requiring actual hardware or steering wheel controls.

### **Prerequisites**
- Android Studio with an Android Automotive emulator set up, or a connected Android device.
- ADB installed and added to your system's PATH.
- Python 3.x installed on your development machine.

### **Setup**
1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/your-repo/ChargeStreamer.git
   cd ChargeStreamer
   ```
2. Navigate to the `dev-tools/` directory:
   ```bash
   cd dev-tools
   ```
3. Verify your emulator or connected device is recognized by ADB:
   ```bash
   adb devices
   ```

### **Usage**
To run the script, use the following command:
```bash
python adb_media_controls.py
```

Follow the on-screen menu to send media key events to the emulator or connected device. The available options include:
- `[1] Play`
- `[2] Pause`
- `[3] Next Track`
- `[4] Previous Track`
- `[5] Fast Forward`
- `[6] Rewind`
- `[0] Exit`

### **Example Log Output**
If the script successfully sends a key event, you should see a message like this in your terminal:
```
Sent keyevent: KEYCODE_MEDIA_PLAY
```

You can verify that your app responds to these events by checking the logs in Android Studio's **Logcat**.

### **Troubleshooting**
- **ADB command not found**: Ensure ADB is installed and added to your PATH.
- **Device not listed**: Ensure your emulator is running or your device is connected and recognized by `adb devices`.

### **Contributions**
Feel free to contribute by enhancing the script’s functionality or documentation. Open a pull request or submit an issue for any bugs or feature requests.
