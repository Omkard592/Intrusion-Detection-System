This dirs contains 2 subdirs (consumer and producer)

This demonstarted the NOT operation. The NOT event will be detected
on the local site.

It also includes the following bat file

1) startNot_1package.bat :: for starting the demo
   
	REMARK !!!  	
	Before starting the demo, we need to set the codebase 
	class path for RMI communications in the following files.
	- \\Java_Event_Detection\source\startGED.bat

	- \\not_1\consumer\runit.bat
	- \\not_1\producer\runit.bat

	set STUBCLASSES=I:\Java_Event_Detection\classes
	where I is the path name the location  of the zip file
