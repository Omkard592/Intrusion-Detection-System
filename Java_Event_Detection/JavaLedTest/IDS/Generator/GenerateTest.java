/*
Generator only generates non login/logout actions

Generator data only used for training

Use a subset of data for training

For testing, manually plant attacks in the remaining subset

It is essential to insert atleast one login/logout record for each user in the training subset

For each new user, the first entry will be login

*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateTest
{
	
	public  static double[] calculateMean(double mean,double count,double sumSquare,double newEntry)
	{
		double returnValue[]=new double[4];
		double newMean=((mean*count)+newEntry)/(count+1);
		sumSquare=(sumSquare+(newEntry)*(newEntry));
		double deviation=Math.sqrt((sumSquare/count)-(newMean)*(newMean));
		returnValue[0]=newMean;
		returnValue[1]=count+1;
		returnValue[2]=deviation;
		returnValue[3]=sumSquare;
		return returnValue;
	}
	
	public static void main(String[] args) 
	{
		
		///test for deviation
		double toUse[]=new double[4];
		double mean=0;
		double sumSquare=0;
		int SharedFile4Free=1,SharedFile5Free=1,SharedFile6Free=1,executeIf=0,fileNumber,act,a,temp,year,month,hour,date,min,minute,sec,SF4=0,SF5=0,SF6=0,startUsing=0;
		String SharedFile4UsedBy="null",SharedFile5UsedBy="null",SharedFile6UsedBy="null",actionperformed,fileName,month1,date1,hour1,min1,sec1,time,username;
		double valueToUse[]={0.38,21.87,43.53,0.4};
		for(int y=0;y<valueToUse.length;y++)
		{
			mean=mean+valueToUse[y];
			sumSquare=sumSquare+(valueToUse[y]*valueToUse[y]);
		}
		mean=mean/valueToUse.length;
		toUse=calculateMean(mean,valueToUse.length,sumSquare,71.05);
		System.out.println("newMean="+toUse[0]);
		System.out.println("newCount="+toUse[1]);
		System.out.println("Deviation="+toUse[2]);
		System.out.println("sumSquare="+toUse[3]);
		//test for deviation
		try {
				//// for a specific User
				//strOut = str.substring(0,7) + "...";
				year=2016;
				month=4;
				date=2;
				hour=8;
				min=1;
				sec=21;
				
				File file = new File("1.txt");
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				double resourceUsage,timeGenerated;
				String files[]={"File1","File2","File3","SharedFile4","SharedFile5","SharedFile6","Gcrome.exe","cmd.exe","calculator.exe","IExplorer.exe","MP3.exe"};   		//File1 word,File2 logfile is excelsheet,File3 is txt		////files can be log files
				String action[]={"Read","Write","Copy","Cut","Delete","Execute"};
				String Users[]={"User1","User2","User3","User4","User5","User6","User7","User8","User9","User10"};
				for(int i=0;i<=5000;i++)
				{
					
			{	temp=randInt(0,240);                                    /////////for TIME
					sec=temp%60;
					minute=min+temp/60;
					if(minute==min){min=minute+1;}
					else{min=minute;}
					if(min>=60)
						{
							min=min%60;
							hour=hour+1;
							if(hour>=24)
								{
									hour=hour%24;
									date=date+1;
									if(month/2==0)
										{
											if(month==2)
											{
												
												if(date>=28)
												{
												date=date%28;
												month=month+1;
	
													if(month>=12)
													{
														month=month%12;
														year=year+1;
													}
												}
											}
											else
											{	
												if(date>=30)
												{
												date=date%30;
												month=month+1;
													if(month>=12)
													{
														month=month%12;
														year=year+1;
													}
												}
											}
										}
									else
										{
												if(date>=31)
												{
												date=date%31;
												month=month+1;
													if(month>=12)
													{
														month=month%12;
														year=year+1;
													}
												}
											
										}
										
								}
						}
					String zero="0";
					month1=Integer.toString(month);
					date1=Integer.toString(date);
					hour1=Integer.toString(hour);
					min1=Integer.toString(min);
					sec1=Integer.toString(sec);
					if(month<10)
					{
							month1=zero.concat(month1);
					}
					if(date<10)
					{
							date1=zero.concat(date1);
					}
					if(hour<10)
					{
							hour1=zero.concat(hour1);
					}
					if(min<10)
					{
							min1=zero.concat(min1);
					}
					if(sec<10)
					{
							sec1=zero.concat(sec1);
					}
			}
					//System.out.println(year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
					
					fileNumber = randInt(0,10);
					fileName=files[fileNumber];
					int number= randInt(0,9);
					username=Users[number];
					//resourceUsage=randInt(30,60)+Math.random();
					//System.out.println(resourceUsage);
					switch (fileNumber)
					{
						case 0: //for File 1
									act=randInt(0,4);
									actionperformed=action[act];
									switch (act)
											{
												case 0: a=(int)((randInt(28,50)+Math.random())*100.0);      
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 1: a=(int)((randInt(28,50)+Math.random())*100.0);       
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 2: a=(int)((randInt(28,50)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 3: a=(int)((randInt(28,50)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 4: a=(int)((randInt(28,50)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												default: System.out.println("In read default");
														break;
											
											}
							break;
						case 1: //for File 2
									act=randInt(0,4);
									actionperformed=action[act];
									switch (act)
											{
												case 0: a=(int)((randInt(15,40)+Math.random())*100.0);      
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 1: a=(int)((randInt(15,40)+Math.random())*100.0);       
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 2: a=(int)((randInt(15,40)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 3: a=(int)((randInt(15,40)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 4: a=(int)((randInt(15,40)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												default: System.out.println("In read default");
														break;
											
											}
							break;
						case 2: //for File 3
									act=randInt(0,4);
									actionperformed=action[act];
									switch (act)
											{
												case 0: a=(int)((randInt(1,3)+Math.random())*100.0);      
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 1: a=(int)((randInt(1,3)+Math.random())*100.0);       
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break;
												case 2: a=(int)((randInt(1,3)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 3: a=(int)((randInt(1,3)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												case 4: a=(int)((randInt(1,3)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();
														break; 
												default: System.out.println("In read default");
														break;
											
											}
							break;
						case 3: //for SharedFile 4
								{	executeIf=0;
									startUsing=0;
									act=20;	
									actionperformed="null";
											if(SharedFile4Free==1)
											{
												SharedFile4UsedBy=username;
												SharedFile4Free=0;
												executeIf=1;
												SF4++;
												System.out.println("SF4 user="+username);
												startUsing=1;
											}
											else
											{
												if(SharedFile4UsedBy==username)
												{
													executeIf=1;
													SF4++;
													System.out.println("SF4 user="+username);
													if(SF4>10)
													{
														SF4=0;
														SharedFile4Free=1;
														SharedFile4UsedBy="null";
														startUsing=2;
													}
												}
											}
									if(executeIf==1)
									{
										if((startUsing==1)||(startUsing==2))
										{
											if(startUsing==1)
											{
												actionperformed="Open";
												
											}
											else
												if(startUsing==2)
												{
													actionperformed="Close";
													
												}
										}
										else
										{
										act=randInt(0,2);
										actionperformed=action[act];
										}
										switch (act)
												{
													case 0: a=(int)((randInt(5,10)+Math.random())*100.0);      
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 1: a=(int)((randInt(5,10)+Math.random())*100.0);       
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 2: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 3: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 4: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													default:a=(int)((randInt(1,20)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
												
												}
									}	
								}
							break;
						case 4: //for SharedFile 5
								{	executeIf=0;
									startUsing=0;
									act=20;
									actionperformed="null";
											if(SharedFile5Free==1)
											{
												SharedFile5UsedBy=username;
												SharedFile5Free=0;
												executeIf=1;
												SF5++;
												System.out.println("SF5 user="+username);
												startUsing=1;
											}
											else
											{
												if(SharedFile5UsedBy==username)
												{
													executeIf=1;
													SF5++;
													System.out.println("SF5 user="+username);
													if(SF5>8)
													{
														SF5=0;
														SharedFile5Free=1;
														SharedFile5UsedBy="null";
														startUsing=2;
													}
												}
											}
									if(executeIf==1)
									{
										if((startUsing==1)||(startUsing==2))
										{
											if(startUsing==1)
											{
												actionperformed="Open";
												
											}
											else
												if(startUsing==2)
												{
													actionperformed="Close";
													
												}
										}
										else
										{
										act=randInt(0,2);
										actionperformed=action[act];
										}
										switch (act)
												{
													case 0: a=(int)((randInt(10,15)+Math.random())*100.0);      
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 1: a=(int)((randInt(10,15)+Math.random())*100.0);       
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 2: a=(int)((randInt(10,15)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 3: a=(int)((randInt(10,15)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 4: a=(int)((randInt(10,15)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													default:a=(int)((randInt(1,20)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
												
												}
									}
								}
							break;
						case 5: //for SharedFile 6
								{	executeIf=0;
									startUsing=0;
									act=20;
									actionperformed="null";
											if(SharedFile6Free==1)
											{
												SharedFile6UsedBy=username;
												SharedFile6Free=0;
												executeIf=1;
												SF6++;
												System.out.println("SF6 use="+username);
												startUsing=1;
											}
											else
											{
												if(SharedFile6UsedBy==username)
												{
													executeIf=1;
													SF6++;
													System.out.println("SF6 use="+username);
													if(SF6>4)
													{
														SF6=0;
														SharedFile6Free=1;
														SharedFile6UsedBy="null";
														startUsing=2;
													}
												}
											}
									if(executeIf==1)
									{
										if((startUsing==1)||(startUsing==2))
										{
											if(startUsing==1)
											{
												actionperformed="Open";
												
											}
											else
												if(startUsing==2)
												{
													actionperformed="Close";
	
												}
										}
										else
										{
										act=randInt(0,2);
										actionperformed=action[act];
										}
										switch (act)
												{
													case 0: a=(int)((randInt(5,10)+Math.random())*100.0);      
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 1: a=(int)((randInt(5,10)+Math.random())*100.0);       
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
													case 2: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 3: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													case 4: a=(int)((randInt(5,10)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break; 
													default:a=(int)((randInt(1,20)+Math.random())*100.0);
															resourceUsage=a/100.0;
															bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
															bw.newLine();
															break;
												
												}
									}
								}
							break;
						case 6:
								{
									actionperformed=action[5];
									a=(int)((randInt(80,130)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();					
								}
							break;
						case 7:
								{
									actionperformed=action[5];
									a=(int)((randInt(1,2)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();					
								}
							break;
						case 8:                       
								{
									actionperformed=action[5];
									a=(int)((randInt(9,12)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();					
								}
							break;
						case 9:
								{
									actionperformed=action[5];
									a=(int)((randInt(30,130)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();					
								}
							break;
						case 10:
								{
									actionperformed=action[5];
									a=(int)((randInt(110,120)+Math.random())*100.0);
														resourceUsage=a/100.0;
														bw.write(username+","+actionperformed+","+fileName+","+"1,"+resourceUsage+","+year+":"+month1+":"+date1+":"+hour1+":"+min1+":"+sec1);
														bw.newLine();					
								}
							break;
						default: System.out.println("Copy");
								break;
					
					}
				}
				String content = "This is the content to write into file";

				

				// if file doesnt exists, then create it
				/* if (!file.exists()) {
					file.createNewFile();
				} */

				
				bw.write(content);
				bw.newLine();
				bw.close();

				System.out.println("Done");

			}
		catch (IOException e) 
			{
			e.printStackTrace();
			}
			
	}
	public static int randInt(int min, int max) 
	{	
	Random rand=new Random();
	int randomNum = rand.nextInt((max - min) + 1) + min;
    return randomNum;
	}
}