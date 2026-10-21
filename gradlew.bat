@echo off
setlocal
set "APP_HOME=%~dp0"
set "GRADLE_WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
if not exist "%GRADLE_WRAPPER_JAR%" (
  echo Error: Gradle wrapper JAR is missing: "%GRADLE_WRAPPER_JAR%" 1>&2
  exit /b 1
)
if defined JAVA_HOME (
  set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVACMD=java.exe"
)
"%JAVACMD%" -classpath "%GRADLE_WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
