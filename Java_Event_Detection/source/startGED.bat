@echo off
REM this starts the GED server. Uses Global.config and server.policy in this folder

REM ---------------------------------------------------
REM setting the environment and start the rmiregistry
REM ---------------------------------------------------

set CLASSPATH=..\..\Java_Event_Detection\classes;%CLASSPATH%;
start rmiregistry

REM ---------------------------------
REM generating global.config file
REM ---------------------------------

echo generating Global.config
javac GenerateGlobalConfig.java
java GenerateGlobalConfig


REM ------------------------------
REM setting the sentinel package
REM ------------------------------

set SENTINEL=..\..\Java_Event_Detection\classes


REM ---------------------------------
REM setting the additional libraries
REM ---------------------------------

set JARS=..\..\Java_Event_Detection\jars


REM ------------------------------------
REM start running the program
REM ------------------------------------

echo Starting the GED server...

java -Djava.security.policy=server.policy -classpath "%JARS%;%SENTINEL%" sentinel/ged/GECAAgent
pause





