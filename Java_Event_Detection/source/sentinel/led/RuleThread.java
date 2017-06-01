/**
 * RuleThread.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;
import java.util.Vector;

/** The RuleThread class is used to represent the thread that is created for
 *  executing a rule. Each rule is executed in a separate thread when it is
 *  scheduled by the rule scheduler.
 *  The name of the thread is the rule name itself.
 */

class RuleThread extends Thread {
  private String name;
  private ListOfParameterLists paramLists;
  private int operatingMode = RuleOperatingMode.READY;
  private int priority;
  private CouplingMode coupling;
  private ParamContext context;
  private Thread parent;
  private Executable rule;
  private Object dummySwitch =new Object() ;
  private boolean start= false;

  RuleThread next;

  private boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  RuleThread() {}

  RuleThread(Executable rule, ListOfParameterLists paramLists, Thread parent) {
    this.name = rule.getName();
    this.paramLists = paramLists;
    this.priority = rule.getPriority();
    this.coupling = rule.getCoupling();
    this.context = rule.getContext();
    this.rule = rule;
    this.parent = parent;
    this.next = null;
  }

  RuleThread(RuleThread rt) {
    this.name = rt.getName();
    this.paramLists = rt.getParamLists();
    this.priority = rt.getRulePriority();
    this.coupling = rt.getCoupling();
    this.context = rt.getContext();
    this.rule = rt.getRule();
    this.operatingMode = rt.getOperatingMode();
    this.parent = rt.getParent();
    this.next = null;
  }

  synchronized void setOperatingMode(int mode) {
    this.operatingMode = mode;
  }

  synchronized int  getOperatingMode() {
    return operatingMode;
  }

  CouplingMode getCoupling() {
    return coupling;
  }

  Thread getParent() {
    return parent;
  }

  ParamContext getContext() {
    return context;
  }

  Executable getRule() {
    return rule;
  }

  ListOfParameterLists getParamLists() {
    return paramLists;
  }

  int getRulePriority() {
    return priority;
  }

  void print(){
    if(ruleSchedulerDebug){
      System.out.print("Rule: " + rule.getName());
      System.out.print("Priority: " + rule.getPriority());
      System.out.println("Mode: " + this.operatingMode);
    }
  }

  /** The run method of the rule thread simply executes the rule.
  */

  public boolean isStart(){
    synchronized (dummySwitch){
      try{
        while (start != true)
          dummySwitch.wait();       // wait until this thread is start
      }
      catch(InterruptedException e){}
    }
    return true;
  }

  private Scheduler ruleScheduler;

  public void setScheduler(Scheduler ruleScheduler){
    this.ruleScheduler = ruleScheduler;
  }

  public void run() {
    if(ruleSchedulerDebug)
      System.out.println("Start Executing rule "+Thread.currentThread().getName() );

    synchronized (dummySwitch){
      this.start = true;

      // notify the parent thread that this rule already started.
      // so that the parent thread can perform wait-for-imm method.
      dummySwitch.notifyAll();
    }
    if (ruleSchedulerDebug)
      System.out.print("RuleThread: Executing rule " );

    this.print();

    if(ruleSchedulerDebug)
      System.out.print("RuleThread: Start Executing rule " );

    // Start checking condition and performing action (if applicable)
    rule.execute(paramLists,context.getId());

    if(ruleSchedulerDebug)
      System.out.print("RuleThread: Finish Executing rule " );

    synchronized(ruleScheduler){
      ruleScheduler.notifyAll();
    }
  }
}

