
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

    System.out.println("\n\nStart testing OR global event detected in the local site\n");

    System.out.println("Subscribe to START_SERVICE (Left Event) at the producer site");
    EventHandle leftEvntHndl = myAgent.createPrimitiveEvent ("g1","Consumer","START_SERVICE","producer","newdelhi");

    System.out.println("Subscribe to START_SERVICE (Left Event) at the producer site");
    EventHandle rightEvntHndl = myAgent.createPrimitiveEvent ("g2","Consumer","STOP_SERVICE","producer","newdelhi");

    System.out.println("\nCreate Middle Event (Global) ");
    EventHandle midEvntHndl = myAgent.createPrimitiveEvent ("g3","Consumer","e1","conprod","newdelhi");

    System.out.println("\nCreate NOT event");
    EventHandle compEvntHandl = myAgent.createCompositeEvent (EventType.NOT,"TESTNOTEVENT",leftEvntHndl,midEvntHndl,rightEvntHndl);

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
      System.out.println("Type 0 to end");

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










