set SENTINEL=..\..\..\..\Java_Event_Detection\classes
set JARS=..\..\..\..\Java_Event_Detection\jars


@echo off

javac -g -classpath "%JMQ_CPATH%;%SENTINEL%;%JARS%"  Producer.java

if "%OS%" == "Windows_NT" endlocal

echo Done Compiling !!

pause

