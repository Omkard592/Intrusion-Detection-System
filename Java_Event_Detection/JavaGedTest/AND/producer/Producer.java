
import sentinel.led.*;

import java.io.*;

public class Producer implements Serializable {

  public Producer(){}

  public static void main(String[] args) {





    ECAAgent myAgent = ECAAgent.initializeECAAgent();


    Producer p1 = new Producer();

    System.out.println("\nBegin create primitives");
    System.out.println("START_SERVICE (G1)");
    System.out.println("STOP_SERVICE (G2)\n");


    PrimitiveEventHandle startServiceEvent =     (PrimitiveEventHandle) myAgent.createPrimitiveEvent ("START_SERVICE","Producer",EventModifier.BEGIN ,"void startService()");
    PrimitiveEventHandle stopServiceEvent =      (PrimitiveEventHandle) myAgent.createPrimitiveEvent("STOP_SERVICE", "Producer",EventModifier.BEGIN ,"void stopService()");
    EventHandle startAndstop =     myAgent.createCompositeEvent (EventType.AND,"startAndstop",startServiceEvent,stopServiceEvent);
    int inputIn = 1;
    while(inputIn != 3){
      System.out.println("\n=======================================");
      System.out.println("Type 1 to invoke START_SERVICE (G1)");
      System.out.println("Type 2 to invoke STOP_SERVICE (G2)");
      System.out.println("Type 3 to end");


      String input ;
      boolean inputValid = false;
      while(!inputValid){

        try{

          BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
          inputIn  = Integer.parseInt(inputStream.readLine());

          System.out.println("\n\ninputIn = "+inputIn);
          switch(inputIn){
          case 0:
            inputValid = true;
            break;
          case 1:
            System.out.println("case 1");
            System.out.println("Insert String Object \"paramTest\" ");
            System.out.println("The value of this object is  \"paramFromProducer\"\n");
            startServiceEvent.insert("paramTest","paramFromProducer");
            p1.startService ();
            inputValid = false;
            System.out.println("\n=======================================");
            System.out.println("Type 1 to invoke START_SERVICE (G1)");
            System.out.println("Type 2 to invoke STOP_SERVICE (G2)");
            System.out.println("Type 3 to end");
            break;
          case 2:
            p1.stopService();
            inputValid = false;
            System.out.println("\n=======================================");
            System.out.println("Type 1 to invoke START_SERVICE (G1)");
            System.out.println("Type 2 to invoke STOP_SERVICE (G2)");
            System.out.println("Type 3 to end");
            break;
          case 3:
           inputValid = true;

          }
        }catch(NumberFormatException e ){
          inputValid = false;
        }
        catch(Exception ex ){
          inputValid = false;
        }
      }
    }


    // End the application
    System.out.println("\n\nTo end, hit enter button");
    DataInputStream reader = new DataInputStream(System.in);
	int ch = '0';
	try{
      ch = reader.read();
	}catch(IOException ie) {
	  ie.printStackTrace();
    }
    System.out.println("\n*** END of LED application ***\n");
  }

  public void startService(){
    System.out.println("invoke START_SERVICE");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("START_SERVICE");
    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }

  public void stopService(){
    System.out.println("invoke STOP_SERVICE");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("STOP_SERVICE");
    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }
}
