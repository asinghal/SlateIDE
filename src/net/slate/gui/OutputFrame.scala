package net.slate.gui

import com.sun.awt.AWTUtilities._

class OutputFrame extends javax.swing.JFrame {
  setIconImage(net.slate.TrayIcon.icon)

  if (isTranslucencySupported(Translucency.TRANSLUCENT)) {
    try {
      setWindowOpacity(this, 0.8f)
    } catch {
      case e: Exception => // ignore this error. Opacity should not cause the system to fail.
    }
  }

  val outputPane = new Console

  //  setBackground(java.awt.Color.decode("0x363636"))
  //  setUndecorated(true)
  setMinimumSize(new java.awt.Dimension(321, 250))
  setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)

  val tk = java.awt.Toolkit.getDefaultToolkit();
  val screenSize = tk.getScreenSize();
  val WIDTH = screenSize.width;
  val HEIGHT = screenSize.height;
  // Setup the frame accordingly
  // This is assuming you are extending the JFrame //class
  this.setSize(WIDTH / 2, HEIGHT / 2);
  this.setLocation(WIDTH / 6, WIDTH / 6);

  outputPane.opaque = true
  add(outputPane.peer)
}