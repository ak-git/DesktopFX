```shell
cd C:\Users\ak\Documents\DesktopFX

gradlew clean build -x test

gradlew installBootDist

cd C:\Users\ak\Documents\DesktopFX\Desktop\build\install\Desktop-boot\lib

"C:\Program Files\Java\jdk-27\bin\jpackage" --main-jar Desktop.jar --input . --app-version 26.10.06 --name briko-caliper --vendor ak --win-dir-chooser --win-shortcut
```