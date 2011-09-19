package net.slate.gui

import java.io.File
import javax.swing.{ JFileChooser, JToolBar }
import scala.swing._
import net.slate.Launch

class ToolBar(title: String) extends Component with SequentialContainer.Wrapper {

  override lazy val peer: JToolBar = new JToolBar(title)

  def add(action: Action) { peer.add(action.peer) }

  def add(component: Component) { peer.add(component.peer) }
}

class NavigationToolBar extends ToolBar("Navigation") {

  val component = this
  add(new Action("New Project") {
    icon = new javax.swing.ImageIcon("images/newjprj_wiz.gif")

    def apply() {
      val chooser = ProjectOpener.chooser
      val returnVal = chooser.showOpenDialog(component)
      Launch.top.fileExplorer.openProject(chooser.selectedFile)
    }
  })
}

object ProjectOpener {
  val chooser = new FileChooser(new File("."))
  chooser.peer.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
}