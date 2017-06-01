@echo off
REM Running Makefits (aircraft track)

REM ------------------------
REM setting the environment 
REM ------------------------

set CLASSPATH=..\..\Java_Event_Detection\classes;%CLASSPATH%;


REM ---------------------------------
REM generating the application config
REM ---------------------------------

echo generating App.config
javac GenerateAppConfig.java
java GenerateAppConfig

REM ------------------------------
REM setting the sentinel package
REM ------------------------------

REM set SENTINEL=..\..\..\Java_Event_Detection\classes
set SENTINEL=..\..\..\..\..\Java_Event_Detection\classes

REM ---------------------------------
REM setting the additional libraries
REM ---------------------------------

REM set JARS=..\..\..\spawar\jar\fesi.jar;..\..\..\spawar\jar\jbcl.jar
set JARS=..\..\..\..\..\Java_Event_Detection\jars\fesi.jar;..\..\..\..\..\Java_Event_Detection\jars\jbcl.jar

REM ---------------------------------
REM setting the classes' location
REM ---------------------------------

set MAKEFITSCLASSES=..\classes

REM ------------------------------------
REM start running the program
REM ------------------------------------

echo Beginning MAKEFITS program... (Air)


java  -Djava.security.policy=server.policy -classpath ".;%MAKEFITSCLASSES%;%JARS%;%SENTINEL%" MAKEFITS/TrackMonitor -getLines MAKEFITS/Tst05a.G-input.txt


pause
