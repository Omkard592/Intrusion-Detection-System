set SENTINEL=..\..\Java_Event_Detection\classes
set JARS=..\..\Java_Event_Detection\jars


@echo off
if "%OS%" == "Windows_NT" setlocal

javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" sentinel/comm/*.java


if "%OS%" == "Windows_NT" endlocal

echo Done Compiling sentinel.comm !!

pause
