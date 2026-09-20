Tiger Call — সহজ APK Build

এই ZIP-টি GitHub-এ আপলোড করলে GitHub Actions নিজে APK বানাবে।

সহজ ধাপ:
1. GitHub-এ নতুন Repository তৈরি করুন (Public বা Private)।
2. এই ZIP Extract করে সব ফাইল Repository-তে Upload করুন।
3. Actions ট্যাবে যান।
4. “Build Tiger Call APK” workflow চলবে।
5. Build শেষ হলে Actions → সর্বশেষ run → Artifacts থেকে “TigerCall-debug-apk” ডাউনলোড করুন।
6. ZIP খুলে app-debug.apk ফোনে Install করুন।

নোট:
- এই Starter version-এ Firebase Phone OTP login + Realtime Database user status আছে।
- WebRTC audio/video calling এখনো যোগ করা হয়নি; এটি পরের ধাপ।
