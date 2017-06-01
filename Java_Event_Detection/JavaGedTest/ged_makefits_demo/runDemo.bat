@echo off


REM -------------------------------
REM  start GED server
REM -------------------------------
cd ..\..\..\Java_Event_Detection\source
start "GED server program ..."   startGED

echo after GED server started, click any key to continue running the clients
pause


REM -------------------------------
REM  start control center
REM -------------------------------
echo start control center !
cd ..\..\Java_Event_Detection\JavaGedTest\ged_makefits_demo\ControlCenter\source
start "Control Center program ..."  runit


REM -------------------------------
REM  start Makefits (ship)
REM -------------------------------
echo start makefits (ship track) center !
cd ..\..\Makefits_Navy\source
start "Makefits (navy) program ..."  runit


REM -------------------------------
REM  start Makefits (aircraft)
REM -------------------------------
echo start makefits (air track) center !
cd ..\..\Makefits_AirForce\source
start "Makefits (air force) program ..."  runit
