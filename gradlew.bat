@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set GRADLE_WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
if not exist "%GRADLE_WRAPPER_JAR%" (
    echo Downloading Gradle Wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile '%GRADLE_WRAPPER_JAR%'"
)
java -jar "%GRADLE_WRAPPER_JAR%" %*
