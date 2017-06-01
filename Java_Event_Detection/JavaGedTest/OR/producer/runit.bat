@echo off
REM running a producer program for testing OR operator 

set CLASSPATH=..\..\..\..\Java_Event_Detection\classes;%CLASSPATH%;

REM ---------------------------------
REM generating the application config
REM ---------------------------------

echo generating App.config
javac GenerateAppConfig.java
java GenerateAppConfig


REM ------------------------------
REM setting the sentinel package
REM ------------------------------


set SENTINEL=..\..\..\..\Java_Event_Detection\classes


REM ---------------------------------
REM setting the additional libraries
REM ---------------------------------

set JARS=..\..\..\..\Java_Event_Detection\jars


REM ------------------------------------
REM start running the program
REM ------------------------------------


echo Running the AND test program (Producer)...

java  -Djava.security.policy=server.policy   -classpath .;"%JMQ_CPATH%;%SENTINEL%;%JARS%" Producer


pause
