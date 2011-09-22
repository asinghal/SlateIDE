package net.slate.gui

import java.awt.{ Color, Dimension }
import scala.swing.{ Dialog, MainFrame }

abstract class BaseHelpDialog(frame: MainFrame) extends Dialog(frame.owner) {
  val pane = new TextPane

  background = Color.decode("#FFFFAA")
  
  peer.getContentPane.add(pane.peer)
  preferredSize = new Dimension(400, 300)
  pane.peer.setContentType("text/html")
  pane.peer.setEditable(false)
  peer.setModal(true)

  def text(t: String) = {
    pane.text = t
  }

  def display = {
    pack()
    peer.setLocationRelativeTo(frame.peer)

    peer.setVisible(true)
  }

  protected def read(file: String) = {
    val source = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(file), "UTF-8")
    val lines = source.mkString
    source.close()
    
    lines
  }
}

class HelpDialog(frame: MainFrame) extends BaseHelpDialog(frame) {
  val help = read("net/slate/gui/html/help.html")

  title = "Help"
  text(help)
}

class AboutDialog(frame: MainFrame) extends BaseHelpDialog(frame) {
  val about = read("net/slate/gui/html/about.html")

  title = "About"
  text(about)
}