REM This file removes class files from led, ged, and comm dirs
@echo off

echo start cleaning!
del .\..\..\Java_Event_Detection\classes\sentinel\led\*.class
del .\..\..\Java_Event_Detection\classes\sentinel\ged\*.class
del .\..\..\Java_Event_Detection\classes\sentinel\comm\*.class
echo Done Cleaning !!

pause
