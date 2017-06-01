/**
 * Primitive.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import sentinel.comm.*;
import java.util.*;

/** The Primitive class is used to construct primitive event nodes for class level
 *  events, instance level events as well as temporal events.
 */
public class Primitive extends Event {
  static boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");
  static boolean mainDebug = Utilities.isDebugFlagTrue("mainDebug");

  private String methodSignature;
  private EventModifier eventModifier;
  private Vector instanceRuleList;
  private DetectionMode detectionMode = DetectionMode.SYNCHRONOUS;
  private Object temporalInstance;

  Primitive(String eName, String cName, EventModifier eModifier, String signature) {
    super(eName);
    instanceRuleList = new Vector();
    InstanceRules instanceRules = new InstanceRules(null);
    instanceRuleList.addElement(instanceRules);
    eventCategory = EventCategory.LOCAL;
  }

  Primitive(String eName, String cName, EventModifier eModifier,
            String signature, DetectionMode detectMode) {
    super(eName);
    detectionMode = detectMode;
    instanceRuleList = new Vector();
    InstanceRules instanceRules = new InstanceRules(null);
    instanceRuleList.addElement(instanceRules);
    eventCategory = EventCategory.LOCAL;
  }

  Primitive(String eName, String cName, EventModifier eModifier,
          String signature, Object instance,DetectionMode detectMode) {
    super(eName);
    detectionMode =	detectMode;
    instanceRuleList = new Vector();
    InstanceRules instanceRules	= new InstanceRules(instance);
    instanceRuleList.addElement(instanceRules);
    eventCategory = EventCategory.LOCAL;
  }

  private void initializeEventNode(String eName, String cName,
	 EventModifier eModifier, String signature) {
    eventType = EventType.PRIMITIVE;
    if(eventDetectionDebug)
      System.out.println("From Primitive , Constructor");
      eventModifier = eModifier;
      methodSignature = Utilities.remWhiteSpaces(cName+eModifier.getId()+signature);
      if(eventDetectionDebug)
        System.out.println("eventName : " + eventName +
                           "\tclassName : " + cName +
                           "\neventModifier : " + eventModifier +
                           "\tmethodSignature : " + methodSignature);
  }
    // add temporalEvent to keep the default temporal event
    // 03/24


  Primitive(String timeStr, Object instance , Object temporalInstance) {
    super(timeStr);
    this.methodSignature = timeStr;
    this.temporalInstance = temporalInstance;
    eventType = EventType.TEMPORAL;
   // An instance rule list is added for a temporal primitive node in order
   // to accomodate rules for absolute temporal events.
   // For both absolute and relative temporal nodes (Plus, P and P*) events, the first element
   // in instanceRuleList stores the Object instance that is specified in the
   // definitions of these events. The value of this variable is inserted into
   // the parameter list whenever the corresponding temporal node is notified by
   // the Timer.
    instanceRuleList = new Vector();
    InstanceRules instanceRules = new InstanceRules(instance);
    instanceRuleList.addElement(instanceRules);
  }

  Primitive(String timeStr, Object instance ) {
    super(timeStr);
    this.methodSignature = timeStr;
    eventType = EventType.TEMPORAL;
  // An instance rule list is added for a temporal primitive node in order
  // to accomodate rules for absolute temporal events.
  // For both absolute and relative temporal nodes (Plus, P and P*) events, the first element
  // in instanceRuleList stores the Object instance that is specified in the
  // definitions of these events. The value of this variable is inserted into
  // the parameter list whenever the corresponding temporal node is notified by
  // the Timer.
    instanceRuleList = new Vector();
    InstanceRules instanceRules = new InstanceRules(instance);
    instanceRuleList.addElement(instanceRules);
  }

  int getDetectionMode() {
    return detectionMode.getId();
  }

  Object getTemporalEventInstance() {
    return temporalInstance;
  }

  String getMethodSignature() {
    return methodSignature;
  }

  public  void setContextRecursive(int context) {
    this.setContextCurrent(context);
  }

  public void resetContextRecursive(int context) {
    this.resetContextCurrent(context);
  }

  Vector getInstanceRuleList() {
    return instanceRuleList;
  }


  /** This method adds an instance level rule to a primitive event node
    * having the same method signature as the event specified in the
    * rule. These are special instance level rules that do not have an
    * associated primitive event node
    */
  void addRule(Rule ruleNode, Object instance) {
    if (instanceRuleList == null)
      System.out.println("ERROR! Primitive: An instance level rule is being" +
			   " added to a class level event that doesn't exist");

    int size = instanceRuleList.size();
    if(mainDebug)
      System.out.println("+size of instance rule list "+ size);
    InstanceRules instanceRules = null;
    setContextRecursive(ruleNode.context.getId());
    for (int i=0; i<size; i++) {
      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      if (instance == instanceRules.getInstance()) {
        instanceRules.addRule(ruleNode);
	return;
      }
    }
    if(instanceRuleList.contains(instance)){
      if(mainDebug)
        System.out.println("WRROR !! design error in addRule");
    }
    else{
      instanceRules = new InstanceRules(instance);
      instanceRules.addRule(ruleNode);
      instanceRuleList.addElement(instanceRules);

    /*  System.out.println("ERROR! Sentinel: The specied event specified in the " +
			      "definition  of the instance level rule \"" +
				 ruleNode.getName() + "\" does not exist");
      */
      return;
    }

  }

  void deleteRule(Rule ruleNode) {
    int size = instanceRuleList.size();
    InstanceRules instanceRules = null;
    resetContextRecursive(ruleNode.context.getId());
    for (int i=0; i<size; i++) {
      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      if (instanceRules.deleteRule(ruleNode) == true)
        return;
    }
  }

  void disableRule(Rule ruleNode) {
    resetContextRecursive(ruleNode.context.getId());
    ruleNode.disable();
  }

  void enableRule(Rule ruleNode) {
    setContextRecursive(ruleNode.context.getId());
    ruleNode.enable();
  }


  /** This method adds a rule to a class level or an instance level
    * primitive event node
    */
  void addRule(Rule ruleNode) {
    if (instanceRuleList == null) {
      System.out.println("ERROR! Primitive: An instance level rule is being" +
		   " added to a class level event that doesn't exist");
      return;
    }
    InstanceRules instanceRules = (InstanceRules)instanceRuleList.firstElement();
    setContextRecursive(ruleNode.context.getId());
    instanceRules.addRule(ruleNode);
  }

  public boolean isClassLevelEvent() {
    InstanceRules instanceRules = (InstanceRules) instanceRuleList.firstElement();
    if (instanceRules.getInstance() == null)
      return true;
    else
      return false;
  }

  public boolean isInstanceLevelEvent(Object instance) {
    if(instance != null){
      InstanceRules instanceRules = (InstanceRules)instanceRuleList.firstElement();
      if (instanceRules.getInstance() == instance)
        return true;
      else
        return false;
    }
    else
      return false;
  }

  void propagateEvent(Thread parent,RuleScheduler ruleScheduler){
    Notifiable event = null;
    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Notifiable) subscribedEvents.elementAt(i);
      Composite a = (Composite) event;
      a.notify(this,parent, ruleScheduler);
    }
  }

  // added by wtanpisu
  // rational : the difference is passing the parent and ruleScheduler

  public void notify(ParameterList paramList, Thread parent, RuleScheduler ruleScheduler) {

    if(eventDetectionDebug){
      System.out.println("Primitive::notify");
      System.out.println("parent name "+parent.getName());
    }

    byte context = 0;
    context = (byte) ContextBit.RECENT + ContextBit.CHRONICLE +
		  ContextBit.CONTINUOUS + ContextBit.CUMULATIVE;

    if(eventDetectionDebug){
      System.out.println("Context"+context);
      System.out.println("call propagatePC(paramList,context"+parent.getName());
    }

    executeRules(paramList, parent, ruleScheduler);
    propagatePC(paramList, context);     // in event class
    propagateEvent(parent, ruleScheduler );
  }


  // added by wtanpisu on 24 Jan 2000

  public void notify(Notifiable event, Thread parent,Scheduler ruleScheduler) {
    // Primitive event does not get notified from another event
    return;
  }

  // Primitive event does not store an event table.
  protected Table getTable(Notifiable event) {
    return null;
  }

  public void executeRules(ParameterList paramList, Thread parent, RuleScheduler ruleScheduler) {
    if(eventDetectionDebug)
    System.out.println("Primitive::executeRules");

    ListOfParameterLists parameterLists = new ListOfParameterLists();
    parameterLists.add(paramList);
  // If this is a class level event, the first element
  // contains class level rules
  // If this is an instance level event, the first element
  // contains instance level rules for that event
    if (instanceRuleList == null) return;
      if(eventDetectionDebug)
        System.out.println("Executing rules on event '" + eventName + "' ...");
    InstanceRules instanceRules = (InstanceRules) instanceRuleList.firstElement();
    executeRuleList(instanceRules,parameterLists,parent,ruleScheduler);
    int size = instanceRuleList.size();

    if(mainDebug)
      System.out.println("Size of instance rule list= "+size);
  // All the other elements in the InstanceRule list contain
  // instance level rules that are created without creating an
  // instance level event
    for (int i=1; i<size; i++) {
      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      if (instanceRules.getInstance() == paramList.getEventInstance())
        executeRuleList(instanceRules,parameterLists,parent,ruleScheduler);
    }
  }

  public void executeRules(ParameterList paramList) {
    ListOfParameterLists parameterLists = new ListOfParameterLists();
    parameterLists.add(paramList);
    // If this is a class level event, the first element
    // contains class level rules
    // If this is an instance level event, the first element
    // contains instance level rules for that event

    if (instanceRuleList == null) {
      if(eventDetectionDebug)
        System.out.println("Check !!! the instanceRuleList is null");
      return;
    }
    if(eventDetectionDebug)
	    System.out.println("Executing rules on event '" +
                                eventName + "' ...");
    InstanceRules instanceRules = (InstanceRules) instanceRuleList.firstElement();
    executeRuleList(instanceRules,parameterLists);
    int size = instanceRuleList.size();
    // All the other elements in the InstanceRule list contain
    // instance level rules that are created without creating an
    // instance level event
    for (int i=1; i<size; i++) {
      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      if (instanceRules.getInstance() == paramList.getEventInstance())
        executeRuleList(instanceRules,parameterLists);
    }
  }

  void executeRuleList(InstanceRules instanceRules, ListOfParameterLists parameterLists) {
    Vector rules = instanceRules.getSubscribedRules();
    Rule rule;
    for (int i=0; i<rules.size(); i++) {
      rule = (Rule) rules.elementAt(i);
      if(eventDetectionDebug)
	 System.out.println("executing the rule without the rulescheduler");
      rule.execute(parameterLists,rule.context.getId());
    }
  }


  // changed by wtanpisu
  void executeRuleList( InstanceRules instanceRules,
                        ListOfParameterLists parameterLists,
                        Thread parent,RuleScheduler ruleScheduler) {

    if(eventDetectionDebug){
      System.out.println("Primitive::executeRuleList");
      if(instanceRules.getInstance() != null)
        System.out.println("The instance that's associated with this rule is" + instanceRules.getInstance().getClass().getName());
    }
    Vector rules = instanceRules.getSubscribedRules();

    Rule rule;
    ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
    RuleThread ruleThread = null;
    if(eventDetectionDebug){
      System.out.println("Rules on event " + eventName);
      System.out.println("Number of rules = " + rules.size());
    }

    if(Constant.RSCHEDULERFLAG){
      for (int i=0; i<rules.size(); i++) {
        rule = (Rule) rules.elementAt(i);
	    ruleThread = new RuleThread(rule,parameterLists,parent);
	    ruleThread.setName(rule.getName());
        if( ruleThread == null){
          if(eventDetectionDebug)
            System.out.println("Error rule thread is null:");
        }
        processRuleList.addRuleThread(ruleThread);
      }
    }
    else{
      if(eventDetectionDebug)
        System.out.println("Execute rule immediately");
      for (int i=0; i<rules.size(); i++) {
        rule = (Rule) rules.elementAt(i);
        rule.execute(parameterLists,rule.context.getId());
      }
    }
    /*
    for (int i=0; i<rules.size(); i++) {
      rule = (Rule) rules.elementAt(i);
      ruleThread = new RuleThread(rule,parameterLists,parent);
      ruleThread.setName(rule.getName());
      if( ruleThread == null){
        if(eventDetectionDebug)
          System.out.println("Error rule thread is null:");
      }
      processRuleList.addRuleThread(ruleThread);
    }
    */
  }
}

