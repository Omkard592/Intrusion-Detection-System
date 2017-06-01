/**
 * EventHandle.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The abstract EventHandle class is extended by the PrimitiveEventHandle and
 *  CompositeEventHandle classes. An event handle is associated with every named
 *  event defined by the application. The event handle is used in composite event
 *  definitions and rule definitions.
 *
 *  It contains the attributes that are common to both primitive and composite
 *  event handles.
 */
public abstract class EventHandle {

  protected Event eventNode;
  private String eventName;

  protected EventHandle(Event event, String eventName) {
    this.eventNode = event;
    this.eventName = eventName;
  }

  public Event getEventNode() {
    return eventNode;
  }

  String getEventName() {
    return eventName;
  }
}
