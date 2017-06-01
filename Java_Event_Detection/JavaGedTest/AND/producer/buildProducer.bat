set SENTINEL=..\..\..\..\Java_Event_Detection\classes
set JARS=..\..\..\..\Java_Event_Detection\jars


@echo off


javac -g -classpath "%SENTINEL%;%JARS%"  Producer.java



echo Done Compiling !!

pause

