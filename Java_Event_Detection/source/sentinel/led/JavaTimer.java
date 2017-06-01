/**
 * addTqhItem.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

class JavaTimer {
  public static native void addTqhItem(String timeStr, int eventId);
  static {
    System.loadLibrary("hello");
    System.out.println("libhello.so loaded");
  }

  public static void javaMethod(int code) {
    System.out.println("in javaMethod, code= " + code);
    System.out.println("Notification received from the C method");
    if (code == 0) // || code == 1)
      sleepMethod();
    else
      System.out.println("not sleeping, continuing execution");
  }

  public void addItem(String timeStr, int eventId) {
    System.out.println("Begin: JavaTimer.addItem()");
    addTqhItem(timeStr, eventId);
    System.out.println("End: JavaTimer.addItem()");
  }

  public static void sleepMethod() {
    System.out.println("Sleeping for 1/2 minute");
    try {
      Thread.sleep(10000);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      System.out.println("\nInterruptedExeception caught in sleep");
    }
    System.out.println("End of sleepMethod");
  }

  public static void main(String[] args) throws InterruptedException {
    System.out.println("in Java main");
    try {
      JavaTimer jt = new JavaTimer();
      jt.addTqhItem("1 sec",1);
    }
    catch (Exception e){
      e.printStackTrace();
      System.out.println("in main catch");
    }
    System.out.println("Added the Tqh item");
    //Thread.sleep(10000);
    //System.out.println("time = " + System.currentTimeMillis());
    /*for (int k=0; k<10000; k++)
     for (int j=0; j<100000; j++);*/
    // for (int j=0; j<100000; j++);
    // System.out.print(j%1000);
    //System.out.println("time = " + System.currentTimeMillis());
    //while(true);
    //System.out.println("in java main after display");
  }
}
