REM This file compiles  led, GEd and the com packages

set SENTINEL=..\..\Java_Event_Detection\classes
set JARS=..\..\Java_Event_Detection\jars


@echo off
REM if "%OS%" == "Windows_NT" setlocal

echo Compiling sentinel.comm package
javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" sentinel/comm/*.java
echo Done compiling sentinel.comm package




echo Compiling sentinel.led package
javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" Sentinel/led/*.java
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.led.LEDMesgRecvImp
echo Dome compiling sentinel.led package

echo Compiling sentinel.ged package
javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" sentinel/ged/*.java
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.GEDMesgRecvImp
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.GlobalEventFactoryImp
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.ServerConnectorImp


echo Dome compiling sentinel.ged package

REM if "%OS%" == "Windows_NT" endlocal
pause
