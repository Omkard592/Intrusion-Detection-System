/**
 * Table.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.*;
import java.io.*;

/** The abstract Table class is extended by the PCTable class that denotes an
 *  event table. It contains two abstract methods for propagating primitive and
 *  composite events respectively. The PCTable class provides implementation for
 *  these methods.
 */
abstract public class Table implements Serializable {

   transient private static boolean mainDebug = Utilities.isDebugFlagTrue("mainDebug");

    Vector pcEntries;

    public void print() {
       PCEntry entry;
       if(mainDebug)
        System.out.println("Number of entries in Table = " + pcEntries.size());
       for (int i=0; i<pcEntries.size(); i++) {
         entry = (PCEntry) pcEntries.elementAt(i);
         if(mainDebug)
         entry.print();
       }
    }

    Enumeration elements() {
	return pcEntries.elements();
    }

    Vector getEventSets() {
	return pcEntries;
    }

    void add(Object o) {
	pcEntries.addElement(o);
    }

    int size() {
	return pcEntries.size();
    }

    abstract public void getPropagation(ParameterList paramList, byte context);
    abstract public void getCompositePropagation(EventSet eventSet,int context);
}

