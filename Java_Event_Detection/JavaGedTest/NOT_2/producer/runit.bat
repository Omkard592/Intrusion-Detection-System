@echo off

REM ----------------------------
REM setting environment variable
REM ----------------------------
set CLASSPATH=..\..\..\..\Java_Event_Detection\classes;%CLASSPATH%;

REM ---------------------------------
REM generating the application config
REM ---------------------------------

echo generating App.config
javac GenerateAppConfig.java
java GenerateAppConfig

set SENTINEL=..\..\..\..\Java_Event_Detection\classes
set JARS=..\..\..\..\Java_Event_Detection\jars


@echo off
echo Running the Producer program...
java  -Djava.security.policy=server.policy      -classpath .;"%SENTINEL%;%JARS%" Producer

pause
