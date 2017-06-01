@echo off

REM set SENTINEL=../../../spawar/jar/sentinel.jar
REM set SENTINEL=../../../Java_Event_Detection\classes
set SENTINEL=../../../../../Java_Event_Detection\classes

REM set JARS=../../../spawar/jar/fesi.jar;../../../spawar/jar/jbcl.jar
set JARS=../../../../../Java_Event_Detection/jars/fesi.jar;../../../../../Java_Event_Detection/jars/jbcl.jar
set MAKEFITSCLASSES=../classes


echo Compling control center program

javac -classpath ".;%JARS%;%SENTINEL%" -d "%MAKEFITSCLASSES%"  controlcenter/*.java


echo Compiling completed
pause
