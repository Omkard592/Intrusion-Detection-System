package MAKEFITS;

import java.io.*;
import java.util.*;

public class LatLongReader
{
  long[] lTimeArray;
  int[] iTrackArray;
  String sLatArray[];
  String sLongArray[];
  String sInputFile;


  public LatLongReader(String sInputFile)
  {
    this.sInputFile = sInputFile;
  }

  public void writeEventsToFile(String sOutFile)
  {
    String sOutput = toString();
    writeToFile(sOutput,sOutFile);
  }

  public static void writeToFile(String sCommand, String sOutFile)
  {
    PrintWriter pw;

    try { pw = new PrintWriter(new FileWriter(sOutFile));}
    catch(Exception exc)
    {
      System.err.println("OPEN FILE ERROR: Sentinel:writeToFile() -- " + exc);
      return;
    }

    try { pw.print(sCommand);}
    catch(Exception exc)
    {
      System.err.println("WRITE FILE ERROR: Sentinel:writeToFile() -- " + exc);
    }

    try { pw.close();}
    catch(Exception exc)
    {
      System.err.println("CLOSE FILE ERROR: Sentinel:writeToFile() -- " + exc);
    }
  }  


  public static void writeToFile(String[] sLines, String sOutFile)
  {
    PrintWriter pw;
 
    try { pw = new PrintWriter(new FileWriter(sOutFile));}
    catch(Exception exc)
    {
      System.err.println("OPEN FILE ERROR: Sentinel:writeToFile() -- " + exc);
      return;
    }
 
    try 
    { 
     for (int jj=0;jj<sLines.length;jj++)
       pw.println(sLines[jj]);
    }
    catch(Exception exc)
    {
      System.err.println("WRITE FILE ERROR: Sentinel:writeToFile() -- " + exc);
    }
 
    try { pw.close();}
    catch(Exception exc)
    {
      System.err.println("CLOSE FILE ERROR: Sentinel:writeToFile() -- " + exc);
    }
  }


  public long[] getTimes(int iStartIndex, int iStopIndex)
  {
    int iLength = iStopIndex - iStartIndex + 1;
    long[] lTimes = new long[iLength];
    for (int jj=0;jj<iLength;jj++)
      lTimes[jj] = lTimeArray[iStartIndex+jj];
    return(lTimes);
  }

  public int[] getTracks(int iStartIndex, int iStopIndex)
  {
    int iLength = iStopIndex - iStartIndex + 1;
    int[] iTracks = new int[iLength];
    for (int jj=0;jj<iLength;jj++)
      iTracks[jj] = iTrackArray[iStartIndex+jj];
    return(iTracks);
  }

  public String[] getLats(int iStartIndex, int iStopIndex)
  {
    int iLength = iStopIndex - iStartIndex + 1;
    String[] sLats = new String[iLength];
    for (int jj=0;jj<iLength;jj++)
      sLats[jj] = sLatArray[iStartIndex+jj];
    return(sLats);
  }

  public String[] getLongs(int iStartIndex, int iStopIndex)
  {
    int iLength = iStopIndex - iStartIndex + 1;
    String[] sLongs = new String[iLength];
    for (int jj=0;jj<iLength;jj++)
      sLongs[jj] = sLongArray[iStartIndex+jj];
    return(sLongs);
  }

  public int[] getEvents(long lStartTime, long lStopTime)
  {
    int iStartIndex = -1;
    int iStopIndex = -1;
    int jj;
    for (jj=0;jj<lTimeArray.length;jj++)
    {
      if ((lTimeArray[jj] >= lStartTime)  && (lTimeArray[jj] < lStopTime) && (iStartIndex == -1))
        iStartIndex = jj;
      if (lTimeArray[jj] >= lStopTime)
      {
        iStopIndex = jj-1;
        break;
      }
    }

    if (iStopIndex == -1)
      iStopIndex = iStartIndex;

    if (iStartIndex == -1)
    {
      if (jj==lTimeArray.length)
        iStartIndex = -2;
      iStopIndex = -1;
    }
    	
    int[] iReturn = new int[2];
    iReturn[0] = iStartIndex;
    iReturn[1] = iStopIndex;
    return(iReturn);
  }

  public long getFirstEventStartTime()
  {
    return(lTimeArray[0]);
  }
        
   
      

  public static void main(String[] args)
  {
    if (args.length < 1)
    {
      System.out.println("USAGE: java LatLongReader <inputFile>");
      return;
    }

    LatLongReader llr = new LatLongReader(args[0]);
    llr.run();
  }

  public void run()
  {
    
    loadData(sInputFile);

    //display();

    sortByTime();
    String sSortOutputFile = sInputFile + ".sort";
    System.out.println(" WRITING SORTED OUTPUT to " + sSortOutputFile);
    writeEventsToFile(sSortOutputFile); 
    //display();

  }

  public static String getDateGMT(long lTime)
  {
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.setTime(new Date(lTime));
    String sMonth = "" + (gc.get(Calendar.MONTH)+1);
    String sDay = "" + (gc.get(Calendar.DAY_OF_MONTH)+1);
    String sHour = "" + gc.get(Calendar.HOUR_OF_DAY);
    if (sHour.length() < 2)
      sHour = "0" + sHour;
    String sMin = "" + gc.get(Calendar.MINUTE);
    if (sMin.length() < 2)
      sMin = "0" + sMin;
   
    String sReturn = sMonth + "/" + sDay + "/" + sHour + sMin + " ZULU"; 
    return(sReturn);
  } 

  public void sortByTime()
  {
    long lTimeTemp;
    int iTrackTemp;
    String sLatTemp;
    String sLongTemp;

    for (int jj=0;jj<lTimeArray.length;jj++)
      for (int kk=0;kk<lTimeArray.length;kk++)
        if (lTimeArray[kk] > lTimeArray[jj])
        {
           lTimeTemp = lTimeArray[kk];
           lTimeArray[kk] = lTimeArray[jj];
           lTimeArray[jj] = lTimeTemp;

           iTrackTemp = iTrackArray[kk];
           iTrackArray[kk] = iTrackArray[jj];
           iTrackArray[jj] = iTrackTemp;

           sLatTemp = sLatArray[kk];
           sLatArray[kk] = sLatArray[jj];
           sLatArray[jj] = sLatTemp;

           sLongTemp = sLongArray[kk];
           sLongArray[kk] = sLongArray[jj];
           sLongArray[jj] = sLongTemp;
        }
  }
          


  public void display()
  {
    for (int jj=0;jj<iTrackArray.length;jj++)
    {
      System.out.println("TIME: " + getDateGMT(lTimeArray[jj])  +
        " TRACK = " + iTrackArray[jj] +
	" LAT/LONG = " + sLatArray[jj] + "/" + sLongArray[jj]);
    }
  }

  public String toString()
  {
    String sResult = "";
    for (int jj=0;jj<iTrackArray.length;jj++)
    {
      sResult += "TIME: " + getDateGMT(lTimeArray[jj]) +
        " TRACK = " + iTrackArray[jj] +
	" LAT/LONG = " + sLatArray[jj] + "/" + sLongArray[jj] + "\n";
    }
    return(sResult);
  }


  public void loadData(String sInputFile)
  {
    String[] sLines = getLines(sInputFile);

    Vector vTracks = new Vector();
    Vector vTimes = new Vector();
    Vector vLats = new Vector();
    Vector vLongs = new Vector();

    int iTrackNum = 0;
    long lTime;
    String[] sLatLong;
    for (int jj=0;jj<sLines.length;jj++)
    {
      if (sLines[jj].startsWith("CTC")) 
      {
        iTrackNum = getTrackNum(sLines[jj]);
        continue;
      }
   
      if (sLines[jj].startsWith("POS"))
      {
        lTime = getTime(sLines[jj]);
        sLatLong = getLatLong(sLines[jj]);
        if (sLatLong == null)
          continue;
        vTracks.addElement(new Integer(iTrackNum));
        vTimes.addElement(new Long(lTime));
        vLats.addElement(sLatLong[0]);
        vLongs.addElement(sLatLong[1]);
      }
    }

    int iNumEvents = vLats.size();
    iTrackArray = new int[iNumEvents];
    sLatArray = new String[iNumEvents];
    sLongArray = new String[iNumEvents];
    lTimeArray = new long[iNumEvents];

    for (int jj=0;jj<iNumEvents;jj++)
    {
       iTrackArray[jj] = ((Integer) vTracks.elementAt(jj)).intValue();
       lTimeArray[jj] = ((Long) vTimes.elementAt(jj)).longValue();
       sLatArray[jj] = (String) vLats.elementAt(jj);
       sLongArray[jj] = (String) vLongs.elementAt(jj);
    }
  }

  public int getTrackNum(String sCTCLine)
  {
    StringTokenizer st = new StringTokenizer(sCTCLine,"/");
    st.nextToken();
    String sTrackNum = st.nextToken();
    sTrackNum = sTrackNum.substring(1,sTrackNum.length());
    int iTrackNum = Integer.parseInt(sTrackNum);
    return(iTrackNum);
  }

  public long getTime(String sPOSLine)
  {
    String sMonths[] = {"JAN","FEB","MAR","APR","MAY","JUN","JUL",
	"AUG","SEP","OCT","NOV","DEC"};

    StringTokenizer st = new StringTokenizer(sPOSLine,"/");
    st.nextToken();
    String sTime = st.nextToken(); // e.g. 030800Z1
    String sMonth = st.nextToken(); // e.g. MAR
    
    String sDay = sTime.substring(0,2);
    int iDay = Integer.parseInt(sDay);
    String sHr = sTime.substring(2,4);
    int iHr = Integer.parseInt(sHr);
    String sMin = sTime.substring(4,6);
    int iMin = Integer.parseInt(sMin);
    TimeZone tz = TimeZone.getTimeZone("GMT");

    GregorianCalendar gc = new GregorianCalendar(tz);
    int iMonth = 0;
    for (int jj=0;jj<sMonths.length;jj++)
      if (sMonths[jj].equals(sMonth))
        iMonth = jj;

    gc.set(Calendar.MONTH,iMonth);
    gc.set(Calendar.DAY_OF_MONTH,iMonth);
    gc.set(Calendar.HOUR_OF_DAY,iHr);
    gc.set(Calendar.MINUTE,iMin);

    long lTime = gc.getTime().getTime();
    return(lTime);
  }

  public String[] getLatLong(String sPOSLine)
  {
    StringTokenizer st = new StringTokenizer(sPOSLine,"/");
    st.nextToken();
    String sTime = st.nextToken(); // e.g. 030800Z1
    sTime = sTime.substring(0,sTime.length() -2);
    String sMonth = st.nextToken(); // e.g. MAR

    String sLat = st.nextToken();
    sLat = sLat.substring(0,sLat.length()-1);

    String sLong = st.nextToken();
    sLong = sLong.substring(0,sLong.length()-1);

    String[] sReturn = new String[2];
    sReturn[0] = sLat;
    sReturn[1] = sLong;
    return(sReturn);
  }
  

  public static String[] getLines(String sInputFile)
  {
    String[] sWords;
    BufferedReader br;
        // count lines in word file
    try { br = new BufferedReader(new FileReader(sInputFile)); }
    catch(Exception exc)
    {
      System.err.println("OPEN FILE ERROR: LatLongReader:getLines() -- " + exc);
      return(null);
    }
 
    String sNextLine;
    int iCountLines;
    try
    {
      sNextLine = br.readLine();
      iCountLines = 0;
      while(sNextLine != null)
      {  
        iCountLines++;
        sNextLine = br.readLine();
      }  
    }
    catch(Exception exc)
    {
      System.err.println("READ ERROR: LatLongReader:getLines() -- " + exc);
      iCountLines = 0;
    }

    try { br.close();}
    catch(Exception exc)
    {
      System.err.println("CLOSE ERROR: LatLongReader:getLines() -- " + exc);
      iCountLines = 0;
    }

    if (iCountLines == 0)
      return(null);

      // allocate memory for words
      sWords = new String[iCountLines];

        // reopen file
    try { br = new BufferedReader(new FileReader(sInputFile)); }
    catch(Exception exc)
    {
      System.err.println("OPEN FILE ERROR: LatLongReader:getLines() -- " + exc);
      return(null);
    }
   
        // read in words
    try
    {  
      sNextLine = br.readLine();
      iCountLines = 0;
      while(sNextLine != null)
      {
        sWords[iCountLines++] = sNextLine;
        sNextLine = br.readLine();
      }
    }
    catch(Exception exc)
    {
      System.err.println("READ ERROR: LatLongReader:getLines() -- " + exc);
      sWords = null;
    }
 
    try { br.close();}
    catch(Exception exc)
    {
      System.err.println("ERROR: LatLongReader:getLines() -- " + exc);
    }
 
    return(sWords);
  }

    
}
