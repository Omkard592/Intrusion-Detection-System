set SENTINEL=..\..\Java_Event_Detection\classes
set JARS=..\..\Java_Event_Detection\jars


@echo off


javac -g -classpath "%SENTINEL%;%JARS%\fesi.jar" -d "%SENTINEL%" sentinel/ged/*.java

rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.GEDMesgRecvImp
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.GlobalEventFactoryImp
rmic -d "%SENTINEL%" -classpath "%SENTINEL%;%JARS%\fesi.jar" sentinel.ged.ServerConnectorImp


echo Done Compiling sentinel.ged !!

pause