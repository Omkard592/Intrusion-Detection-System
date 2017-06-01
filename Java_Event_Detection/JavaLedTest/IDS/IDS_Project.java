import sentinel.led.*;
import java.io.*;
import java.util.* ;
import java.text.*;
import java.util.Date;
import java.util.HashMap;

public class IDS_Project
{
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private String subject = "unknown";
	private String action = "unknown";
	private static HashMap<String,Double> hm =  new HashMap<String,Double>();
	private static HashMap<String,ArrayList<String>> subject_action_list = new HashMap<String,ArrayList<String>>();
	private static HashMap<String,ArrayList<Double>> subject_object_list = new HashMap<String,ArrayList<Double>>();
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public IDS_Project()
	{
		//System.out.println("m here");
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public IDS_Project(String action) 
	{
		//System.out.println("m here too ");
		this.action = action;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void writeToAttackList(String sub,String obj,String ts,String type,String flag)throws Exception
	{
		FileWriter fw = new FileWriter("AttackList.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
			
		out.println(flag+" Detected intrusion: possible \""+type+"\" by \""+sub+"\" on resource \""+obj+"\" at \""+ts+"\"");
			
		out.close();
		bw.close();
		fw.close();
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void train_sub_act_list(String subject,String action) 
	{
		hm.put(subject, new Double(0));
		try
		{
			ArrayList al = ((ArrayList)subject_action_list.get(subject));
			
			if(al==null)			//subject not in subject_action_list, thus add him to it
			{
				ArrayList<String> alist = new ArrayList<>();
				alist.add(action);
				subject_action_list.put(subject,alist);
			}
			else					//subject is in subject_action_list, thus add action only if it is new
			{
				ArrayList<String> alist = new ArrayList<>();
				// code to eliminate duplicate actions from the list
				alist.addAll(al);
				alist.add(action);
				Set<String> temphs = new HashSet<>();
				temphs.addAll(alist);
				alist.clear();
				alist.addAll(temphs);
				subject_action_list.put(subject,alist);
			}
		}
		catch(Exception e)
		{
			System.out.println("in catch train_sub_act_list "+e.getMessage());
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	double[] calculateMean(double mean,double sumSquare,double count,double newEntry)
	{
		double returnValue[]=new double[4];
		double newMean=((mean*count)+newEntry)/(count+1);
		sumSquare=(sumSquare+(newEntry)*(newEntry));
		double deviation=Math.sqrt((sumSquare/count)-(newMean)*(newMean));
		returnValue[0]=newMean;
		returnValue[1]=deviation;
		returnValue[2]=sumSquare;
		returnValue[3]=count+1;
		return returnValue;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void train_sub_obj_list(String subject,String object,double res_usg) 
	{
		try
		{
			ArrayList al = ((ArrayList)subject_object_list.get(subject+object));
			
			if(al==null)		
			{
				ArrayList<Double> alist = new ArrayList<>();
				alist.add(res_usg);				//initial mean
				alist.add(res_usg);				//initial std
				alist.add(res_usg*res_usg);		//initial sum square
				alist.add(1.0);					//initial count
				subject_object_list.put(subject+object,alist);
			}
			else			
			{
				double newstd[]=calculateMean((Double)al.get(0),(Double)al.get(2),(Double)al.get(3),res_usg);
				
				ArrayList<Double> alist = new ArrayList<>();
				alist.add(newstd[0]);
				alist.add(newstd[1]);
				alist.add(newstd[2]);
				alist.add(newstd[3]);
				subject_object_list.put(subject+object,alist);
			}
		}
		catch(Exception e)
		{
			System.out.println("in catch train_sub_obj_list "+e.getMessage());
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void startDetection() 
	{
		System.out.println("Start Detection for " + this.action);
		EventHandle[] startDetection = ECAAgent.getEventHandles("startDetection");
		ECAAgent.raiseBeginEvent(startDetection,this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void endDetection() 
	{
	    System.out.println("End Detection for " + this.action);
	    EventHandle[] endDetection = ECAAgent.getEventHandles("endDetection");
	    ECAAgent.raiseBeginEvent(endDetection,this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void setLoginDetails(String subject,String object,String exception,String timestamp,int eventCount,String flag) 
	{	
		EventHandle[] setLoginDetails = ECAAgent.getEventHandles("setLoginDetails");
		ECAAgent.insert(setLoginDetails,"action",this.action);
		ECAAgent.insert(setLoginDetails,"subject",subject);
		ECAAgent.insert(setLoginDetails,"excep",exception);
		ECAAgent.insert(setLoginDetails,"object",object);
		ECAAgent.insert(setLoginDetails,"timestamp",timestamp);
		ECAAgent.insert(setLoginDetails,"flag",flag);
		ECAAgent.insert(setLoginDetails, "subject" + eventCount, subject);
		ECAAgent.insert(setLoginDetails, "excep" + eventCount, exception);
		ECAAgent.raiseEndEvent(setLoginDetails,this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean CheckFordetectBreakIn(ListOfParameterLists paramLists)
	{
		String thisExcep = "";
		String thisSub = "";
		int eventCount = 0;	
		double failcount=0;
		
		try
		{	
			for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
			{
				ParameterList paramList = (ParameterList) e.nextElement();
				
				try
				{
					thisExcep = (String)paramList.getObject("excep"+eventCount);
					thisSub = (String)paramList.getObject("subject");
				}
				catch(Exception err)
				{
					//ignore
					//System.out.println("in catch CheckFordetectBreakIn paramlist problem "+err.getMessage());
				}
				
				if(thisExcep.equals("0"))
				{
					failcount = ((Double)hm.get(thisSub)).doubleValue();
					failcount+=1;
					hm.put(thisSub, new Double(failcount));					
				}
				eventCount++;
			}
			
			if(failcount >=10)		//if login fail count exceeds the threshold
			{
				return true;
			}
		}
		catch(Exception ex)
		{ 	
			hm.put(thisSub, new Double(1));		//new user who was not in training
			//System.out.println("in catch CheckFordetectBreakIn "+ex.getMessage());
		}
		
		return false;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void PerformActionRuledetectBreakIn(ListOfParameterLists paramLists)throws Exception
	{
		String thisSub="";
		String thisObject="";
		String thisTS="";
		String thisFlag="";
		
		for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
		{
			ParameterList paramList = (ParameterList) e.nextElement();
			
			try
			{
				thisSub = (String)paramList.getObject("subject");
				thisObject = (String)paramList.getObject("object");
				thisTS = (String)paramList.getObject("timestamp");
				thisFlag = (String)paramList.getObject("flag");
				
			}
			catch (Exception err)
			{
				//ignore
				//System.out.println("in catch PerformActionRuledetectBreakIn paramlist problem "+err.getMessage());
			}
			
		}
		System.out.println("*****Condition is true. Detected Breakin for the Subject "+thisSub+"*****"); 
		writeToAttackList(thisSub,thisObject,thisTS,"Breakin",thisFlag);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void setActionDetails(String subject,String action,String timestamp,String flag)
	{
		EventHandle[] setActionDetails = ECAAgent.getEventHandles("setActionDetails");
		ECAAgent.insert(setActionDetails,"action",action);
		ECAAgent.insert(setActionDetails,"subject",subject);
		ECAAgent.insert(setActionDetails,"timestamp",timestamp);
		ECAAgent.insert(setActionDetails,"flag",flag);
		ECAAgent.raiseEndEvent(setActionDetails,this);		
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void setObjectDetails(String object,double res_usage,String flag)
	{
		EventHandle[] setObjectDetails = ECAAgent.getEventHandles("setObjectDetails");
		ECAAgent.insert(setObjectDetails,"object",object);
		ECAAgent.insert(setObjectDetails,"res_usage",res_usage);
		ECAAgent.insert(setObjectDetails,"flag",flag);
		ECAAgent.raiseEndEvent(setObjectDetails,this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean CheckFordetectMasquarade(ListOfParameterLists paramLists) 
	{
		String thisObject = "";
		String thisSub = "";
		String thisAction = "";
		Double thisRes_Usg=0.0;
		int dec = 0;
		
		try
		{	
			for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
			{
				ParameterList paramList = (ParameterList) e.nextElement();
				
				if(dec==0)			//parameters from setActionDetails
				{
					try
					{
						thisSub = (String)paramList.getObject("subject");
						thisAction = (String)paramList.getObject("action");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch CheckFordetectMasquarade paramlist problem "+err.getMessage());
					}
				}
				else				//parameters from setObjectDetails
				{
					try
					{						
						thisObject = (String)paramList.getObject("object");
						thisRes_Usg = (Double)paramList.getObject("res_usage");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch CheckFordetectMasquarade paramlist problem "+err.getMessage());
					}
				}
				
				dec++;
			}
			
			int obj_exists=0;
			
			try
			{
				ArrayList al = ((ArrayList)subject_object_list.get(thisSub+thisObject));
				
				if(!(al==null))			//object exists already
					obj_exists=1;
				
				//System.out.println("al "+al);
				ArrayList<String> templist = ((ArrayList)subject_action_list.get(thisSub));
				
				if((!templist.contains(thisAction)) && (thisRes_Usg>((Double)al.get(0)+((Double)al.get(1)*1.8))))	//masquerade
				{
					return true;
				}
				/*else		//all normal nothing suspicious, hence update the profile
				{
					if(thisObject.indexOf(".exe")==-1)
					{
						double newstd[]=calculateMean((Double)al.get(0),(Double)al.get(2),(Double)al.get(3),thisRes_Usg);
						
						ArrayList<Double> alist = new ArrayList<>();
						alist.add(newstd[0]);
						alist.add(newstd[1]);
						alist.add(newstd[2]);
						alist.add(newstd[3]);
						subject_object_list.put(thisSub+thisObject,alist);
					}
				}*/
			}
			catch(Exception e)
			{
				if(obj_exists==0)		//new  subject/object
				{
					ArrayList<Double> alist = new ArrayList<>();
					alist.add(thisRes_Usg);					//initial mean
					alist.add(thisRes_Usg);					//initial std
					alist.add(thisRes_Usg*thisRes_Usg);		//initial sum square
					alist.add(1.0);							//initial count
					subject_object_list.put(thisSub+thisObject,alist);
				}
				
				/*ArrayList al = ((ArrayList)subject_action_list.get(thisSub));
				
				if(al==null)			//subject not in subject_action_list, thus add him to it
				{
					ArrayList<String> alist = new ArrayList<>();
					alist.add(thisAction);
					subject_action_list.put(thisSub,alist);
				}*/
				/*else					//subject is in subject_action_list, thus add action only if it is new
				{
					ArrayList<String> alist1 = new ArrayList<>();
					// code to eliminate duplicate actions from the list
					alist1.addAll(al);
					alist1.add(thisAction);
					Set<String> temphs = new HashSet<>();
					temphs.addAll(alist1);
					alist1.clear();
					alist1.addAll(temphs);
					subject_action_list.put(thisSub,alist1);
				}*/
				//System.out.println("in catch CheckFordetectMasquarade no profile found "+thisSub+obj_exists+" "+e.getMessage());
			}
		}
		catch(Exception ex)
		{ 
			//System.out.println("in catch CheckFordetectMasquarade "+ex.getMessage());
		}
		return false;	
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void PerformActionRuledetectMasquarade(ListOfParameterLists paramLists)throws Exception
	{
		String thisSub="";
		String thisObject="";
		String thisTS="";
		String thisFlag="";
		int dec = 0;
		
		for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
			{
				ParameterList paramList = (ParameterList) e.nextElement();
				
				if(dec==0)
				{
					try
					{
						thisSub = (String)paramList.getObject("subject");
						thisTS = (String)paramList.getObject("timestamp");
						thisFlag = (String)paramList.getObject("flag");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch PerformActionRuledetectMasquarade paramlist problem "+err.getMessage());
					}
				}
				else
				{
					try
					{
						thisObject = (String)paramList.getObject("object");
						thisFlag = (String)paramList.getObject("flag");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch PerformActionRuledetectMasquarade paramlist problem "+err.getMessage());
					}
				}
				
				dec++;
				
			}
			
		System.out.println("*****Condition is true. Detected Masquerade for the Subject "+thisSub+"*****"); 	
		writeToAttackList(thisSub,thisObject,thisTS,"Masquerade",thisFlag);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void setVObjectDetails(String object,double res_usage,String flag)
	{
		EventHandle[] setVObjectDetails = ECAAgent.getEventHandles("setVObjectDetails");
		ECAAgent.insert(setVObjectDetails,"object",object);
		ECAAgent.insert(setVObjectDetails,"res_usage",res_usage);
		ECAAgent.insert(setVObjectDetails,"flag",flag);
		ECAAgent.raiseEndEvent(setVObjectDetails,this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	void setVActionDetails(String subject,String action,String timestamp,String flag)
	{
		EventHandle[] setVActionDetails = ECAAgent.getEventHandles("setVActionDetails");
		ECAAgent.insert(setVActionDetails,"action",action);
		ECAAgent.insert(setVActionDetails,"subject",subject);
		ECAAgent.insert(setVActionDetails,"timestamp",timestamp);
		ECAAgent.insert(setVActionDetails,"flag",flag);
		ECAAgent.raiseEndEvent(setVActionDetails,this);
	}	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean CheckFordetectVirus(ListOfParameterLists paramLists) 
	{
		String thisObject = "";
		String thisSub = "";
		String thisAction = "";
		Double thisRes_Usg=0.0;
		int dec = 0;
		
		try
		{	
			for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
			{
				ParameterList paramList = (ParameterList) e.nextElement();
				
				if(dec==0)				//parameters from setVActionDetails
				{
					try
					{
						thisSub = (String)paramList.getObject("subject");
						thisAction = (String)paramList.getObject("action");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch CheckFordetectVirus paramlist problem "+err.getMessage());
					}
				}
				else					//parameters from setVObjectDetails
				{
					try
					{
						thisObject = (String)paramList.getObject("object");
						thisRes_Usg = (Double)paramList.getObject("res_usage");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch CheckFordetectVirus paramlist problem "+err.getMessage());
					}
				}
				
				dec++;
			}
			
			int obj_exists=0;
			
			try
			{
				ArrayList al = ((ArrayList)subject_object_list.get(thisSub+thisObject));
				
				if(!(al==null))			//object exists already
					obj_exists=1;
				
				ArrayList<String> templist = ((ArrayList)subject_action_list.get(thisSub));
				
				if(!(thisObject.indexOf(".exe")==-1))		//if it is an exe
				{
					if((templist.contains(thisAction)) && (thisRes_Usg>((Double)al.get(0)+((Double)al.get(1)*1.8))))
					{
						return true;
					}
				}
				
				else		//all normal nothing suspicious, hence update the profile
				{
					double newstd[]=calculateMean((Double)al.get(0),(Double)al.get(2),(Double)al.get(3),thisRes_Usg);
					
					ArrayList<Double> alist = new ArrayList<>();
					alist.add(newstd[0]);
					alist.add(newstd[1]);
					alist.add(newstd[2]);
					alist.add(newstd[3]);
					subject_object_list.put(thisSub+thisObject,alist);
				}
			}
			catch(Exception e)
			{
				if(obj_exists==0)		//new  subject/object
				{
					ArrayList<Double> alist = new ArrayList<>();
					alist.add(thisRes_Usg);					//initial mean
					alist.add(thisRes_Usg);					//initial std
					alist.add(thisRes_Usg*thisRes_Usg);		//initial sum square
					alist.add(1.0);							//initial count
					subject_object_list.put(thisSub+thisObject,alist);
				}
				//System.out.println("in catch CheckFordetectVirus no profile found "+thisSub+obj_exists+" "+e.getMessage());
			}
		}
		catch(Exception ex)
		{ 
			//System.out.println("in catch CheckFordetectVirus "+ex.getMessage());
		}
		
		return false;	
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void PerformActionRuledetectVirus(ListOfParameterLists paramLists)throws Exception
	{
		String thisSub="";
		String thisObject="";
		String thisTS="";
		String thisFlag="";
		int dec = 0;
		
		for (Enumeration e = paramLists.elements() ; e.hasMoreElements() ;) 
			{
				ParameterList paramList = (ParameterList) e.nextElement();
				
				if(dec==0)					//parameters from setVActionDetails
				{
					try
					{
						thisSub = (String)paramList.getObject("subject");
						thisTS = (String)paramList.getObject("timestamp");
						thisFlag = (String)paramList.getObject("flag");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch PerformActionRuledetectVirus paramlist problem "+err.getMessage());
					}
				}
				else						//parameters from setVObjectDetails
				{
					try
					{
						thisObject = (String)paramList.getObject("object");
						thisFlag = (String)paramList.getObject("flag");
					}
					catch (Exception err)
					{
						//ignore
						//System.out.println("in catch PerformActionRuledetectVirus paramlist problem "+err.getMessage());
					}
				}
				
				dec++;
			}
			
		System.out.println("*****Condition is true. Detected Virus for the Subject "+thisSub+"*****"); 	
		writeToAttackList(thisSub,thisObject,thisTS,"Virus",thisFlag);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String args[])
	{
		String subject;
		String action;
		String object;
		String excep = "";
		double res_usg;
		String timestamp = "";
		String flag="";
		
		IDS_Project action1 = new IDS_Project("Breakin");		
		IDS_Project action2 = new IDS_Project("Masquarade");
		IDS_Project action3 = new IDS_Project("Virus");
		
		try
		{	
			boolean success = (new File("AttackList.txt")).delete();
		}
		catch(Exception e)
		{
			System.out.println("Could not delete AttackList.txt");
		}
		
		ECAAgent myAgent = ECAAgent.initializeECAAgent();
		
		
		EventHandle startDetection = myAgent.createPrimitiveEvent("startDetection","IDS_Project",EventModifier.BEGIN, 
																  "void startDetection()", DetectionMode.SYNCHRONOUS);
																	
	    EventHandle endDetection = myAgent.createPrimitiveEvent("endDetection","IDS_Project",EventModifier.BEGIN, 
																"void endDetection()", DetectionMode.SYNCHRONOUS);
		
		EventHandle setLoginDetails = myAgent.createPrimitiveEvent("setLoginDetails","IDS_Project",EventModifier.END, 
																   "void setLoginDetails()",DetectionMode.SYNCHRONOUS);
		
		EventHandle detectBreakIn = myAgent.createCompositeEvent(EventType.APERIODIC,"detectBreakIn", 
	    									                     startDetection, setLoginDetails, endDetection);
		
		myAgent.createRule("detectBreakInRule",detectBreakIn,"IDS_Project.CheckFordetectBreakIn","IDS_Project.PerformActionRuledetectBreakIn", 
						   1, CouplingMode.DEFAULT,ParamContext.RECENT);
							
		EventHandle setActionDetails = myAgent.createPrimitiveEvent("setActionDetails","IDS_Project",EventModifier.END, 
																	"void setActionDetails()",DetectionMode.SYNCHRONOUS);
		
		EventHandle setObjectDetails = myAgent.createPrimitiveEvent("setObjectDetails","IDS_Project",EventModifier.END, 
																	"void setObjectDetails()",DetectionMode.SYNCHRONOUS);
									
		EventHandle detectMasquarade = myAgent.createCompositeEvent(EventType.AND,"masqueradeAndEvent",
																	setActionDetails,setObjectDetails);		
	    									
		myAgent.createRule("detectMasquaradeRule",detectMasquarade,"IDS_Project.CheckFordetectMasquarade","IDS_Project.PerformActionRuledetectMasquarade", 
						   1, CouplingMode.IMMEDIATE,ParamContext.CHRONICLE);
		
		EventHandle setVActionDetails = myAgent.createPrimitiveEvent("setVActionDetails","IDS_Project",EventModifier.END, 
																	 "void setVActionDetails()",DetectionMode.SYNCHRONOUS);
									
		EventHandle setVObjectDetails = myAgent.createPrimitiveEvent("setVObjectDetails","IDS_Project",EventModifier.END, 
																	 "void setVObjectDetails()",DetectionMode.SYNCHRONOUS);
		
		EventHandle detectVirus = myAgent.createCompositeEvent(EventType.AND,"detectVirus", 
															   setVActionDetails,setVObjectDetails);
		
		myAgent.createRule("detectVirusRule",detectVirus,"IDS_Project.CheckFordetectVirus","IDS_Project.PerformActionRuledetectVirus", 
						   1, CouplingMode.DEFAULT,ParamContext.CHRONICLE);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try
		{
			action1.startDetection();
			action2.startDetection();
			action3.startDetection();
			
			String line;    		// String that holds current file line
			int count = 0;  		// Line number of count
			int eventCount = 0;		// Counter to process multiple events for periodic tracking
			
			FileReader input = new FileReader("dsa.csv");
			BufferedReader bufRead = new BufferedReader(input);
			line = bufRead.readLine();
			count++;

			while (line != null)
			{
				try
				{
					String [] userInfo = null;		// Array for file line contents
					userInfo = line.split(",");	    // Split the file line by the commas
					subject = userInfo[0];
					
					if(subject.equals("Subject"))
					{
						line = bufRead.readLine();
						count++;
						continue;
					}
					
					action = userInfo[1];
					object = userInfo[2];
					excep = userInfo[3];
					res_usg = Double.parseDouble(userInfo[4]);
					timestamp = userInfo[5];
					flag = userInfo[6];
					//System.out.println(flag);
					
					if(count<=9001)			//training phase for first n records
					{
						//System.out.println("training");
						action2.train_sub_act_list(subject,action);
						action2.train_sub_obj_list(subject,object,res_usg);
					}
					
					else						//testing phase
					{
						if(!(action.equalsIgnoreCase("Login") || action.equalsIgnoreCase("Logout")))
						{
							action2.setActionDetails(subject,action,timestamp,flag);
							action2.setObjectDetails(object,res_usg,flag);
							action3.setVActionDetails(subject,action,timestamp,flag);
							action3.setVObjectDetails(object,res_usg,flag);
						}
					
						Thread.sleep (20);			//essential to give enough processing time for previous events
	
						if(action.equals("Login") && excep.equals("0"))		//if login fails, process this event
						{
							action1.setLoginDetails(subject,object,excep,timestamp,eventCount,flag);
							eventCount++;
							Thread.sleep (10);
						}
						
						if(action.equals("Login") && excep.equals("1"))		//if login is a success, reset fail count
						{
							hm.put(subject, new Double(0));
						}
						
					}
					
					line = bufRead.readLine();
					count++;
			
				}
				catch (Exception err)
				{ 
					//Do nothing
				}
			}
			
			bufRead.close();
			
			action1.endDetection();
			action2.endDetection();
			action3.endDetection();
			
			try
			{
				Thread.sleep (1000);
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		catch(FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}	
		catch(Exception e) 
		{
		
		}
		
		System.out.println("End of IDS program...\n");
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////