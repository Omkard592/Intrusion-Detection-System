/**
 * ExplicitRuleQueue.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/**
 * The ExplicitRuleQueue class  is extended by the ImmRuleQueue class and
 * DefRuleQueue class. All the rules that are associated with the explicit-event
 * will be inserted in this ExplicitRuleQueue (ImmRuleQueue or DefRuleQueue).
 */

abstract class ExplicitRuleQueue extends RuleQueue {

  /**
   * This abstract method require the derived class to implement this
   * insertRule to insert the rule thread into the rule queue
   */
  protected abstract void insertRule(RuleThread newRuleThd);

  ExplicitRuleQueue(Thread applThread, String ruleQueueType){
    super(applThread,ruleQueueType);
  }
}