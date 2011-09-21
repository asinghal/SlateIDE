package net.slate.gui

import java.awt.{ Color, Dimension, Font }
import javax.swing.{ BorderFactory, JPopupMenu }
import javax.swing.border._
import scala.swing.{ Action, Menu, Component, MenuItem }
import scala.swing.SequentialContainer.Wrapper

import net.slate.Launch

object PopupMenu {
  private[PopupMenu] trait JPopupMenuMixin { def popupMenuWrapper: PopupMenu }
}

class PopupMenu extends Component with Wrapper {

  override lazy val peer: JPopupMenu = new JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int): Unit = peer.show(invoker.peer, x, y)

  /* Create any other peer methods here */
}

class EditorPopupMenu extends PopupMenu with MenuPainter {
  import net.slate.action._

  contents += new Menu("Source") with MenuFont {
    contents += new MenuItem("Format") with MenuFont { peer.addActionListener(new FormatFileAction) }
    contents += new MenuItem("Organise Imports") with MenuFont
  }
  contents += new Menu("Execute") with MenuFont {
    contents += new MenuItem("Run") with MenuFont
    contents += new MenuItem("Debug") with MenuFont
  }
}

class ProjectTreeMenu extends PopupMenu with MenuPainter {
  var path = ""
  lazy val form = new NewItemDetailsDialog(Launch.top)
  var nodeRow = -1

  preferredSize = new Dimension(100, 100)

  contents += new Menu("New") with CreateNewItemMenuItem {
    contents += new MenuItem(createNewItemAction("Scala Class", "ScalaClass")) with CreateNewItemMenuItem
    contents += new MenuItem(createNewItemAction("Scala Object", "ScalaObject")) with CreateNewItemMenuItem
    contents += new MenuItem(createNewItemAction("Scala Trait", "ScalaTrait")) with CreateNewItemMenuItem
  }

  def createNewItemAction(text: String, templateName: String) = new Action(text) {
    def apply() {
      form.display(templateName, path, nodeRow)
    }
  }
}

trait MenuPainter {
  self: Component =>
}

trait MenuFont {
  self: Component =>
  border = BorderFactory.createEmptyBorder
}

trait CreateNewItemMenuItem {
  self: Component =>
  val itemSize = new Dimension(100, 20)
  minimumSize = itemSize
  maximumSize = itemSize
}