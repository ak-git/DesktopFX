```shell
cd C:\Users\ak\Documents\DesktopFX

gradlew build -x test

gradlew installBootDist

cd C:\Users\ak\Documents\DesktopFX\Desktop\build\install\Desktop-boot\lib

"C:\Program Files\Java\jdk-25\bin\jpackage" --main-jar Desktop.jar --input . --app-version 26.7.22 --name rcms-ecg --vendor ak --win-dir-chooser --win-shortcut
```