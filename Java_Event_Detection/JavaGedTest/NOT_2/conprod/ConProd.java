
import sentinel.led.*;

import java.io.*;

public class ConProd implements Serializable {

  public ConProd(){}

  public static void main(String[] args) {

    ECAAgent myAgent = ECAAgent.initializeECAAgent();
    ConProd p1 = new ConProd();

    System.out.println("\nBegin create primitives");
    System.out.println("e1 (middle event)");

    PrimitiveEventHandle e1 = (PrimitiveEventHandle) myAgent.createPrimitiveEvent ("e1","ConProd",EventModifier.BEGIN ,"void event1()");


    int inputIn = 0;
    while(inputIn != 2){
      System.out.println("\n======================================");
      System.out.println("Type 1 to invoke middle event");
      System.out.println("Type 2 to end");

    //  e1.insert("paramTest","paramFromConProd");

      boolean inputValid = false;
      while(!inputValid){

        try{
          BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
          inputIn  = Integer.parseInt(inputStream.readLine());
          System.out.println("\nInput = "+inputIn);

          switch(inputIn){
          case 1:
            System.out.println("Case 1");
            e1.insert("paramTest","paramFromConProd");

            p1.event1();
            inputValid = false;
            System.out.println("\n======================================");
            System.out.println("Type 1 to invoke middle event");
            System.out.println("Type 2 to end");
            break;
          case 2:
            inputValid = true;
            break;
          case 3:
            p1.stopService();
            inputValid = false;
            System.out.println("\n======================================");
            System.out.println("Type 1 to invoke middle event");
            System.out.println("Type 2 to end");
            break;
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
		try {
			ch = reader.read();
		}

		catch(IOException ie) {
			ie.printStackTrace();
		}

    System.out.println("\n*** END of LED application ***\n");
  }

  public void event1(){
    System.out.println("invoke event1");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("e1");
    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }

  public void stopService(){
    System.out.println("invoke stopservice");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("STOP_SERVICE");

    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }
}
