package net.slate.gui
import javax.swing.BorderFactory

import java.awt.{ GridBagConstraints, GridBagLayout }

import javax.swing.{ JLabel, JPanel, JTextField }
import scala.swing._
import scala.swing.event._

import net.slate.Launch._

class NewItemDetailsDialog(frame: MainFrame) extends Dialog(frame.owner) {
  title = "New"
  val SPACING = 5

  var templateName = ""
  var targetPath = ""
  var targetNodeRow = -1

  val pane = new JPanel(new GridBagLayout)
  pane.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING))
  peer.getContentPane.add(pane)

  val pkgField = new TextField("", 30)
  val nameField = new TextField("", 30)
  val superClassField = new TextField("", 30)
  val traitField = new TextField("", 30)

  pane.add(new Label("Package Name").peer, constraint(0, 0, 1))
  pane.add(pkgField.peer, constraint(1, 0, 3))

  pane.add(new Label("Class Name").peer, constraint(0, 1, 1))
  pane.add(nameField.peer, constraint(1, 1, 3))

  pane.add(new Label("Super class").peer, constraint(0, 2, 1))
  pane.add(superClassField.peer, constraint(1, 2, 3))

  pane.add(new Label("Inherited Trait").peer, constraint(0, 3, 1))
  pane.add(traitField.peer, constraint(1, 3, 3))

  val butCreate = new Button("Finish")
  val butCancel = new Button("Cancel")

  pane.add(butCreate.peer, constraint(0, 5, 1))
  pane.add(butCancel.peer, constraint(1, 5, 1))

  listenTo(butCreate, butCancel)
  reactions += {
    case ButtonClicked(`butCreate`) =>
      val source = scala.io.Source.fromFile("templates/" + templateName)
      var text = source.mkString
      source.close()

      val name = nameField.text.trim
      val filename = nameField.text.trim + ".scala"

      text = if (pkgField.text.trim != "") { text.replace("${packageName}", "package " + pkgField.text) } else text.replace("${packageName}", "")
      text = if (name != "") { text.replace("${className}", name) } else text.replace("${className}", "NewClass")
      text = if (superClassField.text.trim != "") { text.replace("${extends}", "extends " + superClassField.text) } else text.replace("${extends}", "")
      text = if (traitField.text.trim != "") { text.replace("${traits}", "with " + traitField.text) } else text.replace("${traits}", "")
      text = text.replace("${date}", (new java.util.Date).toString)
      text = text.replace("${username}", System.getProperty("user.name"))
      text = text.replace("${importStatements}", "")

      val path = targetPath + java.io.File.separator + filename
      val fw = new java.io.FileWriter(path)
      fw.write(text)
      fw.close()

      addTab(filename, path)
      currentScript.text.text = text
      peer.setVisible(false)

      top.fileExplorer.addNewNode(targetNodeRow, filename, path, false)

    case ButtonClicked(`butCancel`) =>
      peer.setVisible(false)
  }

  //  preferredSize = new Dimension(500, 300)

  def display(template: String, path: String, nodeRow: Int) {
    templateName = template
    targetPath = path
    targetNodeRow = nodeRow
    pack()
    pkgField.requestFocus()
    pkgField.selectAll()

    peer.setLocationRelativeTo(frame.peer)
    peer.setVisible(true)
  }

  private def constraint(x: Int, y: Int, width: Int) = {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.gridx = x
    c.gridy = y
    c.gridwidth = width

    c
  }

}