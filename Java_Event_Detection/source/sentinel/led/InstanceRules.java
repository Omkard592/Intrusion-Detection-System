/**
 * InstanceRules.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.Vector;

/** This class is used to store an instance and the rules associated
 *  with the corresponding instance level event. A primitive event node stores a
 *  vector of InstanceRules in order to accomodate instance level rules that are
 *  created without creating instance level events.
 */
class InstanceRules {

  /**
   * The flag indicates the policy of adding a rule in the subscribedRules list.
   * If it is true, the rule is inserted into the proper place according to
   * its priority.
   * It it is false, the rule is inserted at the end of the list.
   */
  boolean OrderByPriorityFlag = false;

  Object instance;
  Vector subscribedRules;
  private boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  InstanceRules(Object instance) {
    this.instance = instance;
    subscribedRules = new Vector();

    /**
     * When rule scheduler is off, the rule is inserted into the place based
     * on its priority.
     */
    if(Constant.RSCHEDULERFLAG == false){
      OrderByPriorityFlag = true;
    }else{
      OrderByPriorityFlag = false;
    }
  }

  Object getInstance() {
    return instance;
  }

  /** This method adds the given rule to the rule list associated with this instance.
   */
  void addRule(Rule ruleNode) {
    // order by priority
    if(OrderByPriorityFlag){
      if(subscribedRules.size() == 0){
        subscribedRules.addElement(ruleNode);
        return;
      }
      else{
        for(int i=0; i< subscribedRules.size(); i++){
          Rule currntRule = (Rule) subscribedRules.elementAt(i);
          if(currntRule.getPriority() < ruleNode.getPriority()){
            subscribedRules.insertElementAt(ruleNode,i);
            return;
          }
        }
        subscribedRules.addElement(ruleNode);
        return;
      }
    }
    // insert at the end of list
    else{
      subscribedRules.addElement(ruleNode);
        return;
    }
  }



  /** This method deletes the given rule from the rule list associated with this
   *  instance.
   */
  boolean deleteRule(Rule ruleNode) {
    if (subscribedRules.removeElement(ruleNode) == true)
      return true;
    else
      return false;
  }

  Vector getSubscribedRules() {
    return subscribedRules;
  }

  void print() {
    Rule rule;
    if(ruleSchedulerDebug){
      if(instance != null)
        System.out.println(instance.toString());

      for (int i=0; i<subscribedRules.size(); i++) {
        rule = (Rule) subscribedRules.elementAt(i);
        rule.print();
     }
    }
  }
}
