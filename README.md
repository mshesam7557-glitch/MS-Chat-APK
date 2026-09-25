# MS__Chat Android v3

This Android build intentionally does **not** inject or alter the web site's mobile UI.
It loads the live MS__Chat web app directly so the phone web layout is used as-is.

- App label: MS__Chat
- Application id: com.mschat.app
- Target URL: https://ms-chat-307k.onrender.com/
- JavaScript + DOM storage enabled
- Microphone/camera permission bridge for WebRTC
- File picker bridge
- Explicit WebView focus/touch handling
- No Android-side mobile CSS/DOM override

Build from GitHub Actions or with `gradlew.bat assembleDebug`.
