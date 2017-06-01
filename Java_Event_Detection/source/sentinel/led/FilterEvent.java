/**
 * FilterEvent.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import sentinel.comm.*;
import java.util.Vector;

public class FilterEvent extends Event{
  protected Event event;
  protected Filter filter;

  /**
   * Constructor with Event and Filter
   *
   * @param event a value of type 'Event'
   * @param filter a value of type 'Filter'
   */
  FilterEvent(Event event, Filter filter) {
    this.event = event;
    this.filter = filter;
  }

  /**
   * Check filter
   *
   * @param obj a value of type 'Reactive'
   * @return a value of type 'boolean'
   */
  public boolean check(Reactive obj) {
    return filter.check(obj);
  }

  /**
   * Check filter
   *
   * @return a value of type 'boolean'
   */
  protected boolean check() {
    return filter.check();
  }


  /**
   * This routine will be called when a primitive event is propagated to the composite events
   * Before it notify, check the filter whether it is better to drop propagation off.
   * @param child a value of type 'Notifiable'
   */
  public void notify(Notifiable child, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;
    // This routine will be called when a primitive event is propagated to the composite events.
    // if(check()) event.notify(child);
    if(check()) ((Composite)event).notify(child, parent,ruleScheduler);
  }

  public Event getEventNode(){
	return event;
  }

  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   *
   * @param context a value of type 'int'
   */
  public void setContextRecursive(int context) {
    event.setContextRecursive(context);
  }

  /** This method decrements the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   *
   * @param context a value of type 'int'
   */
  public  void resetContextRecursive(int context) {
    event.resetContextRecursive(context);
  }

  /** delegate call to inner event
   *
   * @param eventSet of type 'EventSet'
   * @param context of type 'Context'
   */
  void executeRules(EventSet eventSet,ParamContext context) {
    event.executeRules(eventSet,context);
  }

  /**
   * This method returns the context counter for the given context for an event node.
   */
  int getContextCurrent(int context){
    return event.getContextCurrent(context);
  }

  Vector getSubscribedRules() {
    return event.getSubscribedRules();
  }

  protected  Table getTable(Notifiable e) {
    return event.getTable(e);
  }

  /**
   * This method propagates the occurrence of an event to all subscribed events.
   */
  // modified by wtanpisu
  // what : add two parameter : Thread and RuleScheduler
  void propagateEvent(Thread parent, RuleScheduler ruleScheduler) {
    Notifiable event = null;
    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Notifiable) subscribedEvents.elementAt(i);
      Composite a = (Composite) event;
      a.notify(this,parent, ruleScheduler);
    }
  }

  /**
   * This method propagates an event set in the given context to all
   * the subscribed events. This method is used for propagating a composite
   * event occurrence to the subscribed composite events.
   */
  void propagatePC(EventSet s,ParamContext context) {
    event.propagatePC(s,context);
  }

  void propagatePC(ParameterList paramList, byte context) {
    event.propagatePC(paramList, context);
  }

  /**
   * This method decrements the context counter for the given context for an event node.
   */
  protected  void resetContextCurrent(int context){
    event.resetContextCurrent(context);
  }

  /**
   * This method increments the context counter for the given context for an event node.
   */
  protected void setContextCurrent(int context) {
   event.setContextCurrent(context);
  }

  /** This method subscribes a composite event to this event.
   */
  void subscribe(Event compositeEvent) {
    event.subscribe(compositeEvent);
  }
  void addRule(Rule rule) {
    if(event instanceof Primitive)
    ((Primitive) event).addRule(rule);
    else if(event instanceof Composite)
    ((Composite) event).addRule(rule);
  }
}
