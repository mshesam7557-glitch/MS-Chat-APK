# MS__Chat Android

این مخزن فقط برای ساخت APK اندروید است و نباید به مخزن وب Render اضافه شود.

## ساخت APK بدون Android Studio

1. یک GitHub Repository جدا برای Android بساز.
2. **تمام محتویات همین پوشه** را در ریشه Repository قرار بده. باید این ساختار را داشته باشی:

```text
.github/workflows/build-apk.yml
app/
gradle/
gradlew

gradlew.bat
build.gradle
gradle.properties
settings.gradle
README.md
```

3. در GitHub وارد تب **Actions** شو.
4. Workflow با نام **Build MS__Chat APK** را باز کن.
5. روی **Run workflow** بزن.
6. بعد از سبز شدن Build، پایین صفحه بخش **Artifacts** را باز کن.
7. فایل **MS-Chat-APK** را دانلود و از حالت ZIP خارج کن.
8. فایل نهایی **app-debug.apk** است و می‌توانی آن را مستقیم برای دیگران بفرستی.

`.env` لازم نیست و نباید در این مخزن قرار بگیرد.

## نکته
این پروژه برای ساخت APK از همان سایت MS__Chat استفاده می‌کند؛ آدرس فعلی سایت داخل `MainActivity.java` تنظیم شده است.

برای انتشار وب، فقط مخزن وب Render را با فایل‌های وب خودش نگه دار. فایل‌ها و پوشه‌های Android این مخزن برای Render لازم نیستند.
