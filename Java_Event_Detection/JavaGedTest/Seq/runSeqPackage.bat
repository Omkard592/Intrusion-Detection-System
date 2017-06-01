@echo off
REM running the AND package


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
cd ..\..\Java_Event_Detection\JavaGedTest\AND\producer
start "Producer program ..."  runit


REM -------------------------------
REM  start consumer
REM -------------------------------
echo start consumer !
cd ..\consumer
start "Consumer program ..."  runit


REM -------------------------------
REM  start conprod
REM -------------------------------
echo start conprod !
cd ..\conprod
start "ConProd program ..."  runit


