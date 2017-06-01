@echo off


REM -------------------------------
REM  start GED server
REM -------------------------------
cd ..\..\source
start "GED server program ..."   startGED

echo after GED server started, click any key to continue running the clients
pause


REM -------------------------------
REM  start producer
REM -------------------------------
echo start producer !
cd ..\..\Java_Event_Detection\JavaGedTest\Not_1\producer
start "Producer program ..."  runit


REM -------------------------------
REM  start consumer
REM -------------------------------
echo start consumer !
cd ..\consumer
start "Consumer program ..."  runit


