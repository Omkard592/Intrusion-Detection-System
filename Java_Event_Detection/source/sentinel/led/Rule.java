/**
 * Rule.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.reflect.*;


/**  The Rule class contains all the attributes of an ECA rule. This class
  *  is instantiated to form Rule objects that are processed when the
  *  associated Event occurs.
  */

public class Rule implements Executable {

  static boolean ruleSchedulerDebug = DebuggingHelper.isDebugFlagTrue("ruleSchedulerDebug");
  static boolean eventDetectionDebug = DebuggingHelper.isDebugFlagTrue("eventDetectionDebug");
  static boolean mainDebug = DebuggingHelper.isDebugFlagTrue("mainDebug");



  /** The name of the rule */
  String ruleName;

  /** The context in which the associated event is detected */
  ParamContext context = ParamContext.RECENT;

  /** The coupling mode of the rule indicates the time when the rule
    * is executed
    */
  CouplingMode coupling = CouplingMode.IMMEDIATE;

  /** The triggering mode of the rule determines the event occurences
    * used for detecting the event
    */
  TriggerMode triggerMode = TriggerMode.NOW;

  /** The priority of the Rule */
  int priority = 1;

  /** This flag indicates whether the rule is disabled or not */
  boolean disabled = false;

  /** Reference to the event node in the event graph that is associated
    * with this rule
    */
  Event eventNode;

  /** This denotes the value of the event occurrence counter(declared in TimeStamp
    *  class) when the rule is created
    */
  long sequenceNo;

  /** Reference to the condition method of the Rule */
  private Method condMethod;

  /** Reference to the action method of the Rule */
  private Method actionMethod;

  /** Events are detected only if the detectFlag is true. The detectFlag is set
   *  to false when the rule is executing the condition method. This is because a
   *  condition is supposed to be side-effect free, i.e., it does not trigger any
   *  events.
   */
  private boolean detectFlag = true;

  private String condName;

  private String actionName;



  // added by seyang on 24 July 99

  /** condition code which will override the condMethod */

  private Condition condition;

  /** action code which will override the actionMethod  */

  private Action action;

  // added by seyang on 24 July 99
  /** Parameters passed to the condition and action methods */
  private Object[] methodParams;

  /** The instance over which the Condition and Action methods are invoked */
  private Object targetInstance = null;



  private RuleScheduler ruleScheduler;


  /** Constructs a new Rule object with the default context, coupling,
   * trigger mode and priority
   */
  Rule(String rule, EventHandle eventHandle, Method condMethod,
    Method actionMethod) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
      Method actionMethod , Object targetInstance) {
      initializeRule(rule, eventHandle, condMethod, actionMethod);
      this.targetInstance = targetInstance;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
	    Method actionMethod, int priority) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.priority = priority;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
      Method actionMethod , int priority, Object targetInstance) {
      initializeRule(rule, eventHandle, condMethod, actionMethod);
      this.priority = priority;
      this.targetInstance = targetInstance;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
    Method actionMethod, int priority, CouplingMode coupling) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.priority = priority;
    this.coupling = coupling;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
    Method actionMethod, int priority, CouplingMode coupling, Object targetInstance) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.priority = priority;
    this.coupling = coupling;
    this.targetInstance = targetInstance;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
    Method actionMethod, int priority, CouplingMode coupling, ParamContext context) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.context = context;
    this.coupling = coupling;
    this.priority = priority;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
    Method actionMethod, int priority, CouplingMode coupling, ParamContext context, Object targetInstance) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.context = context;
    this.coupling = coupling;
    this.priority = priority;
    this.targetInstance = targetInstance;
  }



  Rule(String rule, EventHandle eventHandle, Method condMethod,
  		Method actionMethod, int priority, CouplingMode coupling,
    ParamContext context,TriggerMode triggerMode) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.context = context;
    this.coupling = coupling;
    this.triggerMode = triggerMode;
    this.priority = priority;
  }

  Rule(String rule, EventHandle eventHandle, Method condMethod,
  		Method actionMethod, int priority, CouplingMode coupling,
    ParamContext context,TriggerMode triggerMode, Object targetInstance) {
    initializeRule(rule, eventHandle, condMethod, actionMethod);
    this.context = context;
    this.coupling = coupling;
    this.triggerMode = triggerMode;
    this.priority = priority;
    this.targetInstance = targetInstance;
  }

  /** This will create a rule with Condition and Action interface object
   *  added by seyang on 24 July 99
   */
  // modified by wtanpisu
  // what : add ruleName parameter
  public Rule(String ruleName,Reactive reactive, EventHandle eventHandle,
      Condition condition,Action action) {
    // modified by wtanpisu on 03/31/2000
    this.ruleName = ruleName;
    this.targetInstance = reactive;
    this.condition = condition;
    this.action = action;
  }

  private void initializeRule(String rule, EventHandle eventHandle,
      Method condMethod, Method actionMethod) {
    this.ruleName = rule;
    this.condMethod = condMethod;
    this.actionMethod = actionMethod;
    eventNode = eventHandle.getEventNode();

    //this.sequenceNo = TimeStamp.getSequenceCounter();
    this.sequenceNo = TimeStamp.getTime();

    if (eventNode == null) {
      System.out.println("Fatal ERROR : Rule with name " + rule +
	  "subscribes to the event" + eventHandle + "that is not registered yet");
      return;
    }
    return;
  }

  void setCondName(String condName) {
    this.condName = condName;
  }

  void setActionName(String actionName) {
    this.actionName = actionName;
  }

  void setRuleScheduler(RuleScheduler ruleScheduler) {
    this.ruleScheduler = ruleScheduler;
  }

  public int getPriority() {
    return priority;
  }

  public CouplingMode getCoupling() {
    return coupling;
  }

  public ParamContext getContext() {
    return context;
  }

  public String getName() {
    return ruleName;
  }

  Event getEventNode() {
    return eventNode;
  }

  boolean getDetectFlag() {
    return detectFlag;
  }

  void print() {
    if(ruleSchedulerDebug){
      System.out.print("Rule name : "+ruleName);
	   System.out.println("Priority :"+priority);
    }
  }

  void disable() {
    this.disabled = true;
  }

  void enable() {
    this.disabled = false;
  }



  /**  This method executes the Rule in the given context. Condition and action
   *  methods can use the parameters stored in the parameterLists structure. The
   *  rule is executed only if the rule is not disabled and the context of the
   *  rule matches with the given context. If the rule triggering mode is NOW,
   *  it is checked to see if the the time stamp of the rule is greater than the
   *  timestamps of all the constituent events for this event.
   */

   // modified by wtanpisu

  public void execute(ListOfParameterLists parameterLists, int context) {
    RuleThread ruleThread =null;
    if(mainDebug)
    	System.out.println("Executing the rule");

    if(Constant.RSCHEDULERFLAG){
     // if the rule is disabled, remove it from rule queue
      if (disabled){
        if( Thread.currentThread() instanceof RuleThread){
	      ruleThread = (RuleThread) Thread.currentThread();
	      ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
        }
        return;
      }

      if (this.context.getId() != context){
        if( Thread.currentThread() instanceof RuleThread){
          ruleThread = (RuleThread) Thread.currentThread();
          ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
        }
        return;
      }

      if (this.triggerMode == TriggerMode.NOW) {
        boolean validRule = isRuleTSGreaterThanAllEventTS(parameterLists);
        if (!validRule){
	      if( Thread.currentThread() instanceof RuleThread){
            ruleThread = (RuleThread) Thread.currentThread();
            ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
	      }
	      return;
        }
      }

      // added by seyang to override the rule on 24 July 99
      if((condition != null) && (action != null)) {
        if(targetInstance != null) {
          if(condition.check(targetInstance)) {
	        action.execute(targetInstance);
            if( Thread.currentThread() instanceof RuleThread){
  	          ruleThread = (RuleThread) Thread.currentThread();
  	          ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
            }
	        return;
          }
          else{
      	    if( Thread.currentThread() instanceof RuleThread){
      	      ruleThread = (RuleThread) Thread.currentThread();
      	      ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
            }
            return;
          }
        }
        else{
          System.out.println("Rule are Not Associated with any instance");
      	  return;
        }
      }



      methodParams = new Object[1];
      methodParams[0] = parameterLists;
      boolean status = false;
      this.detectFlag = false;
      if(mainDebug)
        System.out.println("Evaluate the condition");

      status = evaluateCondition(methodParams);
      this.detectFlag = true;
      if (status){
        if(mainDebug)
          System.out.println("Condition is true");
        executeAction(methodParams);
      }
      if( Thread.currentThread() instanceof RuleThread){
        if(ruleSchedulerDebug)
          System.out.println(" set the rule operation mode to be FINISHED");
        ruleThread = (RuleThread) Thread.currentThread();
        ruleThread.setOperatingMode(RuleOperatingMode.FINISHED);
      }
    }
    else{
      if (disabled)
        return;
      if (this.context.getId() != context)
        return;
		//System.out.println("rule seqNo = " + this.sequenceNo);
      if (this.triggerMode == TriggerMode.NOW) {
        boolean validRule = isRuleTSGreaterThanAllEventTS(parameterLists);
        if (!validRule)
          return;
      }

		// added by seyang to override the rule on 24 July 99
      if((condition != null) && (action != null)) {
	    if(targetInstance != null) {
		  if(condition.check(targetInstance)) {
		    action.execute(targetInstance);
			return;
          }
          else
            return;
		}
        else {
          System.out.println("Rule are Not Associated with any instance");
		  return;
		}
	  }

      methodParams = new Object[1];
      methodParams[0] = parameterLists;
      boolean status = false;
      this.detectFlag = false;
      status = evaluateCondition(methodParams);
      this.detectFlag = true;
      if (status){
        if(mainDebug)
          System.out.println("Condition is true");
        executeAction(methodParams);
      }
      else{
        if(mainDebug)
          System.out.println("Condition is FALSE");
      }
    }
  }

  // modified by wtanpisu on 24 Jan 2000



  /** This method checks if the time stamp of the rule is greater than the
   *  time stamp of all the constituent events of the event (primitive or composite)
   *  triggering this rule.
   */

  private boolean isRuleTSGreaterThanAllEventTS(ListOfParameterLists parameterLists) {
    Enumeration en = parameterLists.elements();
    ParameterList paramList;
    TimeStamp timeStamp;
    while(en.hasMoreElements()) {
      paramList = (ParameterList) en.nextElement();
      timeStamp = paramList.getTS();
    //			if(this.sequenceNo > timeStamp.getSequence())
      if(this.sequenceNo > timeStamp.getGlobalTick())
        return false;
    }
    return true;
  }


  /**
   * This method returns an instance of the class 'className'.
   */



  private Object getInstanceOfClass(String className) {
    Object instance = null;
    try {
      Class reflectedClass = Class.forName(className);
      if(eventDetectionDebug){
        System.out.println("create new instance for this class :"+className);

      }
      instance = reflectedClass.newInstance();
    }
    catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
    catch(IllegalAccessException iacce) {
      iacce.printStackTrace();
    }
    catch(InstantiationException ie) {
      ie.printStackTrace();
    }
    return instance;
  }



  /** This method evaluates the Condition method using the parameters in the
   * methodParams structure.
   */

  // modified by weera aug 15, 2000
  // ratonal :  to take care if the rule is defined in defferent class from
  //            the class of an event.

  private boolean evaluateCondition(Object[] methodParams) {

    boolean result = false;
    int i = condName.lastIndexOf(".");
    String condClass = condName.substring(0,i);

    if (ruleSchedulerDebug)
      System.out.println("Class of the condition method : "+condClass);

    ParameterList paramList = ((ListOfParameterLists)methodParams[0]).getParamListWithInstanceType(condClass);

    if (targetInstance != null){  // rule has spcified "target instance",a rule instance.
  if (ruleSchedulerDebug)
        System.out.println("targetInstance != null");

      // do nothing, target instance will be used to invoke declared conditon method

    }
    else{
        if (ruleSchedulerDebug)
          System.out.println("targetInstance == null");

      // rule with no rule instance, need to check whether the class of a rule
      // is different from the class of an event.
      Object eventObject;

      if (paramList == null){
        if (ruleSchedulerDebug)
          System.out.println("paramList == null");

        targetInstance = getInstanceOfClass(condClass);
      }
      else{
        eventObject = paramList.getEventInstance();
        String eventClass = eventObject.getClass().getName();


        // Check whether the class of a rule and the class of an event are the same
        if ( condClass.compareTo(eventClass) == 0){ // same
        if (ruleSchedulerDebug)
          System.out.println("condClass.compareTo(eventClass) == 0");



            targetInstance = eventObject;

        }
        else{ // not the same
          targetInstance = getInstanceOfClass(condClass);
        }
      }
   }

    try {
      result = ((Boolean) condMethod.invoke(targetInstance, methodParams)).booleanValue();
    }
    catch (IllegalAccessException iacce) {
      System.out.println("\tERROR! The Condition method is not " +
		 " accessible from this package.");
      System.out.println("\tThe Condition class and the Condition method may "+
		 "not have been declared public.");
      iacce.printStackTrace();
    }
    catch (IllegalArgumentException iarge) {
      System.out.println("\tERROR! Illegal arguments specified for the " +
		 "Condtion method.");
      System.out.println("\tThe Condition method should take only a single" +
		 " argument of type 'Sentinel.ListOfParameterLists'.");
      iarge.printStackTrace();
    }
    catch (InvocationTargetException ite) {
      ite.printStackTrace();
    }
    return result;
  }
  // end modified


  /** This method executes the Action method if the Condition method
    * returns true.
    */

  private void executeAction(Object[] methodParams) {
    int i = actionName.lastIndexOf(".");
    String actionClass = actionName.substring(0,i);
    ParameterList paramList = ((ListOfParameterLists)methodParams[0]).getParamListWithInstanceType(actionClass);


    // modified by weera aug 15, 2000
    // ratonal :  to take care if the rule is defined in defferent class from
    //            the class of an event.


    if (ruleSchedulerDebug)
      System.out.println("Class of the action method : "+actionClass);

    if (targetInstance != null){  // rule has spcified "target instance",a rule instance.

      if (ruleSchedulerDebug)
        System.out.println("targetInstance != null");

      // do nothing, target instance will be used to invoke declared conditon method

    }
    else{
      // rule with no rule instance, need to check whether the class of a rule
      // is different from the class of an event.

      Object eventObject;
      //Object eventObject = paramList.getEventInstance();

      if (paramList == null){
        if (ruleSchedulerDebug)
          System.out.println("paramList == null");

        targetInstance = getInstanceOfClass(actionClass);
      }
      else{

        eventObject = paramList.getEventInstance();
        String eventClass = eventObject.getClass().getName();

        // Check whether the class of a rule and the class of an event are the same
        if ( actionClass.compareTo(eventClass) == 0){ // same
          targetInstance = eventObject;
        }
        else{ // not the same
          targetInstance = getInstanceOfClass(actionClass);
        }
      }
    }
    try {
      actionMethod.invoke(targetInstance, methodParams);
    }
    catch (IllegalAccessException iacce) {
      System.out.println("\tERROR! The Action method is not " +
              " accessible from this package.");
      System.out.println("\tThe Action class and the Action method may "+
	      "not have been declared public.");
      iacce.printStackTrace();
    }
    catch (IllegalArgumentException iarge) {
      System.out.println("\tERROR! Illegal arguments specified for the " +
              "Action method.");
      System.out.println("\tThe Action method should take only a single" +
	      " argument of type 'Sentinel.ListOfParameterLists'.");
      iarge.printStackTrace();
    }
    catch (InvocationTargetException ite) {
      ite.printStackTrace();
    }
    return;
  }

  //added by seyang on Oct 6 99

  public void setCondition(Condition cond) {
    condition = cond;
  }

  public void setAction(Action act) {
    action = action;
  }
  //added by seyang on Oct 6 99
}
