@echo off

set SENTINEL=..\..\..\Java_Event_Detection\classes


echo Compling IDS_Project program... 

javac -classpath ".;%SENTINEL%"  IDS_Project.java


echo Compiling completed
pause
