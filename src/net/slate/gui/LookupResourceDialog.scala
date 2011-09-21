package net.slate.gui

import java.awt.{ FlowLayout }
import java.io.File
import javax.swing.{ BoxLayout, DefaultListModel, JList, JPanel, JScrollPane, JTextField }
import scala.swing._
import scala.swing.event.{ ButtonClicked, KeyReleased }

import net.slate.{ ExecutionContext, Launch }
import net.slate.util.FileUtils

/**
 *
 *
 */
class LookupResourceDialog(frame: MainFrame) extends Dialog(frame.owner) {
  private var projectFiles = List[String]()

  title = "Open"
  val SPACING = 5
  val pane = new JPanel
  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS))
  peer.getContentPane.add(pane)
  val txtFind = new TextField(30)
  pane.add(txtFind.peer)

  val listModel = new DefaultListModel
  val results = new JList(listModel)
  val resultsPane = new JScrollPane(results)
  resultsPane.setPreferredSize(new Dimension(300, 300))
  pane.add(resultsPane)

  results.addMouseListener(new java.awt.event.MouseAdapter {
    override def mouseClicked(e: java.awt.event.MouseEvent) {
      if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
        val index = results.locationToIndex(e.getPoint());
        open(index)
      }
    }
  })

  val buttonPanel = new JPanel(new FlowLayout)
  pane.add(buttonPanel)
  val btnOpen = new Button("Open")
  val btnCancel = new Button("Cancel")
  buttonPanel.add(btnOpen.peer)
  buttonPanel.add(btnCancel.peer)

  listenTo(txtFind.keys, btnOpen, btnCancel)
  reactions += {
    case KeyReleased(`txtFind`, _, _, _) =>
      val l = projectFiles.filter { f => f.substring(f.lastIndexOf(File.separator) + 1).toLowerCase.startsWith(txtFind.peer.getText.toLowerCase) }
      listModel.clear

      l.foreach { listModel.addElement(_) }
    case ButtonClicked(`btnOpen`) =>
      val index = results.getSelectedIndex()
      if (index != -1) {
        open(index)
      }
    case ButtonClicked(`btnCancel`) =>
      peer.setVisible(false)
      projectFiles = null
  }

  private def open(index: Int) = {
    listModel.getElementAt(index) match {
      case x: String => FileUtils.open(FileUtils.getSimpleName(x), x)
    }
    peer.setVisible(false)
  }

  def display() {
    projectFiles = FileUtils.findAllFiles(ExecutionContext.currentProjectName, null)
    pack()
    txtFind.requestFocus()
    txtFind.selectAll()
    peer.setLocationRelativeTo(frame.peer)

    peer.setVisible(true)
  }
}