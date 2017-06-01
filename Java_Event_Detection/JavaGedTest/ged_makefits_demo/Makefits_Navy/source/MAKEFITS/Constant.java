package MAKEFITS;

import java.io.*;


public class Constant{


  public static String YEAR = "2000";            // setting year


  // RULE 4 Show track-X status every Y minutes
  // eg. to show track every 5 seconds

  public static String SHOW_TRACK_EVERY_X_MINS ;

  // RULE 5   No change in location of track-Z for X minutes

  public static String NO_CHANGE_IN_LOCATION_FOR_X_MINS ;

  // RULE 11 Notify analyst of Track-Y 2 minutes after it enters the Area of Interest

  public static String AFTER_TRACK_ENTER_X_MINS ;


  public static PrintWriter othGoldOutput = null;
  public static PrintWriter alertOutput = null;
   // BufferedWriter = br;

  static  {
    try {
      othGoldOutput   = new PrintWriter(new BufferedWriter(new FileWriter("MAKEFITS/OTH_Gold_Output.txt")));
      alertOutput   = new PrintWriter(new BufferedWriter(new FileWriter("MAKEFITS/Alert_Output.txt")));
    }
    catch(IOException ioe){

       System.err.println("ERROR: can't create the output files" );

    }
  }
}
