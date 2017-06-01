/**
 * TimeQueue.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;

/** The TimeQueue class is used to store time items in a linked list. The time queue
 *  stores the time items in the increasing order of their time values. The time items
 *  with the lowest time value is used to set the timer first. Thus, always the time
 *  value contained in the head time item of the queue is used to set the timer.
 */
class TimeQueue {
  TimeItem head;

  TimeQueue() {
    TimeItem head = null;
  }

  /** This method is used to add the given time item to the timer. The time items
   *  are added in the increasing order of their time values.
   */

  private int noOfEvntInQue;
  public int getNoOfEvntInQue(){
    return noOfEvntInQue;
  }

  synchronized void add(TimeItem ti) {
    long currTime = ti.getMillis();
    noOfEvntInQue ++ ;
    if (head == null) {
      head = ti;
      return;
    }

    TimeItem tempItem = head;
    if (currTime < head.getMillis()) {
      ti.next = head;
      head = ti;
      return;
    }

    while (tempItem.next != null) {
      if (currTime > tempItem.next.getMillis())
        tempItem = tempItem.next;
      else {
	ti.next = tempItem.next;
	tempItem.next = ti;
	return;
      }
    }
    tempItem.next = ti;
    return;
  }

   /** This method deletes the head time item from the time queue and returns it.
    */
   synchronized TimeItem deleteHead() {
       noOfEvntInQue -- ;
      if (head == null) return null;
      TimeItem result = new TimeItem();
      result = head;
      head = head.next;
      result.next = null;

      return result;
   }

   /** This method returns the time value contained in the head time item.
    */
   long getFirstTime() {
      return(head.getMillis());
   }

   boolean isEmpty() {
      return (head == null);
   }

   void print() {
      if (head == null) {
		System.out.println("Time Queue is empty");
		return;
      }

      TimeItem tempItem = head;
      System.out.println();
      while (tempItem != null) {
		tempItem.print();
        tempItem = tempItem.next;
      }
      System.out.println();
   }

   public static void main(String[] args) {
/*
      TimeItem ti1 = new TimeItem("05/21/1999/13:15:00",1);
      TimeItem ti2 = new TimeItem("05/22/1999/10:30:40",2);
      TimeItem ti3 = new TimeItem("05/22/1999/10:30:20",3);
      TimeItem ti4 = new TimeItem("05/22/1999/10:30:30",4);
      TimeItem ti5 = new TimeItem("05/22/1999/10:30:50",5);
      TimeItem ti2 = new TimeItem("0 hrs 0 min 5 sec",2);
      TimeItem ti3 = new TimeItem("0 hrs 0 min 3 sec",3);
      TimeItem ti4 = new TimeItem("0 hrs 0 min 6 sec",4);
      TimeItem ti5 = new TimeItem("0 hrs 0 min 4 sec",5);
      TimeQueue tq = new TimeQueue();
      tq.add(ti1);
      tq.add(ti2);
      tq.add(ti3);
      tq.add(ti4);
      tq.add(ti5);
      System.out.println("\nTimeQueue after adding 5 time items");
      tq.print();
      TimeItem delItem = tq.deleteHead();
      System.out.println("TimeQueue after deleting the head");
      tq.print();
      System.out.print("DeletedItem: ");
      delItem.print();
      System.out.println("\nLowest time = " + tq.getFirstTime());
      System.out.println("\nDeleting all the items ... ");
      while (!tq.isEmpty())
         tq.deleteHead();
      System.out.println("\nTimeQueue after deleting all the items\n");
      tq.print();
      System.out.println(); */
   }
}
