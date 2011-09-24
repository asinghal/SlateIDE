package net.slate.gui

import java.awt.{ BorderLayout, FlowLayout }
import java.io.File
import javax.swing.{ BorderFactory, BoxLayout, DefaultListModel, DefaultListCellRenderer, ImageIcon, JList, JPanel, JScrollPane, JTextField }
import scala.swing._
import scala.swing.event.{ ButtonClicked, KeyReleased }

import net.slate.Launch
import net.slate.builder.{ JavaBuilder, ScalaBuilder }
import net.slate.util.FileUtils

/**
 *
 *
 */
class RunDialog(frame: MainFrame) extends Dialog(frame.owner) {
  title = "Run"
  val SPACING = 5
  val pane = new JPanel
  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS))
  peer.getContentPane.add(pane)
  val txtFind = new TextField(30)
  pane.add(txtFind.peer)

  val radScala = new RadioButton("Scala") {
    selected = true
  }
  val radJava = new RadioButton("Java")

  val pnlDirection = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
  pnlDirection.setBorder(BorderFactory.createTitledBorder("Type"))
  pane.add(pnlDirection, BorderLayout.EAST)
  pnlDirection.add(radScala.peer)
  pnlDirection.add(radJava.peer)

  val buttonPanel = new JPanel(new FlowLayout)
  pane.add(buttonPanel)
  val btnRun = new Button("Run")
  val btnCancel = new Button("Cancel")
  buttonPanel.add(btnRun.peer)
  buttonPanel.add(btnCancel.peer)

  listenTo(btnRun, btnCancel)
  reactions += {
    case ButtonClicked(`btnRun`) =>
    import net.slate.ExecutionContext._

      if (txtFind.text.trim != "") {
        peer.setVisible(false)
        val project = currentProjectName
        if (radScala.selected) ScalaBuilder.run(project, txtFind.text)
        if (radJava.selected) JavaBuilder.run(project, txtFind.text)
      }

    case ButtonClicked(`btnCancel`) =>
      peer.setVisible(false)
  }

  def display() {
    pack()
    txtFind.requestFocus()
    txtFind.selectAll()
    peer.setLocationRelativeTo(frame.peer)

    peer.setVisible(true)
  }
}