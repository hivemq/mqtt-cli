@ECHO off
setlocal

REM Add the launcher flags only on the Java versions that recognize them, so mqtt-cli still starts on its
REM Java 8 and 11 baseline. The flags are passed via JAVA_TOOL_OPTIONS so the raw @@exeName@@ keeps working
REM when invoked directly. The major version is parsed from "java -version".
REM see https://netty.io/wiki/java-24-and-sun.misc.unsafe.html
set "JAVA_EXE=java"
if not "%JAVA_HOME%"=="" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

set "JAVA_MAJOR_VERSION=0"
for /f tokens^=2-2^ delims^=^" %%j in ('"%JAVA_EXE%" -version 2^>^&1') do set "JAVA_VERSION=%%j"
for /f "tokens=1 delims=. " %%a in ("%JAVA_VERSION%") do set "JAVA_MAJOR_VERSION=%%a"

if %JAVA_MAJOR_VERSION% GEQ 17 set "JAVA_TOOL_OPTIONS=%JAVA_TOOL_OPTIONS% --enable-native-access=ALL-UNNAMED"
if %JAVA_MAJOR_VERSION% GEQ 24 set "JAVA_TOOL_OPTIONS=%JAVA_TOOL_OPTIONS% --sun-misc-unsafe-memory-access=allow"

"%~dp0@@exeName@@" %*
