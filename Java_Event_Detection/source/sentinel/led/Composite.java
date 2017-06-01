/**
 * Composite.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.*;

/** The Composite class is an abstract class that is extended by all composite event
 *  operator classes. It contains methods that are common to all composite event
 *  operators.
 */

abstract public class Composite extends Event {

  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");
  static boolean glbDetctnReqstDebug = DebuggingHelper.isDebugFlagTrue("glbDetctnReqstDebug");

  // modified by wtanpisu
  // rational : to utilize the rule scheduler

  /** All composite event operators should provide implementation for the
   *  notify method. The logic of the notify() method depends on the semantics
   *  of the composite event operator.
   */
  abstract public void notify(Notifiable child, Thread parent, Scheduler scheduler );

  //added by wtanpisu on 24 Jan 2000

  Composite() {}

  Composite(String eventName) {
    super(eventName);
    eventType = EventType.COMPOSITE;
  }

  /**
   * This method adds the given rule to a composite event.
   */
  void addRule(Rule rule) {
       subscribedRules.addElement(rule);
       setContextRecursive(rule.context.getId());
  }

  /**
   * This method deletes the given rule for a composite event.
   */
  void deleteRule(Rule rule) {
    subscribedRules.removeElement(rule);
    resetContextRecursive(rule.context.getId());
  }

  /**
   *  This method disables the given rule for a composite event.
   */
  void disableRule(Rule rule) {
    rule.disable();
    resetContextRecursive(rule.context.getId());
  }

  /**
   * This method enables the given rule for a composite event.
   */
  void enableRule(Rule rule) {
    rule.enable();
    setContextRecursive(rule.context.getId());
  }


  /**
   * Starts execution process by inserting the new rules in the right place.
   */
  void executeRules(EventSet eventSet,ParamContext context,Thread parent, RuleScheduler ruleScheduler) {



    Rule rule;
	ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
	RuleThread ruleThread = null;
	if(eventDetectionDebug)
  	  System.out.println("Rules that associated with this event => " + eventName);
    ListOfParameterLists parameterLists = new ListOfParameterLists();
    Vector paramListSet = eventSet.getParamLists();
    ParameterList paramList = null;

    for (int i=0; i<paramListSet.size(); i++) {
      paramList = (ParameterList) paramListSet.elementAt(i);
      parameterLists.add(paramList);
    }

    if(Constant.RSCHEDULERFLAG){
      for (int i=0; i<subscribedRules.size(); i++) {

        rule = (Rule) subscribedRules.elementAt(i);
        if(rule.context == context){
          ruleThread = new RuleThread(rule,parameterLists,parent);
          ruleThread.setName(rule.getName());
    	  processRuleList.addRuleThread(ruleThread);
        }
      }
    }else{
      Vector rules = subscribedRules;
      if(eventDetectionDebug)
        System.out.println("Execute rule immediately");
      for (int i=0; i<rules.size(); i++) {
        rule = (Rule) rules.elementAt(i);
        rule.execute(parameterLists,rule.context.getId());
      }
    }

/*
    Rule rule;
    ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
    RuleThread ruleThread = null;
    if(eventDetectionDebug)
      System.out.println("Rules that associated with this event => " + eventName);
    ListOfParameterLists parameterLists = new ListOfParameterLists();
    Vector paramListSet = eventSet.getParamLists();
    ParameterList paramList = null;

    for (int i=0; i<paramListSet.size(); i++) {
      paramList = (ParameterList) paramListSet.elementAt(i);
      parameterLists.add(paramList);
    }
    for (int i=0; i<subscribedRules.size(); i++) {
      rule = (Rule) subscribedRules.elementAt(i);
      if(rule.context == context){
        ruleThread = new RuleThread(rule,parameterLists,parent);
  	ruleThread.setName(rule.getName());
  	processRuleList.addRuleThread(ruleThread);
      }
    }*/
  }

  /**
   * This method signals the occurrence of this composite event to
   *  all the subscribed events.
   */
  void propagateEvent(Thread parent,RuleScheduler ruleScheduler){
    Notifiable event = null;

    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Notifiable) subscribedEvents.elementAt(i);

      Composite a = (Composite) event;
      a.notify(this,parent, ruleScheduler);
    }
  }
  //added by wtanpisu on 24 Jan 2000
}
