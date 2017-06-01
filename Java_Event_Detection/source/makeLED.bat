REM this file compiles the led package

set SENTINEL=..\..\Java_Event_Detection\classes
set JARS=..\..\Java_Event_Detection\jars

@echo off

javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" Sentinel/led/*.java
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.led.LEDMesgRecvImp

if "%OS%" == "Windows_NT" endlocal

echo Done Compiling sentienl.led !!
pause


