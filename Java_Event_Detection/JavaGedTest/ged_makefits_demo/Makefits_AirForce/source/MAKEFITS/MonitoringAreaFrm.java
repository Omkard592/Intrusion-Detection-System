package MAKEFITS;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.borland.jbcl.layout.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class MonitoringAreaFrm extends JFrame {
  XYLayout xYLayout1 = new XYLayout();
  JPanel jPanel1 = new JPanel();
  JTabbedPane alertTabbedPane = new JTabbedPane();
  JPanel alertPanel = new JPanel();
  JPanel trackInfoPanel = new JPanel();
  JScrollPane alertScrollPane1 = new JScrollPane();
  JScrollPane trackInfoScrollPane1 = new JScrollPane();


  public MonitoringAreaFrm() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
   this.setResizable(false);
    setTitle("ImageTest");
    this.getContentPane().setLayout(xYLayout1);
   setSize(700,334);
   addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e){
        System.exit(0);
      }
    });

//   Container contentPane = getContentPane();
   xYLayout1.setWidth(699);
   xYLayout1.setHeight(333);


//    alertTabbedPane.addTab(");

  // this.getContentPane().add(jPanel1, new XYConstraints(-1, 3, 510, 328));



   this.getContentPane().add(alertTabbedPane, new XYConstraints(513, 3, 183, 327));


//   alertTabbedPane.add(alertPanel, "alertPanel");

//   alertTabbedPane.add(new   TrackLogger(), "Alert");
//   alertPanel.add(alertScrollPane1, null);

//   alertTabbedPane.add(trackInfoPanel, "Track Info");

//   alertTabbedPane.add(new   TrackLogger(), "Track Info");
//   trackInfoPanel.add(trackInfoScrollPane1, null);

  }


  public void addMonitoringMap(JPanel monitoringMap){
     this.getContentPane().add(monitoringMap, new XYConstraints(-1, 3, 510, 328));

  }
  public void addLogger(TrackLogger logger, String loggerName){
  //   alertTabbedPane.add(alertPanel, "alertPanel");
   alertTabbedPane.add(logger,loggerName);
   alertPanel.add(alertScrollPane1, null);
  }
}
