@echo off
REM Running control conter

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

set JARS=..\..\..\..\..\Java_Event_Detection\jars\fesi.jar;..\..\..\..\..\Java_Event_Detection/jars/jbcl.jar


REM ---------------------------------
REM setting the classes' location
REM ---------------------------------
set MAKEFITSCLASSES=../classes



REM ------------------------------------
REM start running the program
REM ------------------------------------
echo Beginning control center program

java  -Djava.security.policy=server.policy -classpath ".;%MAKEFITSCLASSES%;%JARS%;%SENTINEL%" controlcenter/Controller

pause
