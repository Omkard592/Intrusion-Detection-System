@echo off

set SENTINEL=..\..\..\Java_Event_Detection\classes


echo Beginning IDS program... 

java -classpath ".;%SENTINEL%" IDS_Project


pause
