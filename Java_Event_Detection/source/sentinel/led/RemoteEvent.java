/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.led;
import sentinel.comm.*;

import java.util.*;


/**
 * The remote event class is used to construct remote event nodes in the local
 * event detection. This remote node represents the producer event node on the
 * local site.
 */
public class RemoteEvent extends Event {

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");
  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");


  static boolean contextPropagationDebug = DebuggingHelper.isDebugFlagTrue("contextPropagationDebug");
  static boolean globalRuleCreationDebug = DebuggingHelper.isDebugFlagTrue("globalRuleCreationDebug");
  static boolean glbDetctnReqstDebug = DebuggingHelper.isDebugFlagTrue("glbDetctnReqstDebug");
  static boolean eventDetectionDebug = DebuggingHelper.isDebugFlagTrue("eventDetectionDebug");

  /** an event name at the producer site. */
  private String prodEvntNm;

  /** an application name in which the event occurs*/
  private String appNm;

  /** a machine name where the application runs */
  private String machNm;

  /** a list contains the rules associated wit this remote node */
  private Vector instanceRuleList;

  public RemoteEvent() {
  }

  /** Constructor */
  public RemoteEvent(String eventNm,String prodEvntNm,String appNm,String machNm){
    super(eventNm);
    this.prodEvntNm =prodEvntNm;
    this.appNm = appNm;
    this.machNm = machNm;
    instanceRuleList = new Vector();
    InstanceRules instanceRules = new InstanceRules(null);
    instanceRuleList.addElement(instanceRules);
  }


  /**
   * reset the context of this remote event
   */
  public void resetContextRecursive(int context) {
    this.resetContextCurrent(context);
  }

  // Remote event does not store an event table.
  protected Table getTable(Notifiable e) {
    return null;
  }


  /**
   * This method is to set the context by increasing the context counter.
   * This number tells the number of rules at associated (direct and indirect)
   * in particular context
   */

  public void setContextRecursive(int context) {

    /* Increase the counter of this context */
    this.setContextCurrent(context);

    /* Increase the counter of the global event on server */
    propagateContextToServer(context);
  }


  /**
   * This method is to increment the context counter for the given context at
   * the globol event node on the server site. After the context counter is
   * increased, the send back flag on the server is also turn on.
   * The increment method is called when there is a rule defined on this remote node.
   */

  void propagateContextToServer(int context){

    LEDInterface ledIntf = ECAAgent.getLEDInterface();

    if(contextPropagationDebug)
      System.out.println("This new rule has the "+ ParamContext.getContext(context)+"  context");


      /*
       * Only when recent counter is 1, the request will be sent to server to
       * increment the context counter on the global node. When recent
       * counter is greater than 1, there is no need to send the request to
       * set the send back flag on the global node since forwardFlag is already set
       */
    if(context == ParamContext.recentContext && recentCounter == 1){
      if(contextPropagationDebug){
        System.out.println("Propagate the context of the rule to server");
        System.out.println("This signal will set recent context counter on this event node : "+prodEvntNm +" on the server site");
      }
      DetectionRequestMessage detectnReqstMesg = new DetectionRequestMessage(prodEvntNm,appNm,machNm,context);
      String des = Constant.GED_URL+Constant.GED_NAME;
      ledIntf.send(des,detectnReqstMesg);
    }else if(context == ParamContext.contiContext && contiCounter == 1){
      if(contextPropagationDebug){
        System.out.println("Propagate the context of the rule to server");
        System.out.println("This signal will set recent context counter on this event node : "+prodEvntNm +" on the server site");
      }
      DetectionRequestMessage detectnReqstMesg = new DetectionRequestMessage(prodEvntNm,appNm,machNm,context);
      String des = Constant.GED_URL+Constant.GED_NAME;
      ledIntf.send(des,detectnReqstMesg);
    }else if(context == ParamContext.chronContext && chronCounter == 1){
      if(contextPropagationDebug){
        System.out.println("Propagate the context of the rule to server");
        System.out.println("This signal will set recent context counter on this event node : "+prodEvntNm +" on the server site");
      }
      DetectionRequestMessage detectnReqstMesg = new DetectionRequestMessage(prodEvntNm,appNm,machNm,context);
      String des = Constant.GED_URL+Constant.GED_NAME;
      ledIntf.send(des,detectnReqstMesg);
    }else if(context == ParamContext.cumulContext && cumulCounter == 1){
      if(contextPropagationDebug){
        System.out.println("Propagate the context of the rule to server");
        System.out.println("This signal will set recent context counter on this event node : "+prodEvntNm +" on the server site");
      }
      DetectionRequestMessage detectnReqstMesg = new DetectionRequestMessage(prodEvntNm,appNm,machNm,context);
      String des = Constant.GED_URL+Constant.GED_NAME;
      ledIntf.send(des,detectnReqstMesg);
    }
  }


  // Remote event does not get notified from another event
  public void notify(Notifiable event, Thread parent,Scheduler ruleScheduler) {
    return;
  }


  /**
   * Notify the remote (primitive) event. This method is called when it
   * receives the notification message from GED
   */
  private void notifyPrimitive (ParameterList paramList, Thread parent, RuleScheduler ruleScheduler) {
    if(evntNotifDebug)
      System.out.println("\nRemoteEvent::notify!");
    byte context = 0;
    context = (byte) ContextBit.RECENT + ContextBit.CHRONICLE +
	  ContextBit.CONTINUOUS + ContextBit.CUMULATIVE;

    propagatePC(paramList, context);
    executeRules(paramList, parent, ruleScheduler);
    propagateEvent(parent, ruleScheduler );
  }

  /**
   * Notify the remote (composite) event. This method is called when it
   * receives the notification message from GED
   */
  private void notifyComposite (PCTable globalPCTab, Thread parent, RuleScheduler ruleScheduler){

    EventSet set =null;


    InstanceRules instanceRules = (InstanceRules)instanceRuleList.firstElement();
    instanceRules.print() ;

    if(evntNotifDebug){
      System.out.println("\nRemoteEvent::notify remote composite node");
      printDetectionMask();
    }

    if (recentCounter != 0) { // detect recent
      PCEntry entry = globalPCTab.getRecentSet();

      /**
       * if the entry is null, there is no occurence in this context.
       */
      if(entry != null){
        if(evntNotifDebug)
          System.out.println("Execute rule in recent context");
        set =  entry.getEventSet();

        executeRules(set,ParamContext.RECENT,parent,ruleScheduler);
//        System.out.println("propagatePC at the recent conext");
        propagatePC(set,ParamContext.RECENT);
//        System.out.println("Finish propagatePC at the recent conext");

        if(forwardFlag ){ // the event is subscribed by remote event
          globalPCTab.getCompositePropagation(set,ParamContext.RECENT.getId()) ;
          System.out.println("Show the pctable that will sent to remote site");
          globalPCTab.print();
        }
      }
    }
    if (chronCounter != 0) {
      if(evntNotifDebug)
        System.out.println("Execute rule in chronicle context");
      PCEntry  entry = globalPCTab.getOldestChronSet();

      /**
       * if the entry is null, there is no occurence in this context.
       */
      if(entry != null){
        entry.clearChronicle();
        executeRules(set,ParamContext.CHRONICLE,parent,ruleScheduler);
 //       System.out.println("propagatePC at the recent conext");
        propagatePC(set,ParamContext.CHRONICLE);
 //       System.out.println("Finish propagatePC at the recent conext");

        if(forwardFlag ){ // the event is subscribed by remote event
          globalPCTab.getCompositePropagation(set,ParamContext.CHRONICLE.getId()) ;
          System.out.println("Show the pctable that will sent to remote site");
          globalPCTab.print();
        }
      }
    }
    if (contiCounter != 0) {
      if(evntNotifDebug)
        System.out.println("Execute rule in continous context");
      Vector setArry = globalPCTab.getContiSets();
      PCEntry entry = null;
      globalPCTab.print();
      for(int i = 0 ; i < setArry.size(); i++){
        entry = (PCEntry) setArry.elementAt(i) ;
        entry.clearContinuous();
        set =  entry.getEventSet();
        executeRules(set,ParamContext.CONTINUOUS,parent,ruleScheduler);
   //     System.out.println("propagatePC at the recent conext");
        propagatePC(set,ParamContext.CONTINUOUS);
   //     System.out.println("Finish propagatePC at the recent conext");

        if(forwardFlag ){ // the event is subscribed by remote event
          globalPCTab.getCompositePropagation(set,ParamContext.CONTINUOUS.getId()) ;
          if(evntNotifDebug){
            System.out.println("Show the pctable that will sent to remote site");
            globalPCTab.print();
          }
        }
      }
    }
    if(cumulCounter != 0) {
      if(evntNotifDebug)
        System.out.println("Execute rule in cumulative context");
      Vector setArry = globalPCTab.getCumulSet();
      PCEntry entry = null;
      for(int i = 0; i < setArry.size() ; i++){
        set = (EventSet)  setArry.elementAt(i);
        System.out.println(ParamContext.CUMULATIVE);
        executeRules(set,ParamContext.CUMULATIVE,parent,ruleScheduler);
  //      System.out.println("propagatePC at the recent conext");
        propagatePC(set,ParamContext.CUMULATIVE);
  //      System.out.println("Finish propagatePC at the recent conext");
        if(forwardFlag ){ // the event is subscribed by remote event
          globalPCTab.getCompositePropagation(set,ParamContext.CUMULATIVE.getId()) ;
          System.out.println("Show the pctable that will sent to remote site");
          globalPCTab.print();
        }
      }
    }
    propagateEvent(parent,ruleScheduler);
  }

  /**
   * This method is called by LEDInterface, when it receives the notification message from GED
   */

  public void notify(PCTable globalPCTab, Thread parent, RuleScheduler ruleScheduler) {

    if(evntNotifDebug)
        System.out.println("\nRemoteEvent::notify");
    SentinelComm commInterf = null;
    EventSet set =null;
    Vector pcEntries = globalPCTab.getPCEntries();
    PCEntry pcEntry = (PCEntry)     pcEntries.firstElement();
    EventSet evntSet = (EventSet) pcEntry.getEventSet();
    Vector paramLists = evntSet.getParamLists();

    // The global primitive event has occured
    if(globalPCTab.isForPrimitive()){
      if(evntNotifDebug)
        System.out.println("\nRemoteEvent::notify remote primitive node");
      ParameterList globalParam = (ParameterList) paramLists.firstElement();
      notifyPrimitive(globalParam, parent,ruleScheduler);
    }
    // The global composite event has occured
    else{
      notifyComposite(globalPCTab, parent,ruleScheduler);
    }

    //<GED>
    if(forwardFlag){
      LEDInterface ledIntf = ECAAgent.getLEDInterface();
      if(evntNotifDebug){
        System.out.println("\nShow the pctable that will be sent to sever");
        globalPCTab.print();
      }
      NotificationMessage notifMesg = new NotificationMessage(eventName,Constant.APP_NAME,Constant.APP_URL,globalPCTab);

      commInterf =  ledIntf;

      /*For single GED, doesn't need to specify the destination*/
      commInterf.send("",notifMesg);
    }
  }

  /**
   * Execute the rules on the global composite event on the leaf node of the
   * event graph.
   */
  void executeRules(EventSet eventSet,ParamContext context,Thread parent, RuleScheduler ruleScheduler) {
//    System.out.println("Execute the remote rules in this context "+context.getId());
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


    int size = instanceRuleList.size();

    //System.out.println("Size of instance rule list= "+size);
    InstanceRules instanceRules;

    for (int i=0; i<size; i++) {

      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      Vector rules = instanceRules.getSubscribedRules();
//      System.out.println("Size of subscribers"+rules.size());
      for(int j =0 ; j< rules.size(); j++){
        rule = (Rule) rules.elementAt(j);
  //        System.out.println("context "+context.getId());
        if(rule.context == context){
    //      System.out.println("rule.context "+rule.context.getId() +" context = :"+context.getId());

          ruleThread = new RuleThread(rule,parameterLists,parent);
  		  ruleThread.setName(rule.getName());
//          System.out.println("add rule"+rule.getName());
  		  processRuleList.addRuleThread(ruleThread);

        }
      }
    }
  }


  /**
   * Propagate the occurence of this remote event to its parent.
   */
  void propagateEvent(Thread parent,RuleScheduler ruleScheduler){
    Notifiable event = null;
    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Notifiable) subscribedEvents.elementAt(i);
      Composite a = (Composite) event;
      a.notify(this,parent, ruleScheduler);
    }
  }

  /**
   * Execute the rules on the global primitive event on the leaf node of the
   * event graph.
   */
  public void executeRules(ParameterList paramList, Thread parent,RuleScheduler ruleScheduler) {
    ListOfParameterLists parameterLists = new ListOfParameterLists();
    parameterLists.add(paramList);
    // If this is a class level event, the first element
    // contains class level rules
    // If this is an instance level event, the first element
    // contains instance level rules for that event
    if (instanceRuleList == null) return;

    if(eventDetectionDebug)
    System.out.println("Executing rules on event '" +
                                eventName + "' ...");
    InstanceRules instanceRules = (InstanceRules) instanceRuleList.firstElement();
    executeRuleList(instanceRules,parameterLists,parent,ruleScheduler);
    int size = instanceRuleList.size();

    //    if(mainDebug)
    //  System.out.println("Size of instance rule list= "+size);

    // All the other elements in the InstanceRule list contain
    // instance level rules that are created without creating an
    // instance level event
    for (int i=1; i<size; i++) {
      instanceRules = (InstanceRules) instanceRuleList.elementAt(i);
      if (instanceRules.getInstance() == paramList.getEventInstance())
		  executeRuleList(instanceRules,parameterLists,parent,ruleScheduler);
    }
  }

  /**
   * Execute rules
   */
  void executeRuleList( InstanceRules instanceRules,
                        ListOfParameterLists parameterLists,
                        Thread parent,RuleScheduler ruleScheduler) {

    if(eventDetectionDebug){
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

  /** This method adds a rule to a remot event node */

  void addRule(Rule ruleNode) {
    if (instanceRuleList == null) {
      System.out.println("ERROR!!! RemoteEvent: An instance level rule is being" +
		   " added to a class level event that doesn't exist");
      return;
    }
    if(globalRuleCreationDebug)
      System.out.println("RemoteEvent::addRule to "+eventName);


    InstanceRules instanceRules = (InstanceRules)instanceRuleList.firstElement();

    /*Increase the context counter corresponding the rule's context */
    setContextRecursive(ruleNode.context.getId());
    instanceRules.addRule(ruleNode);
    instanceRules.print() ;
  }

  /** Return an event name */
  String getProdEvntNm(){
    return prodEvntNm;
  }

  /** Return an application name */
  String getAppNm(){
    return appNm;
  }

  /** Return a machine name */
  String getMachNm(){
    return machNm;
  }
}