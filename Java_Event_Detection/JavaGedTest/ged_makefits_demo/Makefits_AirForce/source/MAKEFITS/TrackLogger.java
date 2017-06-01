package MAKEFITS;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Title:        Makefits
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      SSC-SD
 * @author       Robert Yowell, Weera Tanpisuth
 * @version
 */

public class TrackLogger extends JPanel{

  JList fLog = new JList(new DefaultListModel());

  static int x = 0;
  public TrackLogger() {
    JScrollPane sp = new JScrollPane(fLog);
    sp.setPreferredSize(new Dimension(180,270));
    this.add(sp);
  }

  public void log(String msg) {
    ((DefaultListModel)fLog.getModel()).addElement(msg);
    fLog.repaint();
  }
}