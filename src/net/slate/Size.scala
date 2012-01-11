package net.slate

import scala.swing.Dimension
import java.awt.{ Toolkit }

object Size {

  val toolkit = Toolkit.getDefaultToolkit
  val screen = toolkit.getScreenSize
  
  val w = screen.width.toDouble / 1280
  val h = screen.height.toDouble / 800

  def apply(x: Int, y: Int) = {
    new Dimension((x * w).toInt, (y * h).toInt)
  }
}