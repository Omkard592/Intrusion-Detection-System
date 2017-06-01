/**
 * TimeItem.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */
package sentinel.led;
import java.util.*;
import java.text.SimpleDateFormat;

/** The TimeItem class is used to store a time value appearing in an absolute or
 *  a relative temporal event. It contains methods to parse the absolute and relative
 *  time strings and convert them into their numerical equivalent in milliseconds.
 *  The times supported are to the precision of seconds.
 *  For absolute time strings with wild cards (?,*), the time item has to be updated
 *  for each value of the wild card field. The TimeItem class also contains data
 *  structures and methods to accomodate the updates to be made to absolute time
 *  strings. Relative time strings cannot be specified with wild cards.
 */

class TimeItem {

  static SimpleDateFormat sdfAbs;
  static SimpleDateFormat sdfRel;

  private static boolean timerDebug = Utilities.isDebugFlagTrue("timerDebug");

  /** Types of time strings
  */
  static final int SINGULAR = 1;		// Absolute time string with no wild cards
  static final int RELATIVE = 2;		// Relative time string
  static final int REPETITIVE = 3;	// Absolute time string with wild cards.

  /** The order of the time fields in the array used to store the time fields
  */
  static final int SECS = 0;
  static final int MINS = 1;
  static final int HOURS = 2;
  static final int DAYS = 3;
  static final int MONTHS = 4;
  static final int YEARS = 5;

  /** The number of time fields */
  static final int NO_TIME_FIELDS = 6;

  /** This array stores the upper limit values that each time field can take
   */
  static int UPPER_LIMIT[] = {60, 60, 24, 32, 13, 10000};

  /** This array stores the lower limit values that each time field can take
   */
  static int LOWER_LIMIT[] = {0, 0, 0, 1, 1, 0};

  /** The values in these fields are used to roll over for wildcards like ?3
   *  These values represent the highest limit values taken by the left most
   *  wild cards.
   */
  static int QUEST_LIMIT[] = {60, 60, 20, 30, 10, 10000};

  /** The lowest increment value for the seconds unit.
   */
  static int GRANULARITY = 1;

  /** This array stores the upper limits for each time field
   */
  int[] myLimits = new int[NO_TIME_FIELDS];

  /** This array stores the increment values for each time field.
   *  For example, if a particular time field is ?2, the increment value is 10.
   *  For 1?, increment is 1, and for *, the increment is 1. For 30, the increment
   *  value is zero.
   */
  int[] increments = new int[NO_TIME_FIELDS];

  /** This array stores the information if a particular time field needs any more
   *  updates or not. Fields with wild cards need updates whereas fields with no
   *  wild cards do not need updates.
   */
  boolean[] updatesFinished = new boolean[NO_TIME_FIELDS];

  /** This array stores the question field limit for all fields. For example,
   *  if the days field contains ?1, the quest field limit for it would be 3 since
   *  there can only be 31 days possible and the value 41 for a day is invalid.
   */
  int[] questFieldLimit = new int[NO_TIME_FIELDS];

  /** This array stores the value of all the time fields in integers. Each of the
   *  time fields specified in the time string are converted into their equivalent
   *  integers in order to increment them with updated values.
   */
  int[] timeField = new int[NO_TIME_FIELDS];

  static final char STAR = '*';
  static final char QUEST = '?';
  static final char NIL = ' ';

  /** This variable stores if this time item requires any more updates or not.
   */
  boolean updatesRequired = false;

  /** This variable stores the offset value to be subtracted for relative temporal
   *  events. This value is determined by the time zone where the program is run.
   *  It is calculated by getting a Calendar instance and its ZONE_OFFSET and
   *  DST_OFFSET (day-light savings time) values.
   */

  static long zoneOffset;

  /** The SimpleDateFormat objects for absolute and relative time strings. These
   *  objects are used to parse the time strings to obtain their equivalent
   *  value in milliseconds since Jan 1, 1970.
   */
  static {
    sdfAbs = new SimpleDateFormat("hh:mm:ss/MM/dd/yyyy",Locale.US);
    sdfRel = new SimpleDateFormat("hh 'hrs' mm 'min' ss 'sec'",Locale.US);
    Calendar localZoneCalendar = Calendar.getInstance();

    // The day light savings time offset is not to be added to
    // to the zone offset. This is taken care of the by the
    // underlying clock. If temporal events give a problem,
    // check if the zoneOffset is being set correctly or not.
    zoneOffset = localZoneCalendar.get(Calendar.ZONE_OFFSET);
    // + localZoneCalendar.get(Calendar.DST_OFFSET);
    if (timerDebug)
      System.out.println("zone offset = " + zoneOffset);
    if (timerDebug) {
      System.out.println("ZONE_OFFSET = " + localZoneCalendar.get(Calendar.ZONE_OFFSET));
      System.out.println("DST_OFFSET = " + localZoneCalendar.get(Calendar.DST_OFFSET));
      System.out.println("zone offset = " + zoneOffset);
    }
  }

  private long millis;
  private long eventId;
  private int timeFormat;
  private Primitive temporalNode;
  private RuleScheduler ruleScheduler;

//  add 03/20
  private NotifyBuffer notifyBuffer;

  TimeItem next;

  TimeItem() { }

  TimeItem (Primitive temporalNode, String time, long eventId) {
    Date date = null;
    long now = 0;
    this.temporalNode = temporalNode;
    for (int i=0; i<NO_TIME_FIELDS; i++) {
      increments[i] = 0;
      questFieldLimit[i] = 0;
      timeField[i] = 0;
      updatesFinished[i] = true;
    }
    this.timeFormat = getTimeFormat(time);
    if (timerDebug)
      System.out.println("timeFormat = " + timeFormat);

    switch (timeFormat) {
      case SINGULAR:
        try {
          date = sdfAbs.parse(time);
        }
        catch (java.text.ParseException pe) {
	  pe.printStackTrace();
	  return;
          // throw new IllegalTimeFormat(time);
        }
	now = 0;
	break;
      case RELATIVE:
        try {
          date = sdfRel.parse(time);
        }
        catch (java.text.ParseException pe) {
          pe.printStackTrace();
          return;
        // throw new IllegalTimeFormat(time);
        }
        //now = System.currentTimeMillis() - 18000000;
        now = System.currentTimeMillis() + zoneOffset;
        break;
      case REPETITIVE:
       date = this.getCurrentDate(time);
       now = 0;
       updatesRequired = true;

       break;
    }
    this.millis = date.getTime() + now;
    if (timerDebug)
      System.out.println("Time value in millis: " + millis);
    this.eventId = eventId;
    this.next = null;
  }

  boolean isRepetitive() {
    if (this.timeFormat == REPETITIVE)
      return true;
    else
      return false;
  }

  void setScheduler(RuleScheduler ruleScheduler) {
    this.ruleScheduler = ruleScheduler;
  }

  private int getTimeFormat(String time) {
    char ch = time.charAt(time.length()-1);
    if (time.indexOf('?') != -1 || time.indexOf('*') != -1)
      return REPETITIVE;
    else if(Character.isLetter(ch))
      return RELATIVE;
    else
      return SINGULAR;
  }

  /** This method returns the date object representing a time value for the
   *  given time string.
   */
  Date getCurrentDate(String timeStr) {
    Date date = null;
    for (int i=0; i<NO_TIME_FIELDS; i++)
      myLimits[i] = UPPER_LIMIT[i];
    int startIndex = 0;
    startIndex = assignFieldValue(timeStr,HOURS,startIndex);
    startIndex = assignFieldValue(timeStr,MINS,startIndex);
    startIndex = assignFieldValue(timeStr,SECS,startIndex);
    startIndex = assignFieldValue(timeStr,MONTHS,startIndex);
    startIndex = assignFieldValue(timeStr,DAYS,startIndex);
    startIndex = assignFieldValue(timeStr,YEARS,startIndex);
    date = convertToDate();
    return date;
  }

  /** This method parses a particular field in the time string and converts
   *  the field into its integer equivalent.
   */
  int assignFieldValue(String timeStr, int fieldPos, int startIndex) {
    int start = startIndex;
    String fieldStr;
    int endIndex;
    endIndex = timeStr.indexOf(':',start);
    if (endIndex == -1)
      endIndex = timeStr.indexOf('/',start);
    if (endIndex == -1)
      endIndex = timeStr.length();

    //System.out.println("endIndex = " + endIndex);
    fieldStr = timeStr.substring(start,endIndex);
    start = endIndex + 1;
    timeField[fieldPos] = convertUnit(fieldStr,fieldPos);
    if (timerDebug)
      System.out.println("timeField[" + fieldPos +"] = " + timeField[fieldPos]);
    return start;
  }

  /** This method takes a particular field string and if there are wild cards
   *  in that field, it substitutes the wild card with its lowest
   *  possible value.
   */
  int convertUnit(String fieldStr, int fieldPos) {
    int length = fieldStr.length();
    char wildCard;

    if (fieldStr.indexOf('*') != -1)
      wildCard = STAR;
    else if(fieldStr.indexOf('?') != -1) {
      boolean starFlag = true;
      for (int i=0; i<length; i++) {
	if (fieldStr.charAt(i) != QUEST) {
	  starFlag = false;
	  break;
	}
      }
      if (starFlag == true)
	wildCard = STAR;
      else
	wildCard = QUEST;
    }
    else
      wildCard = NIL;

      if (timerDebug)
        System.out.println("wildCard = " + wildCard);
      int returnValue = -1;
      int wildCardPos = -1;

      if (wildCard == NIL)
        returnValue = Integer.parseInt(fieldStr);
      else if(wildCard == QUEST) {
        updatesFinished[fieldPos] = false;

      for(int i=0; i<fieldStr.length(); i++) {
        if(fieldStr.charAt(i) == QUEST) {
          wildCardPos = i;
          break;
        }
      }

      String integerPortion;
      switch(wildCardPos) {
        case 0:					// form ?3
          integerPortion = fieldStr.substring(1,2);
          returnValue = Integer.parseInt(integerPortion);
          increments[fieldPos] = 10;
          setQuestFieldLimits(fieldPos);
          break;
        case 1:					// form 3?
          integerPortion = fieldStr.substring(0,1);
          returnValue = 10 * Integer.parseInt(integerPortion) +
          LOWER_LIMIT[fieldPos];
          increments[fieldPos] = 1;
          myLimits[fieldPos] = returnValue + 10;
          break;

        default:
          if (timerDebug)
            System.out.println("Error in convertUnit() with ? option");
      }
    }
    else if (wildCard == STAR) {
      updatesFinished[fieldPos] = false;
      increments[fieldPos] = 1;
      if (fieldPos == YEARS)
	returnValue = getCurrentYear();
      else
	returnValue = LOWER_LIMIT[fieldPos];
    }

    if (fieldPos == SECS && increments[fieldPos] < GRANULARITY)
      increments[fieldPos] = GRANULARITY;
    return returnValue;
  }

  /** This method returns the current year. If a wild card is specified in the
   *  year field, it is substituted with the current year value and not 0.
   */
  int getCurrentYear() {
    GregorianCalendar cal = new GregorianCalendar();
    int currYear = cal.get(Calendar.YEAR);
    if (timerDebug)
      System.out.println("Current year = " + currYear);
    return currYear;
  }

  /** The limits for the wild card fields are set depending on the values in
   *  other fields. For example, if the month is feb, the number of days would be
   *  28 and the number of days would be different for other months.
   */
  void setQuestFieldLimits(int fieldPos) {
    int limit;
    if (fieldPos == DAYS && myLimits[fieldPos] == UPPER_LIMIT[fieldPos])
      limit = daysInMonth(timeField[MONTHS]);
    else
      limit = myLimits[fieldPos];
    questFieldLimit[fieldPos] = QUEST_LIMIT[fieldPos];
    switch(fieldPos) {
      case SECS:
	break;
      case MINS:
	break;
      case HOURS:
	questFieldLimit[HOURS] = 10;
	break;
      case DAYS:
	if(limit >= 30)
	  questFieldLimit[DAYS] = 30;
	else
	  questFieldLimit[DAYS] = 20;
	break;
      case MONTHS:
	questFieldLimit[MONTHS] = 10;
	break;
      case YEARS:
	break;
      }
    }

  /** This method returns the number of days in the given month.
   */
  int daysInMonth(int month) {
    int limit = UPPER_LIMIT[DAYS];
    switch (month) {
      case Months.APR:
      case Months.JUN:
      case Months.SEP:
      case Months.NOV:
	limit--;
	break;
      case Months.FEB:
	limit = limit-3;
        if (isLeapYear(timeField[YEARS]))
	  limit++;
	break;
    }
    return limit;
  }

  /** The method returns true if the given year is a leap year.
   */
  boolean isLeapYear(int year) {
    GregorianCalendar cal = new GregorianCalendar();
    if (cal.isLeapYear(year))
      return true;
    else
      return false;
  }

  /** All the integer time fields in the timeField[] array are converted to
   *  strings and the string is parsed to get the corresponding date object.
   */
  Date convertToDate() {
    String dateString = new String();
    Date date = null;
    dateString = Integer.toString(timeField[HOURS]) + ":" +
    Integer.toString(timeField[MINS]) + ":" +
    Integer.toString(timeField[SECS]) + "/" +
    Integer.toString(timeField[MONTHS]) + "/" +
    Integer.toString(timeField[DAYS]) + "/" +
    Integer.toString(timeField[YEARS]);
    if (timerDebug)
      System.out.println("dateString = " + dateString);
    try {
      date = sdfAbs.parse(dateString);
    }
    catch (java.text.ParseException pe) {
      pe.printStackTrace();
	// throw new IllegalTimeFormat(time);
    }
    return date;
  }

  long getMillis() {
     return millis;
  }

  long getEventId() {
    return eventId;
  }

  /** This method updates an absolute time event with wild cards to the next
   *  updated time.
   */
  boolean process() {
    if (timerDebug)
      System.out.println("Processing timeItem " + this.millis +
					" at " + System.currentTimeMillis());
    computeNext();
    return updatesRequired;
  }

  /** This method notifies the occurrence of a temporal event to the corresponding
   *  temporal event node.
   */
  void notifyEvent() {
    if (timerDebug)
      System.out.println("in notifyEvent");

    ParameterList paramList	= new ParameterList();
    //paramList.setTS(eventId);
    paramList.setTS();
    paramList.setMethodSignature(temporalNode.getMethodSignature());
    Vector instanceRuleList = temporalNode.getInstanceRuleList();
    InstanceRules instanceRules = (InstanceRules) instanceRuleList.firstElement();
    Object monitoringObject = instanceRules.getInstance();
    paramList.insertObject("monitoringObject",monitoringObject);
    if (timerDebug)
      System.out.println("eventId in TimeItem = " + eventId);

    paramList.insertLong("eventId",eventId);
    //temporalNode.notify(paramList, eventId);

    if(!Constant.RSCHEDULERFLAG){
      if (this.timeFormat == RELATIVE){
        NotifyObject notifyObj = new NotifyObject(temporalNode,paramList,Thread.currentThread(),this.ruleScheduler);
        ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
        notifyBuffer.put(notifyObj);
      }else{
            // For the absolute temporal event
        paramList.setEventInstance(temporalNode.getTemporalEventInstance());
        NotifyObject notifyObj = new NotifyObject(temporalNode,paramList,Thread.currentThread(),this.ruleScheduler);
        ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
        notifyBuffer.put(notifyObj);
      }
    }else{

      // modified by wtanpisu
      // rational : put the triggered temporal event into the notify buffer.
      if (this.timeFormat == RELATIVE){

      //	System.out.println("relative");
        NotifyObject notifyObj = new NotifyObject(temporalNode,paramList,Thread.currentThread(),this.ruleScheduler);
        ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();

        if (notifyObj.waitFlag == true){
          //System.out.println(Thread.currentThread().getName()+" waits for its imm child");

          // wait for inserting all the associated rules in the queue
	      // before start waiting for immediate child
          synchronized (notifyObj){
	        try{
              notifyBuffer.put(notifyObj);
	          notifyObj.wait();
    	    }
	        catch(InterruptedException e){}
	      }
          // Do have to wait for the childs
        }
     // If it's parallel mode, put the notify object into the buffer and continue working
        else{
	      notifyBuffer.put(notifyObj);
        }
      }
      else {
        // For the absolute temporal event
        System.out.println("Absolute");

        paramList.setEventInstance(temporalNode.getTemporalEventInstance());
        NotifyObject notifyObj = new NotifyObject(temporalNode,paramList,Thread.currentThread(),this.ruleScheduler);
        ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();

        if (notifyObj.waitFlag == true){
	      //	 System.out.println(Thread.currentThread().getName()+" waits for its imm child");

        // wait for inserting all the associated rules in the queue
	    // before start waiting for immediate child
	      synchronized (notifyObj){
	      try{
            notifyBuffer.put(notifyObj);
	        notifyObj.wait();
	      }
	      catch(InterruptedException e){}
	    }
        // Do have to wait for the childs
      }
      // If it's parallel mode, put the notify object into the buffer and continue working
      else{
        notifyBuffer.put(notifyObj);
      }
    }
    // modified by wtanpisu
    // rational : put the triggered temporal event into the notify buffer.
    }
  }

  // modified by wtanpisu on 03/31/2000
  void computeNext() {
    if (updatesRequired)
      updateTimeItem();
  }

  boolean requiresUpdate() {
    if (updatesRequired)
      return true;
    else
      return false;
  }

  /** This method updates the absolute time item with wild card.
   */
  void updateTimeItem() {
    boolean updated = false;
    int i=0;
    while (!updated && i < NO_TIME_FIELDS) {
      if (updatesFinished[i] == false)
	updated = updateUnit(i);
      i++;
    }
    Date date = convertToDate();
    this.millis = date.getTime();
  }

  /** This method updates a particular field of the time string.
   */
  boolean updateUnit(int fieldPos) {
    int limit;
    boolean updated = true;
    int tempValue;
    if (fieldPos == DAYS && myLimits[fieldPos] == UPPER_LIMIT[fieldPos])
      limit = daysInMonth(timeField[MONTHS]);
    else
      limit = myLimits[fieldPos];

    tempValue = timeField[fieldPos] + increments[fieldPos];
    if (tempValue >= limit) {
      if (questFieldLimit[fieldPos] == 0)
	tempValue = (tempValue % limit) + LOWER_LIMIT[fieldPos];
      else
	tempValue = tempValue % questFieldLimit[fieldPos];
      updated = false;
    }
    timeField[fieldPos] = tempValue;
    tempValue = tempValue + increments[fieldPos];
    if (fieldPos == highestWildCardPos() && tempValue >= limit) {
      updatesFinished[fieldPos] = true;
      if (lastUpdate())
        updatesRequired = false;
    }
    if (timerDebug)
      System.out.println("timeField[" + fieldPos + "] = " + timeField[fieldPos]);
    return updated;
  }

  /** This method returns the highest field that contains a wild card. The wild
   *  cards are first substituted for the seconds, and then in the order of
   *  minutes, hours, days, months and years.
   */
  int highestWildCardPos() {
    if (updatesFinished[YEARS] == false)
      return YEARS;
    else if (updatesFinished[MONTHS] == false)
      return MONTHS;
    else if (updatesFinished[DAYS] == false)
      return DAYS;
    else if (updatesFinished[HOURS] == false)
      return HOURS;
    else if (updatesFinished[MINS] == false)
      return MINS;
    else
      return SECS;
  }

  /** This method returns the field for which the last update has been done.
   */
  boolean lastUpdate() {
    boolean last = true;
    for (int i=0; i<NO_TIME_FIELDS; i++) {
      if (updatesFinished[i] == false)
	return false;
    }
    return last;
  }

  void print() {
    if (timerDebug){
      System.out.print("\nTime : " + millis);
    System.out.print(" eventId : " + eventId);
    //System.out.print("timeField contents: ");
    System.out.print("\nHOURS = " + timeField[HOURS]);
    System.out.print("MINS = " + timeField[MINS]);
    System.out.print("SECS = " + timeField[SECS]);
    System.out.print("MONTHS = " + timeField[MONTHS]);
    System.out.print("DAYS = " + timeField[DAYS]);
    System.out.print("YEARS = " + timeField[YEARS]);

    System.out.print("\nincrements[] = ");
    for (int i=0; i<NO_TIME_FIELDS; i++)
      System.out.print(increments[i] + "\t");

    System.out.print("\nmyLimits[] = ");
    for (int i=0; i<NO_TIME_FIELDS; i++)
      System.out.print(myLimits[i] + "\t");
      System.out.print("\nquestFieldLimit[] = ");
      for (int i=0; i<NO_TIME_FIELDS; i++)
         System.out.print(questFieldLimit[i] + "\t");
      System.out.print("\nupdatesFinished[] = ");
      for (int i=0; i<NO_TIME_FIELDS; i++)
	System.out.print(updatesFinished[i] + "\t");
    }
  }

  //### add 03/20
  void setNotifyBuffer( NotifyBuffer notifyBuffer){
    this.notifyBuffer = notifyBuffer;
  }
  // addy 03/20

    public static void main(String[] args) throws java.text.ParseException {

	     // date format: MM/dd/yyyy/hh:mm:ss

       /*String str1 = "05/14/1999/10:00:00";
       String str2 = "05/21/1999/12:43:00";
       TimeItem tiOne = new TimeItem(str1,0);
       TimeItem tiTwo = new TimeItem(str2,0);
       System.out.println("\nTimeItem One = " + str1 + " " + tiOne.getMillis());
       System.out.println("\nTimeItem Two = " + str2 + " " + tiTwo.getMillis());
       System.out.println("\nDifference in milliseconds = " + (tiTwo.getMillis() -
							tiOne.getMillis()) + "\n");

       String str3 = "1 hrs 2 min 45 sec";
       TimeItem tiThree = new TimeItem(str3,0);
       System.out.println("\nThree - Two = " + (tiThree.getMillis() -
							tiTwo.getMillis()) + "\n");
       System.out.println("\nTimeItem Three = " + str3 + " " + tiThree.getMillis()); */
    }
}

