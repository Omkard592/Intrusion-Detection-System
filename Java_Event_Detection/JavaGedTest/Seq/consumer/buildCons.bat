set JAVA_HOME=E:\jdk1.2.2
set SENTINEL=..\..\..\..\Java_Event_Detection\classes
set JARS=..\..\..\..\Java_Event_Detection\jars


@echo off

if "%OS%" == "Windows_NT" setlocal

"%JAVA_HOME%\bin\javac" -g -classpath "%JARS%;%SENTINEL%"  Consumer.java

if "%OS%" == "Windows_NT" endlocal

echo Done Compiling !!

pause
:end
