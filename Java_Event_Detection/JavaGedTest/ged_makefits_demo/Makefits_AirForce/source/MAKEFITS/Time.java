/**
 * Title:        Your Product Name
 * Version:
 * Copyright:    Copyright (c) 1999
 * Author:       Weera Tanpisuth
 * Company:      University of Texas, Arlington
 * Description:  Your description
 */


package MAKEFITS;
import java.util.TimeZone ;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

public class Time {

  private static boolean timeDebug = DebugHelper.isDebugFlagTrue("timeDebug");

  private long millis;

  // static  SimpleDateFormat	sdfAbs = new SimpleDateFormat("hh:mm:ss/MM/dd/yyyy zzz",Locale.US);
  static  SimpleDateFormat	sdfAbs = new SimpleDateFormat("hh:mm:ss/MM/dd/yyyy",Locale.US);

  Time (String time) {
    Date date = null;
    long now = 0;

    try {
      date = sdfAbs.parse(time);
    }
    catch (java.text.ParseException pe) {
      pe.printStackTrace();
      return;
      // throw new IllegalTimeFormat(time);
    }
    this.millis = date.getTime() + now;
  }

  public long getMillis() {
    return millis;
  }


 /* public static void main(String[] args) {
    Tester tester = new Tester();
    tester.invokedStandalone = true;
    Time t = new Time("0:23:32/01/01/2000 PST");
    Time t1 = new Time("0:23:32/01/01/2000 GMT");
    long d =    t1.getMillis ()-  t.getMillis ()    ;

    System.out.println(t.getMillis ());
    System.out.println(t1.getMillis ());
    System.out.print("diff "+d/(60*60*1000));
  }
   */
}