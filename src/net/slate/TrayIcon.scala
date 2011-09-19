package net.slate

import java.awt.event.{ MouseEvent, MouseAdapter }
import java.awt.{ SystemTray, Toolkit }
import scala.util.Properties.isMac

object TrayIcon {
  val icon = Toolkit.getDefaultToolkit.createImage("images/ide.gif")

  val supported = SystemTray.isSupported && !isMac

  def init() {
    if (supported) {
      val trayIcon = new java.awt.TrayIcon(icon, "Scala Cafe")
      SystemTray.getSystemTray.add(trayIcon)
      trayIcon.addMouseListener(new MouseAdapter() {
        override def mouseClicked(e: MouseEvent) {
          if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount == 2) {
            val main = Launch.top
            main.visible = !main.visible
            if ((main.peer.getExtendedState & java.awt.Frame.ICONIFIED) != 0) main.uniconify()
          }
        }
      })
    }
  }
}