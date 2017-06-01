
/**
 * Title:
 * Description:  Test primitive global event
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import sentinel.led.*;
import java.io.*;


public class Consumer {

  public Consumer() {}

  public static void main(String[] args) {

    // Initialize the ecaagent
    ECAAgent myAgent = ECAAgent.initializeECAAgent();

    System.out.println("\n\nStart testing NOT global event detected in the local site\n");


    System.out.println("\nCreate Left Event");
    EventHandle leftEvntHndl = myAgent.createPrimitiveEvent ("left","Consumer",EventModifier.END,"void raiseLeft()");

    System.out.println("\nCreate Right Event");
    EventHandle rightEvntHndl = myAgent.createPrimitiveEvent ("right","Consumer",EventModifier.END,"void raiseRight()");


    System.out.println("\nCreate Middle Event (Global) ");

    System.out.println("Subscribe to START_SERVICE (Middle Event) at the producer site");
    EventHandle midEvntHndl1 = myAgent.createPrimitiveEvent ("g1","Consumer","START_SERVICE","producer","newdelhi");

    System.out.println("\nCreate NOT event");
    EventHandle compEvntHandl = myAgent.createCompositeEvent (EventType.NOT,"TESTNOTEVENT",leftEvntHndl,midEvntHndl1,rightEvntHndl);

    System.out.println("\nCreate rule associated with NOT event");
    myAgent.createRule("notRule",compEvntHandl,"Consumer.CondTest","Consumer.ActionTestComposite");


    System.out.println("\nCreate rule associated with NOT event in CONTINUOUS context");
    myAgent.createRule("notRuleInCont",compEvntHandl,"Consumer.CondTest","Consumer.ActionTestCompositeInCont",1,CouplingMode.IMMEDIATE,ParamContext.CONTINUOUS);

    System.out.println("\nCreate rule associated with NOT event in CHRONICLE context");
    myAgent.createRule("notRuleInChron",compEvntHandl,"Consumer.CondTest","Consumer.ActionTestCompositeInChron",1,CouplingMode.IMMEDIATE,ParamContext.CHRONICLE);

    System.out.println("\nCreate rule associated with NOT event in CUMULATIVE context");
    myAgent.createRule("notRuleInCum",compEvntHandl,"Consumer.CondTest","Consumer.ActionTestCompositeInCumulative",1,CouplingMode.IMMEDIATE,ParamContext.CUMULATIVE);


    System.out.println("Consumer performs some tasks, while waiting for the notificaion. ");


    Consumer a = new Consumer();


    int inputIn = 1;

    while(inputIn != 0){
      System.out.println("\n======================================");
      System.out.println("Type 0 to end");
      System.out.println("Type 1 to invoke left local event ");
      System.out.println("Type 2 to invoke right local event ");

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
            System.out.println("\nLeft event occurs");
            a.raiseLeft();
            inputValid = false;
            System.out.println("\n======================================");
            System.out.println("Type 0 to end");
            System.out.println("Type 1 to invoke left local event ");
            System.out.println("Type 2 to invoke right local event ");
            break;
          case 2:
            System.out.println("case 2");
            System.out.println("\nRight event occurs");
            a.raiseRight();
            inputValid = false;
            System.out.println("\n======================================");
            System.out.println("Type 0 to end");
            System.out.println("Type 1 to invoke left local event ");
            System.out.println("Type 2 to invoke right local event ");
            break;
          }

        }
        catch(NumberFormatException e ){
          inputValid = false;
        }
        catch(Exception ex ){
          inputValid = false;
        }
      }
    }





    /* Terminate the program*/

    System.out.println("\nHit the Enter to END");
    DataInputStream reader = new DataInputStream(System.in);
		int ch = '0';
		try {
			ch = reader.read();
		}

		catch(IOException ie) {
			ie.printStackTrace();
		}

    System.out.println("\n*** END of LED application ***\n");
  	System.exit(0);
  }

  public void raiseLeft() {
    System.out.println("\nInvoke the left event :: left is raised");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("left");
    ECAAgent.raiseEndEvent(monitoredEvent,this);
  }

  public void raiseRight() {
    System.out.println("\nInvoke the right event :: right is raised");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("right");
    ECAAgent.raiseEndEvent(monitoredEvent,this);
  }

  public static boolean CondTest(ListOfParameterLists parameterLists){
    System.out.println("\n\n***** The Condition is true ***** ");
    return true;
  }

  public static void ActionTestG1(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestG1***** ");
    String dataFromParam="";
    ParameterList paramList = parameterLists.getFirst();
    System.out.println("*** Check the remote paramter !!!");

    try{
      dataFromParam = (String) paramList.getObject("paramTest");
      System.out.println("The value of the paramter from producer is => " +dataFromParam);
    }
    catch(TypeMismatchException e){
      e.printStackTrace() ;
    }
    catch(ParameterNotFoundException e){
      e.printStackTrace() ;
    }
    return ;
  }

  public static void ActionTestComposite(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestComposite***** ");
    return ;
  }
  public static void ActionTestCompositeInCont(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestCompositeInCont***** ");
    return ;
  }
  public static void ActionTestCompositeInChron(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestCompositeInChron***** ");
    return ;
  }
  public static void ActionTestCompositeInCumulative(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestCompositeInCumulative***** ");
    return ;
  }
}










