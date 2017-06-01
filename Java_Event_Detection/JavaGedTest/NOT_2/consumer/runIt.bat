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




if "Windows_NT" == "%OS%" setlocal

echo Running the OR test program...
java  -Djava.security.policy=server.policy    -classpath .;"%SENTINEL%;%JARS%" Consumer


pause
goto end
