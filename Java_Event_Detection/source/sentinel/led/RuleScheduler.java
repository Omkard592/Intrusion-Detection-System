package sentinel.led;



/** The RuleScheduler class represents the rule scheduler. It contains an instance of
 *  a process rule list. The rule scheduler schedules rules in immRuleQueue if
 *  deferredFlag is false, else it schedules the rules in deferredQueueOne. If a
 *  particular rule queue is empty, it calls the wait() method and waits. It is woken
 *  up again when an event triggers rules, that is done from inside the notifyEvent()
 *  method of an event node.
 */

public class RuleScheduler extends Thread implements Scheduler {

  ProcessRuleList processRuleList;
  boolean deferredFlag;
  private boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  // added by wtanpisu
  // rational : to utilize rule scheduler
  Thread applThrd;
  // add 03/20
//<d>  Thread timerThread;
  TemporalRuleQueue temporalRuleQueue;
  ImmRuleQueue immRuleQueue;
  DefRuleQueue deffRuleQueueOne;
  // added by wtanpisu on 24 Jan 2000

 //<d> RuleScheduler(Thread applThread, Thread timerThread) {
 RuleScheduler(Thread applThread) {
   //<d> processRuleList = new ProcessRuleList(applThread,timerThread);
 processRuleList = new ProcessRuleList(applThread);
  	deferredFlag = false;
  	setDaemon(true);
    // added by wtanpisu
    this.applThrd = applThrd;
  	//## add 03/20

  //<d>	this.timerThread = timerThread;
  	immRuleQueue = processRuleList.getImmRuleQueue();
  	deffRuleQueueOne = processRuleList.getDeffRuleQueueOne();
    temporalRuleQueue = processRuleList.getTemporalRuleQueue();
    // added by wtanpisu on 24 Jan 2000
  }

  public ProcessRuleList getProcessRuleList() {
    return processRuleList;
  }

  // This method wakes up the rule scheduler thread by calling the notify() method.
  public synchronized void wakeup() {
    if (ruleSchedulerDebug)
      System.out.print("Waking up ruleScheduler");
	  this.notify();
  }

  // changed by wtanpisu
  public void resetDeferFlag(){
		deferredFlag = false;
	}

  // The run method is in an infinite loop either waiting(when the rule queue is empty) or scheduling rules.
  public void run() {

    RuleThread currntThrd;
		RuleQueue ruleQueue;
  	RuleThread tempThrd;
	  //	RuleThread t;
		RuleQueue currRuleQueue;

		int ruleOperatingMode =0;
		int	tempThrdPriority =0;
		while(true){
      //if(ruleSchedulerDebug)
			 // System.out.println("Looping in rulescheduler");

      // When there is no rule at imm rule queue and process deferred rule
			// flag is false, then go sleep.

      if( deferredFlag == false && temporalRuleQueue.getHead() == null && immRuleQueue.getHead() == null){
        if(ruleSchedulerDebug) {
					System.out.println("There is no rule in any of these rule queues.");
          System.out.println("Scheduler sleep");
        }
        synchronized (this){
					try{
						wait();
					}
					catch(InterruptedException ie) {
						if(ruleSchedulerDebug)
							System.out.println("InterruptedException caught");
						ie.printStackTrace();
					}
				}
				if(ruleSchedulerDebug)
					System.out.println("Scheduler awake");
      }

      // Execute the rules in temporal rule queue
      if(temporalRuleQueue.getHead() != null){
           processTemporalRule()  ;

      }

      // Finish executing all the deferred rules

			if( deferredFlag == true && immRuleQueue.getHead() == null){

  			// transaction complete
	  		// reset the deferredFlag to be false when all the deferred rules are already executed

				deferredFlag = false 	;
				if(deffRuleQueueOne.getHead() == null){
					if(ruleSchedulerDebug)
						System.out.println("All rules in rule queues are already executed.");
				}
			}



      // Start executing a deferred rule.
      else if ( deferredFlag == true && immRuleQueue.getHead() != null){
          processRuleQueue(deffRuleQueueOne);

			}

      //****************************************************************
			//  When there is a rule at imm rule queue and process deferred rule
			// 	flag is false, then trigger a rule.

      else if( immRuleQueue.getHead() != null && deferredFlag == false){
          processRuleQueue(immRuleQueue);
      }
		}
	}

  protected void processRuleQueue(RuleQueue ruleQue){
   if(ruleQue.getHead() !=null){
   if(ruleSchedulerDebug)
    System.out.println("PROCESS"+ruleQue.getRuleQueType ());

    RuleThread currntThrd = null ;
    RuleThread headThrd ; // The thread that is at the head of the imm queue
		int headOperatingMode ; // The operating mode of the rule hread at head
		int	headPriority ;
		Thread headParentThrd ; // Parent of head rule thread.

    headThrd           =  ruleQue.getHead();
		headOperatingMode  =  headThrd.getOperatingMode() ;

		if(ruleSchedulerDebug){
					System.out.println("rule scheduler is working on imm rule queue.");
					System.out.println("Head node has "+	headOperatingMode+" ruleOperatingMode");
		}

				// CASE 1: when the head is in ready mode

		if	((headThrd=ruleQue.getHead()) != null && (headOperatingMode  = headThrd.getOperatingMode()) == RuleOperatingMode.READY){
      if(ruleSchedulerDebug){
			  System.out.println("Start executing "+ headThrd.getName() +"rule thread.");
				headThrd.print();
			}


			if(ruleSchedulerDebug){
			  System.out.println("start thread");
        System.out.println("Changing mode of "+headThrd.getName()+"from READY to EXE");
      }

		 	headThrd.setOperatingMode(RuleOperatingMode.EXE);
		 	headThrd.setScheduler(this);
      headThrd.start();

			headPriority =  headThrd.getRulePriority();
			headParentThrd =  headThrd.getParent();

		 	currntThrd =  headThrd.next;
     //     currntThrd =  headThrd;


			int	currntThrdPriority;
			Thread	currntThrdParent;  // a parent thread of this current rule thread
			int	currntThrdOperatingMode;

			// when neighbor of head node has the same priority and parent.

			while(currntThrd != null){
			  if(ruleSchedulerDebug)
  			  System.out.println("Find the neighbor node that has the same priority and parent");

					currntThrdPriority =	currntThrd.getRulePriority() ;
					currntThrdParent = currntThrd.getParent();
					currntThrdOperatingMode  = currntThrd.getOperatingMode() ;
          // case 1.1:
					if(currntThrdPriority == headPriority &&
						 currntThrdParent ==  headParentThrd &&
						 currntThrdOperatingMode == RuleOperatingMode.READY){

					if(ruleSchedulerDebug){
					  System.out.println(" start child thread =>");
						currntThrd.print();
            System.out.println("Changing mode of "+currntThrd.getName()+"from READY to EXE");

          }

  				currntThrd.setScheduler(this);
			    currntThrd.setOperatingMode(RuleOperatingMode.EXE);
          currntThrd.start();
				}
        // case 1.2:
				else if (currntThrdPriority == headPriority &&
				  currntThrdParent ==  headParentThrd &&
					currntThrdOperatingMode == RuleOperatingMode.EXE){

          yield();
        }
        // case 1.3:
    	else if (currntThrdPriority == headPriority &&
	    	  currntThrdParent ==  headParentThrd &&
		      currntThrdOperatingMode == RuleOperatingMode.WAIT){

          if(currntThrd.next == null){
            currntThrd.setOperatingMode(RuleOperatingMode.EXE);

//              ;
            // All its childs has been completed.
            // This currntThread's operating mode will be changed to FINISHED.
          }
					else{
            // check whether its neighbor is its child
						if(currntThrd.next.getParent() == currntThrd){

  					  if(ruleSchedulerDebug){
	  					  System.out.println("\n"+currntThrd.getName()+" call childRecurse "+currntThrd.next.getName());
		  					currntThrd.print();
              }
  						childRecurse(currntThrd,ruleQue);
            }
          }
        }
        // case 1.4:
				else if (currntThrdOperatingMode == RuleOperatingMode.FINISHED){

				  if(ruleSchedulerDebug){
					  System.out.println("delete "+currntThrd.getName() +" rule thread from rule queue.");
						currntThrd.print();
          }

          if(ruleQue instanceof TemporalRuleQueue){
            processRuleList.deleteRuleThread(currntThrd, temporalRuleQueue);
          }
          else if(ruleQue instanceof ImmRuleQueue){
            processRuleList.deleteRuleThread(currntThrd, immRuleQueue);
          }
          else if(ruleQue instanceof DefRuleQueue){
            processRuleList.deleteRuleThread(currntThrd, deffRuleQueueOne);
          }
	    		//			processRuleList.deleteRuleThread(currntThrd,ruleQue);
					currntThrd = currntThrd.next;

			  	while (currntThrd != null && currntThrd.getOperatingMode() == RuleOperatingMode.FINISHED){
				    if(ruleSchedulerDebug){
						  System.out.println("delete "+currntThrd.getName() +" rule thread from rule queue.");
							currntThrd.print();
            }
            if(ruleQue instanceof TemporalRuleQueue){
              processRuleList.deleteRuleThread(currntThrd, temporalRuleQueue);
            }
            else if(ruleQue instanceof ImmRuleQueue){
              processRuleList.deleteRuleThread(currntThrd, immRuleQueue);
            }
            else if(ruleQue instanceof DefRuleQueue){
              processRuleList.deleteRuleThread(currntThrd, deffRuleQueueOne);
            }
						//	processRuleList.deleteRuleThread(currntThrd,ruleQue);
						currntThrd = currntThrd.next;
          }
					break;
        }
				currntThrd = currntThrd.next;
      }
    }
     // when the head is in the EXE mode
    else if	((headThrd=ruleQue.getHead()) != null && (headOperatingMode  = headThrd.getOperatingMode()) == RuleOperatingMode.EXE){

      yield();
    }
		 // when the head is in the WAIT mode
    else	if	((headThrd=ruleQue.getHead()) != null && (headOperatingMode  = headThrd.getOperatingMode()) == RuleOperatingMode.WAIT){

      if(headThrd.next == null){
		       ;
			}
			else{
        if (headThrd.next.getParent() == headThrd){
          if(ruleSchedulerDebug){
					  System.out.println(headThrd.getName()+" call childRecurse "+headThrd.next.getName());
						headThrd.print();
          }

				  childRecurse(headThrd,ruleQue);
          if(ruleSchedulerDebug)
					  System.out.println(headThrd.getName()+" call childRecurse ");
        }
      }
      Thread.currentThread().yield();

    }

    else if ((headThrd=ruleQue.getHead()) != null && (headOperatingMode  = headThrd.getOperatingMode()) == RuleOperatingMode.FINISHED){
      if(ruleSchedulerDebug){
			  System.out.println("delete "+headThrd.getName() +" rule thread from rule queue.");
				headThrd.print();
      }
      if(ruleQue instanceof TemporalRuleQueue){
        processRuleList.deleteRuleThread(headThrd, temporalRuleQueue);
      }
      else if(ruleQue instanceof ImmRuleQueue){
        processRuleList.deleteRuleThread(headThrd, immRuleQueue);
      }
      else if(ruleQue instanceof DefRuleQueue){
        processRuleList.deleteRuleThread(headThrd, deffRuleQueueOne);
      }
			//processRuleList.deleteRuleThread(headThrd,ruleQue);
			headThrd = headThrd.next;

      while ((headThrd=ruleQue.getHead()) != null && (headOperatingMode  = headThrd.getOperatingMode()) == RuleOperatingMode.FINISHED){
			  if(ruleSchedulerDebug){
				  System.out.println("delete "+headThrd.getName() +" rule thread from rule queue.");
					headThrd.print();
        }
        if(ruleQue instanceof TemporalRuleQueue){
          processRuleList.deleteRuleThread(headThrd, temporalRuleQueue);
        }
        else if(ruleQue instanceof ImmRuleQueue){
          processRuleList.deleteRuleThread(headThrd, immRuleQueue);
        }
        else if(ruleQue instanceof DefRuleQueue){
          processRuleList.deleteRuleThread(headThrd, deffRuleQueueOne);
        }
				//processRuleList.deleteRuleThread(headThrd,ruleQue);
			  headThrd = headThrd.next;
      }
    }
    }
  }

  public void childRecurse(Thread Thrd, RuleQueue ruleQue){
    RuleThread tempThrd = null;
		RuleThread chdThrd;
		int ruleOperatingMode =0;
    if(ruleQue instanceof TemporalRuleQueue){
      tempThrd = temporalRuleQueue.getHead();
    }
    else if(ruleQue instanceof ImmRuleQueue){
      tempThrd = immRuleQueue.getHead();
    }
    else if(ruleQue instanceof DefRuleQueue){
      tempThrd = deffRuleQueueOne.getHead();
    }

    if(tempThrd != null){
      while(tempThrd != null){
        if (tempThrd == Thrd){
	      chdThrd = tempThrd.next;
		  ruleOperatingMode  = chdThrd.getOperatingMode();
          if(ruleSchedulerDebug)
			    System.out.print("Rule experation mode of child thread :"+ruleOperatingMode);

		  if(chdThrd == null){
		    return;
          }
          if(ruleOperatingMode == RuleOperatingMode.READY){
			int rulePriority = chdThrd.getRulePriority();
			Thread ruleParent = chdThrd.getParent();
			if(ruleParent == Thrd){
              if(ruleSchedulerDebug)
			    System.out.print("start child thread =>"); chdThrd.print();
    			chdThrd.setScheduler(this);
    			chdThrd.setOperatingMode(RuleOperatingMode.EXE);
    			chdThrd.start();
				chdThrd = chdThrd.next;

                while(chdThrd != null && chdThrd.getRulePriority() == rulePriority
  					   && ruleParent == Thrd && chdThrd.getCoupling() ==
      			     CouplingMode.IMMEDIATE){
                  if(ruleSchedulerDebug)
      			  	System.out.print(" start child thread =>");

                  chdThrd.print();
                  chdThrd.setScheduler(this);
                  if(chdThrd.getOperatingMode()== RuleOperatingMode.READY ){
                	chdThrd.setOperatingMode(RuleOperatingMode.EXE);
                    chdThrd.start();
                  }
  	              chdThrd = chdThrd.next;
              }
            }
          }
          else if(ruleOperatingMode == RuleOperatingMode.EXE){

            yield();
          } else if(ruleOperatingMode == RuleOperatingMode.FINISHED){
            if(ruleSchedulerDebug)
              System.out.println("delete rule from rule queue");
            chdThrd.print();
		    processRuleList.deleteRuleThread(chdThrd,immRuleQueue);
          }
          else if(ruleOperatingMode == RuleOperatingMode.WAIT){
            if(chdThrd.next == null){
            chdThrd.setOperatingMode(RuleOperatingMode.EXE);
  		      ;
            }
            else{
              if(chdThrd.next.getParent() == chdThrd){
                if(ruleSchedulerDebug)
  				  System.out.println(" call recursurse child");

                if(ruleQue instanceof TemporalRuleQueue){
                  childRecurse(chdThrd, (TemporalRuleQueue) ruleQue);
                }
                else if(ruleQue instanceof ImmRuleQueue){
                  childRecurse(chdThrd, (ImmRuleQueue) ruleQue);
                }
                else if(ruleQue instanceof DefRuleQueue){
                  childRecurse(chdThrd, (DefRuleQueue) ruleQue);
                }
              }
            }

            yield();
		  }
          return;
        }
		else{
	      tempThrd = tempThrd.next;
		}
      }
    }
  }


  // process in the temporal rule queue

  protected void processTemporalRule(){
    RuleThread currntThrd;
    Thread currntThrdParent; // the parent of the current thread;
    int currntOperatingMode;
    int currntThrdPriority  ;

    if( temporalRuleQueue.getHead()!= null){

      //If the rule thread is the top level, trigger the rule.
      currntThrd=temporalRuleQueue.getHead();
      while(currntThrd!= null){
        currntThrdPriority = currntThrd.getPriority ();
        currntThrdParent = currntThrd.getParent();

        if (currntThrd.getOperatingMode() == RuleOperatingMode.READY
             && !(currntThrd.getParent() == applThrd ||
                  currntThrd.getParent().getClass().getName().equals("EventDispatchThread")||
                  currntThrd.getParent().getName ().equals (Constant.LEDReceiverThreadName)
                 )){
          if(ruleSchedulerDebug)
		  		  System.out.println("Changing mode of "+currntThrd.getName()+"from READY to EXE");

          currntThrd.setOperatingMode(RuleOperatingMode.EXE);
				  currntThrd.setScheduler(this);
          currntThrd.start();
          int rulePriority = 0;
          currntThrdParent = currntThrd;
          if(currntThrdParent instanceof RuleThread){
            rulePriority = currntThrd.getRulePriority();
          }
          currntThrd = currntThrd.next;
          while(currntThrd != null && currntThrd instanceof RuleThread &&
                  currntThrd.getRulePriority() == rulePriority
  							   && currntThrd.getParent() == currntThrdParent ){
            if(ruleSchedulerDebug)
      			  System.out.print(" start child thread =>");

            currntThrd.print();
            currntThrd.setScheduler(this);
            if(	currntThrd.getOperatingMode()== RuleOperatingMode.READY ){
              currntThrd.setOperatingMode(RuleOperatingMode.EXE);
              currntThrd.start();
            }
  					currntThrd = currntThrd.next;
          }
        }
        // case 1.2:
        else if (currntThrd != null &&	currntThrd.getOperatingMode() == RuleOperatingMode.EXE){
        				currntThrd = currntThrd.next;

        }
        // case 1.3:
				else if (currntThrd != null && currntThrd.getOperatingMode() == RuleOperatingMode.WAIT){
				  if(currntThrd.next == null){
                  currntThrd.setOperatingMode(RuleOperatingMode.EXE);

//            ;
            // All its childs has been completed.
            // This currntThread's operating mode will be changed to FINISHED.
          }
					else{
            // check whether its neighbor is its child
						if(currntThrd.next.getParent() == currntThrdParent){
              if(ruleSchedulerDebug){
	  					  System.out.println("\n"+currntThrd.getName()+" call childRecurse "+currntThrd.next.getName());
		  					currntThrd.print();
              }

              childRecurse(currntThrd, temporalRuleQueue);
            }
          }
          currntThrd = currntThrd.next;
        }
        // case 1.4:
				else if (currntThrd != null && currntThrd.getOperatingMode() == RuleOperatingMode.FINISHED){
          if(ruleSchedulerDebug){
					  System.out.println("delete "+currntThrd.getName() +" rule thread from rule queue.");
						currntThrd.print();
          }
          processRuleList.deleteRuleThread(currntThrd,temporalRuleQueue);
        	currntThrd = currntThrd.next;
        }
        else{
  				currntThrd = currntThrd.next;
        }
      }
    }
  }


  void process_deff_rules(){
    if(immRuleQueue.getHead() == null){
			System.out.println("ALERT !! no a pre-defined rule (pro -1) in imm rule queue");
		}
		RuleThread head = immRuleQueue.getHead() ;
		if(head!= null){
			if(head.getRulePriority() == -1){
				if(head.next != null)
					System.out.println("ALERT !! no immediate rules allowed at this point\n");
			}
			else
		    System.out.println("ALERT !! no immediate rules allowed at this point\n");
		}
		deferredFlag = true;
    while(deffRuleQueueOne.getHead() != null);
    deferredFlag = false;
	}
  // added by wtanpisu on 24 Jan 2000
}

