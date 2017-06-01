This dirs contains 3 subdirs (consumer,producer, and conprod)

This demonstarted how to define the global composite event (OR), and 
how to insert/extract data in ParameterList. 

It also includes the following bat file

1) startOrPackage.bat :: for starting the demo
   
	REMARK !!!  	
	Before starting the demo, we need to set the codebase 
	class path for RMI communications in the following files.
	- \\Java_Event_Detection\source\startGED.bat

	- \\OR\consumer\runit.bat
	- \\OR\conprod\runit.bat
	- \\OR\producer\runit.bat

	set STUBCLASSES=I:\Java_Event_Detection\classes
	where I is the path name the location  of the zip file
