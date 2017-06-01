
import sentinel.led.*;

import java.io.*;

public class ConProd implements Serializable {

  public ConProd(){}

  public static void main(String[] args) {

    ECAAgent myAgent = ECAAgent.initializeECAAgent();
    ConProd p1 = new ConProd();

    System.out.println("\nBegin create primitives");
    System.out.println("START_SERVICE (G3)");

    PrimitiveEventHandle e1 = (PrimitiveEventHandle) myAgent.createPrimitiveEvent ("START_SERVICE","ConProd",EventModifier.BEGIN ,"void event1()");


    int inputIn = 0;
    while(inputIn != 2){
      System.out.println("\n=======================================");
      System.out.println("Type 1 to invoke START_SERVICE (G3)");
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
            System.out.println("\n=======================================");
            System.out.println("Type 1 to invoke START_SERVICE (G3)");
            System.out.println("Type 2 to end");
            break;
          case 2:
            inputValid = true;
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
    System.out.println("invoke START_SERVICE (G3)");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("START_SERVICE");
    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }

  public void stopService(){
    System.out.println("invoke stopservice");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("STOP_SERVICE");

    ECAAgent.raiseBeginEvent(monitoredEvent,this);
  }
}
