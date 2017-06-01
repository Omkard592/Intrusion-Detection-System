
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
import java.util.*;


public class Consumer {

  public Consumer() {}

  public static void main(String[] args) {

    // Initialize the ecaagent
    ECAAgent myAgent = ECAAgent.initializeECAAgent();
    System.out.println("\n\nStart testing AND global event.\n");

    System.out.println("1)Subscribe to L1 at the local site");
    System.out.println("2)Subscribe to START_SERVICE (g1) at the producer site");
    System.out.println("3)Subscribe to END_SERVICE (g2) at the producer site");
    System.out.println("4)Subscribe to START_SERVICE (g3) at the conprod site");
    System.out.println("5)Subscribe to END_SERVICE (g4) at the conprod site");

    System.out.println("AND event on local    (L1 AND g1)");
    System.out.println("AND event on server   (g1 AND g3)");
    System.out.println("AND event on producer (g1 AND g2)");


    System.out.println("\nCreate L1");
    EventHandle L1 = myAgent.createPrimitiveEvent ("L1","Consumer",EventModifier.END,"void doSomething()");


    System.out.println("\nCreate global event \"g1\"");
    EventHandle g1 = myAgent.createPrimitiveEvent ("g1","Consumer","START_SERVICE","producer","newdelhi");
    System.out.println("\nCreate global event \"g2\"");
    EventHandle g2 = myAgent.createPrimitiveEvent ("g2","Consumer","END_SERVICE","producer","newdelhi");

    System.out.println("\nCreate global event \"g3\"");
    EventHandle g3 = myAgent.createPrimitiveEvent ("g3","Consumer","START_SERVICE","conprod","newdelhi");
    System.out.println("\nCreate global event \"g4\"");
    EventHandle g4 = myAgent.createPrimitiveEvent ("g4","Consumer","END_SERVICE","conprod","newdelhi");



    System.out.println("\nCreate rule associated with g1");
    myAgent.createRule("globalRule",g1,"Consumer.CondTest","Consumer.ActionTestG1");


    System.out.println("\nCreate composite event g1 and g3 ");
    EventHandle g1ANDg3 = myAgent.createCompositeEvent (EventType.AND,"g1Andg3",g1,g3);

    System.out.println("\nCreate composite event g1 and L1 ");
    EventHandle g1ANDL1 = myAgent.createCompositeEvent (EventType.AND,"g1AndL1",g1,L1);

    System.out.println("\nCreate primitive global event (g1 and g2) ");
    EventHandle g1ANDg2 = myAgent.createPrimitiveEvent ("g1Andg2","Consumer","startAndstop","producer","newdelhi");





    System.out.println("\nCreate rule associated with AND event (g1AndL1) in RECENT context");
    myAgent.createRule("g1ANDL1RuleInRecent",g1ANDL1,"Consumer.CondTest","Consumer.ActionTestComposite");

    System.out.println("\nCreate rule associated with AND event in CONTINUOUS context");
    myAgent.createRule("g1ANDL1RuleInCont",g1ANDL1,"Consumer.CondTest","Consumer.ActionTestCompositeInCont",1,CouplingMode.IMMEDIATE,ParamContext.CONTINUOUS);

    System.out.println("\nCreate rule associated with AND event in CHRONICLE context");
    myAgent.createRule("g1ANDL1RuleInChron",g1ANDL1,"Consumer.CondTest","Consumer.ActionTestCompositeInChron",1,CouplingMode.IMMEDIATE,ParamContext.CHRONICLE);

    System.out.println("\nCreate rule associated with AND event in CUMULATIVE context");
    myAgent.createRule("g1ANDL1RuleInCum",g1ANDL1,"Consumer.CondTest","Consumer.ActionTestCompositeInCumulative",1,CouplingMode.IMMEDIATE,ParamContext.CUMULATIVE);



    System.out.println("\nCreate rule associated with AND event (g1Andg3) in RECENT context");
    myAgent.createRule("g1ANDg3RuleInRecent",g1ANDg3,"Consumer.CondTest","Consumer.ActionTestComposite");

    System.out.println("\nCreate rule associated with AND event in CONTINUOUS context");
    myAgent.createRule("g1ANDg3RuleInCont",g1ANDg3,"Consumer.CondTest","Consumer.ActionTestCompositeInCont",1,CouplingMode.IMMEDIATE,ParamContext.CONTINUOUS);

    System.out.println("\nCreate rule associated with AND event in CHRONICLE context");
    myAgent.createRule("g1ANDg3RuleInChron",g1ANDg3,"Consumer.CondTest","Consumer.ActionTestCompositeInChron",1,CouplingMode.IMMEDIATE,ParamContext.CHRONICLE);

    System.out.println("\nCreate rule associated with AND event in CUMULATIVE context");
    myAgent.createRule("g1ANDg3RuleInCum",g1ANDg3,"Consumer.CondTest","Consumer.ActionTestCompositeInCumulative",1,CouplingMode.IMMEDIATE,ParamContext.CUMULATIVE);



    System.out.println("\nCreate rule associated with AND event (g1Andg2) in RECENT context");
    myAgent.createRule("g1ANDg2RuleInRecent",g1ANDg2,"Consumer.CondTest","Consumer.ActionTestPrimitive");

    System.out.println("\nCreate rule associated with AND event in CONTINUOUS context");
    myAgent.createRule("g1ANDg2RuleInCont",g1ANDg2,"Consumer.CondTest","Consumer.ActionTestPrimitiveInCont",1,CouplingMode.IMMEDIATE,ParamContext.CONTINUOUS);

    System.out.println("\nCreate rule associated with AND event in CHRONICLE context");
    myAgent.createRule("g1ANDg2RuleInChron",g1ANDg2,"Consumer.CondTest","Consumer.ActionTestPrimitiveInChron",1,CouplingMode.IMMEDIATE,ParamContext.CHRONICLE);

    System.out.println("\nCreate rule associated with AND event in CUMULATIVE context");
    myAgent.createRule("g1ANDg2RuleInCum",g1ANDg2,"Consumer.CondTest","Consumer.ActionTestPrimitiveInCumulative",1,CouplingMode.IMMEDIATE,ParamContext.CUMULATIVE);



    System.out.println("Consumer performs some tasks, while waiting for the notificaion. ");


    Consumer a = new Consumer();


    int inputIn = 0;
    while(inputIn != 2){
      System.out.println("\n=======================================");
      System.out.println("Type 1 to invoke L1");
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
              ((PrimitiveEventHandle)L1).insert("paramTest","paramFromCons");
              a.doSomething();

              inputValid = false;
              System.out.println("\n=======================================");
              System.out.println("Type 1 to invoke startService");
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

  public void doSomething() {
    System.out.println("\nInvoke doSomething :: L1 is raised");
    EventHandle[] monitoredEvent = ECAAgent.getEventHandles("L1");
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
    System.out.println("***** From Action in Consumer::ActionTestCompositeInRecent***** ");
    Enumeration enum = parameterLists.elements();
    ParameterList paramList1 = (ParameterList)   enum.nextElement();
    ParameterList paramList2 = (ParameterList)   enum.nextElement();

    try{
      String dataFromParam1 = (String) paramList1.getObject("paramTest");
      System.out.println("The value of the paramter 1 => " +dataFromParam1);
      String dataFromParam2 = (String) paramList2.getObject("paramTest");
      System.out.println("The value of the paramter 2 => " +dataFromParam2);
    }
    catch(TypeMismatchException e){
      e.printStackTrace() ;
    }
    catch(ParameterNotFoundException e){
      e.printStackTrace() ;
    }
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


  public static void ActionTestPrimitive(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestPrimitiveInRecent***** ");
    return ;
  }


  public static void ActionTestPrimitiveInCont(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestPrimitiveInCont***** ");
    return ;
  }
  public static void ActionTestPrimitiveInChron(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestPrimitiveInChron***** ");
    return ;
  }

  public static void ActionTestPrimitiveInCumulative(ListOfParameterLists parameterLists){
    System.out.println("***** From Action in Consumer::ActionTestPrimitiveInCumulative***** ");
    return ;
  }
}










