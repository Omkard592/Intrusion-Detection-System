// Author          : Seokwon Yang
// Created On      : Wed Jul 21 16:15:04 1999
// Last Modified By: Seokwon Yang
// Last Modified On: Wed Jul 21 16:27:22 1999
// Copyright (C) University of Florida 1999
// 


package MAKEFITS;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Logger extends JDialog {

   JList fLog = new JList(new DefaultListModel());

  static int x = 0;

   public Logger() {
      JScrollPane sp = new JScrollPane(fLog);
      getContentPane().add(sp);
      if(x == 0){
        setSize(300, 400);
        x++;
      }

      else{
        setSize(400, 300);
      }

   	  show();

    /*  addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
	  });*/
   }

   public void log(String msg) {
     ((DefaultListModel)fLog.getModel()).addElement(msg);
      fLog.repaint(); 
   }

  public static void main(String[] args)
  {
    Logger a = new Logger();
    a.setVisible(true);
    //while (true) {
      a.log("Sentinel group");
    //}
    
  }
  
}   




















