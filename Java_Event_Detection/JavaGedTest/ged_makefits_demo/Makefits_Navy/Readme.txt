This file contains the rule definitiosn for the Makefits demo.

- The map is confined to the region of Latitudes 10S and 40N and Longitudes -140W to -100W.

- Implemented rule 4,5,6,11 described as follows.

  Rule 4 :: Show track-X status every Y minutes

  Rule 5 :: When there is no change of track-Z's location or altitude (or other track attributes) reported for X minute,  notify the analyst (e.g. print a message "No change in location of track-Z for X minutes"

  Rule 6 :: When there is no update on Track-X's location & altitude  between T1 and T2, send message to request Track-X status.

  Rule 7 :: When track(s) enters or leave the defined boundary, generate an alert (boundaries:  fixed areas WRT a given latitude/longitude or a specific track)

  Rule 11:: Notify analyst of Track-Y 2 minutes after it enters the Area of Interest

- The infomation displayed on the "Alert Notification" window and "OTH_Gold" window is captured into the file \\MakefitsV9\makefits\Makefits\Alert_Output.txt and \\MakefitsV9\makefits\Makefits\OTH_Glod_Output.txt repectively


- Rule 4 is applied to Track T7005.
- Rule 5,6,7,11 are applied to Track T7001,T7002

- Input data \\MakefitsV9\makefits\Makefits\Tst05a.G-input.txt




To setup
--------

Basic steps for initial set up before running the demo

1) To update the setup.txt, please update in the after the word "Begin"
2) Please set up the year that you run the program. YEAR = 2000 or YEAR = 2001
3) To set up the time interval for showing track status, 
   set SHOW_TRACK_EVERY_MINS = 0 hrs 0 min 30 sec (for rule 4)
4) To set up the time for rule 5, set  NO_CHANGE_IN_LOCATION_FOR_X_MINS   = 0 hrs 0 min 5 
5) To set up the time for rule 11, set AFTER_TRACK_ENTER_X_MINS = 0 hrs 0 min 5 sec

Here is the examples

Begin
YEAR = 2000
SHOW_TRACK_EVERY_X_MINS = 0 hrs 0 min 30 sec
NO_CHANGE_IN_LOCATION_FOR_X_MINS = 0 hrs 0 min 20 sec
AFTER_TRACK_ENTER_X_MINS = 0 hrs 0 min 5 sec


To compile it
--------------
1) go to Distribution\makefits\
2) Type "buildit"


To run it  
----------
1) go to Distribution\makefits\
2) Type "runSim"

note: This will read the simulated data from Distribution\makefits\MAKEFITS\sim_input.txt


To change the input file
-------------------------
1) create the input file and put it into the "Distribution\makefits\MAKEFITS" directory
2) create the new script file for example  "runnewinput.bat" and put it into the    "Distribution\makefits"  

   If the name of the input file is "newinput.txt", the content in the script file should be as below.
	
   java -classpath .;../java_led/classes/Sentinel.jar;../java_led/classes/fesi.jar MAKEFITS/TrackMonitor -getLines MAKEFITS/newinput.txt


Important Files
----------------

The following files are contained in the Distribution directory

-makefits/buildit.bat	
 Scripts for recompiling the MAKEFITS application

-makefits/runsim.bat	
 Scripts for running this application (running the simulated input which contains second   value in Date-Time field)

-makefits/runit.bat	
 Scripts for running this application (running the simulated input which contains second   value in Date-Time field) from the Tst05a.G-input.txt

-makefits/MAKEFITS/sim_input.txt	
 The simulated input data 

-makefits/MAKEFITS/setup.txt 
 File to set up the time-constraint for ECA rules

-java_led/classes/Sentinel.jar   
 Sentinel package

-java_led/classes/fesi.jar 	
 Utilities package


