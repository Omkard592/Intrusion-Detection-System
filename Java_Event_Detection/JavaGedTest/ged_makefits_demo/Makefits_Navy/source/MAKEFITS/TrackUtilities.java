package MAKEFITS;
import java.io.*;
import java.util.*;
public class TrackUtilities {

/** debug purpose */
  static boolean debug = DebugHelper.isDebugFlagTrue("debug");


  public static Vector readInputData(String sFilename){
    Vector vEvents = new Vector();
    BufferedReader br;
    String sNextLine;
    // OPEN THE FILE
    try { br = new BufferedReader(new FileReader(sFilename));}

    catch(IOException ioe){
      System.err.println("OPEN ERROR: DataFeed:getLines() -- error opening file = " + sFilename + " -- " + ioe);
      return(null);
    }

    // READ IN THE LINES
    try{  sNextLine = br.readLine();
      while(sNextLine != null){
        vEvents.addElement(sNextLine);
        sNextLine = br.readLine();
      }
    }
    catch(Exception exc){
      System.err.println("READ ERROR: DataFeed:getLines() -- error reading file = " + sFilename + " -- " + exc);     vEvents = null;
    }

    // CLOSE THE FILE
    try {br.close();}
    catch(IOException ioe){
      System.err.println("CLOSE ERROR: DataFeed:getLines() -- error opening file = " + sFilename + " -- " + ioe);
    }

    // RETURN THE VECTOR
    return(vEvents);
  }



 /**
  * The method gets the setup data from the file and
  * keeps them into static constant variable
  *
  */
  public static boolean  readConfig(Vector setupData){
    StringTokenizer st ;
    String textLine;
    for(int jj = 0; jj < setupData.size(); jj++){
        if(debug)
      System.out.println(jj+"at line");
      textLine = (String) setupData.elementAt(jj);
      if (textLine.length () > 0){
        st = new StringTokenizer(textLine);
        String attribute = st.nextToken ()  ;

        if(debug)
          System.out.println("Setting the intial value ");
        if(attribute.equals ("YEAR")){
          st.nextToken();
          Constant.YEAR = st.nextToken();
              if(debug)
          System.out.println("YEAR = "+Constant.YEAR);
        }
        else if(attribute.equals ("SHOW_TRACK_EVERY_X_MINS")){
          st.nextToken();
          String time = st.nextToken ();
          while(st.hasMoreTokens ()){
            time = time +" "+st.nextToken ()   ;
          }
          Constant.SHOW_TRACK_EVERY_X_MINS = time;
              if(debug)
          System.out.println("Show track every "+Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS);
        }
        else if(attribute.equals ("NO_CHANGE_IN_LOCATION_FOR_X_MINS")){
          st.nextToken();
          String time = st.nextToken ();
          while(st.hasMoreTokens ()){
            time = time +" "+st.nextToken ()   ;
          }
          Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS = time;
              if(debug)
          System.out.println("No change in location for "+    Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS);
        }
        else if(attribute.equals ("AFTER_TRACK_ENTER_X_MINS")){
          st.nextToken();
          String time = st.nextToken ();
          while(st.hasMoreTokens ()){
            time = time +" "+st.nextToken ()   ;
          }
          Constant.AFTER_TRACK_ENTER_X_MINS = time;
              if(debug)
          System.out.println("After track enter  = "+    Constant.AFTER_TRACK_ENTER_X_MINS);
        }
      }
    }
    return true;
  }

    public static boolean writeLines(Vector vEvents, String fileName){
    boolean bSuccess = true;

    // open printwriter
    PrintWriter pw;
    try{
      pw = new PrintWriter(new FileWriter(fileName));
    }
    catch(Exception exc){
      System.err.println("OpenError: DataFeed: writeLines()-- error opening file "+fileName+" -- "+exc);
      return false;
    }
    // write vector
    try{
      for(int jj = 0; jj < vEvents.size(); jj++){
        pw.println((String)vEvents.elementAt(jj));
      }
    }
    catch(Exception exc){
      System.err.println("Write Error: DataFeed: writeLines()-- error writing to file "+fileName+" -- "+exc);
    bSuccess = false;}
    // close file
    try { pw.close();}
    catch(Exception exc){
      System.err.println("Close Error: DataFeed: writeLines()-- error writing to file "+fileName+" -- "+exc);
      bSuccess = false;
    }
    return(bSuccess);
  }

  // added by wt
  // rational : To parse the input data according to the format

  /**
   * @param inputLne  A line of input data e.g.  the command line arguments
   *                  POS/010000Z6/JAN/3300N6/12033W9  or
   *                  CTC/T7001/UNEQUATED-T7002///UNK//////00///20/20/2001/NCT758493000
   * @return arrayOfAttributes  attributes of CTC or POS
   */

  public static String[] parseInput(String inputLne){

    String inputLine = inputLne;
    String CTCAttri[] = new String[20];
    String POSAttri[] = new String[17];
    int attriIndex = 0;
    String inputType = ""; // CTC or POS

    if((inputLine.substring(0,3)).equals("CTC")){
      inputType = "CTC";
    }
    else if((inputLine.substring(0,3)).equals("POS")){
      inputType = "POS";
    }

    int i = 0;
    for(int j = 0 ; j < inputLine.length(); j++){
      if(inputLine.charAt(j) == '/' &&  (inputLine.charAt(j-1) != '/')){
        String tmp;
        if(attriIndex  != 0)
          tmp = inputLine.substring(i+1,j);
        else
          tmp = inputLine.substring(i,j);

        i = j;
        if(inputType.equals("CTC"))
          CTCAttri[attriIndex] = tmp;
        else
          POSAttri[attriIndex] = tmp;

        attriIndex++;
      }
      if(inputLine.charAt(j) == '/' &&  (inputLine.charAt(j-1) == '/')){
        i=j;
      }
    }
    String last = "";
    if((inputLine.charAt (inputLine.length()-1)) != '/'){
      last = inputLine.substring(i+1);
      if(inputType.equals("CTC"))
        CTCAttri[attriIndex] = last;
      else
        POSAttri[attriIndex] = last;
    }
    if(inputType.equals("CTC"))
      return CTCAttri;
    else
      return POSAttri;
  }


  /*
 public static void showLinesGui(String fileName){
    // set up gui
    Frame guiFrame = new Frame();
    guiFrame.setLayout(new BorderLayout());
    Panel guiPanel = new Panel(new BorderLayout());
    TextArea guiTA = new TextArea();
    guiFrame.setTitle("Bob's GUI");
    guiFrame.add(guiPanel, BorderLayout.CENTER);
    guiPanel.add(guiTA, BorderLayout.CENTER);
    // guiTA.appendText("This is a test");
    // get text to be displayed

    Vector linesVector = TrackUtilities.readInputData(fileName);
    for(int jj = 0; jj < linesVector.size();jj++){
      if(debug)
      System.out.println((String)linesVector.elementAt(jj));
      guiTA.appendText((String)linesVector.elementAt(jj));
    }
    guiFrame.setBounds(100, 100, 200, 200);
    // Display frame
    guiFrame.show();
  }

  */
  public static String[] getLinesAsArray(String sFilename){
    Vector vEvents = TrackUtilities.readInputData(sFilename);
    if(vEvents == null)
      return null;
    String[] sLines = new String[vEvents.size()];
    for(int ii = 0; ii < vEvents.size(); ii++){
      sLines[ii] = (String)vEvents.elementAt(ii);
    }
    return sLines;
  }



  public static void showLines(Vector vEvents){
    String sNextLine;
    for (int jj=0;jj<vEvents.size();jj++){
      sNextLine = (String) vEvents.elementAt(jj);
      if(debug)
        System.out.println("vEvents[" + jj + "] = " + sNextLine);
    }
  }




  public static String printTimeValue(String time){
    String displayValue = "";

    StringTokenizer st = new StringTokenizer(time);
    Integer hrsValue = new Integer(st.nextToken ());


    if(hrsValue.intValue () != 0){
      displayValue = displayValue + hrsValue.intValue();
      displayValue = displayValue +" hours";

    }
    st.nextToken ();
    Integer minValue = new Integer(st.nextToken ());
    if(minValue.intValue() != 0){
      displayValue =  displayValue + minValue.intValue ();
      displayValue = displayValue +" minutes";

    }
          st.nextToken ();
    Integer secValue = new Integer(st.nextToken ());

    if(secValue.intValue() != 0){
      displayValue = displayValue + secValue.intValue();
      displayValue = displayValue +" seconds";

    }


    return      displayValue;
  }

  static public void sleep(long millis){

    try { Thread.sleep(millis);  }
    catch (InterruptedException e) {
      e.printStackTrace();
      System.out.println("\nInterruptedExeception caught in sleep");
    }
  }



  public static String monValue(String mo){
    if(mo.equals ("JAN"))
    mo = "01";
    else if(mo.equals ("FEB"))
    mo = "02";
    else if(mo.equals ("MAR"))
    mo = "03";
    else if(mo.equals ("APR"))
    mo = "04";
    else if(mo.equals ("MAY"))
    mo = "05";
    else if(mo.equals ("JUN"))
    mo = "06";
    else if(mo.equals ("JUL"))
    mo = "07";
    else if(mo.equals ("AUG"))
    mo = "08";
    else if(mo.equals ("SEP"))
    mo = "09";
    else if(mo.equals ("OCT"))
    mo = "10";
    else if(mo.equals ("NOV"))
    mo = "11";
    else if (mo.equals ("DEC"))
    mo = "12";

    return mo;
  }

  static  public String getTimeStamp(){
    Calendar calendar = new GregorianCalendar();
    Date Time = new Date();
    calendar.setTime(Time);

    // print out a bunch of interesting things
    String timeStamp = +calendar.get(Calendar.HOUR_OF_DAY) +":"+ calendar.get(Calendar.MINUTE) +":"+calendar.get(Calendar.SECOND);
    timeStamp = timeStamp +" "+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.DATE)+"/"+calendar.get(Calendar.YEAR);
    //System.out.println(timeStamp);
    return timeStamp;
  }
}





