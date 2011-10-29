/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
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

  var operationCategory: CreationCategory = _

  val pane = new JPanel(new GridBagLayout)
  pane.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING))
  peer.getContentPane.add(pane)

  val pkgField = new TextField("", 30)
  val nameField = new TextField("", 30)
  val superClassField = new TextField("", 30)
  val traitField = new TextField("", 30)
  val interfaceField = new TextField("", 30)

  val packageNameLabel = new Label("Package Name")
  val classNameLabel = new Label("Class Name")
  val superClassLabel = new Label("Super class")
  val traitLabel = new Label("Inherited Trait")
  val interfaceLabel = new Label("Implemented Interface")

  pane.add(packageNameLabel.peer, constraint(0, 0, 1))
  pane.add(pkgField.peer, constraint(1, 0, 3))

  pane.add(classNameLabel.peer, constraint(0, 1, 1))
  pane.add(nameField.peer, constraint(1, 1, 3))

  pane.add(superClassLabel.peer, constraint(0, 2, 1))
  pane.add(superClassField.peer, constraint(1, 2, 3))

  pane.add(traitLabel.peer, constraint(0, 3, 1))
  pane.add(traitField.peer, constraint(1, 3, 3))

  pane.add(interfaceLabel.peer, constraint(0, 3, 1))
  pane.add(interfaceField.peer, constraint(1, 3, 3))

  val butCreate = new Button("Finish")
  val butCancel = new Button("Cancel")

  pane.add(butCreate.peer, constraint(0, 5, 1))
  pane.add(butCancel.peer, constraint(1, 5, 1))

  listenTo(butCreate, butCancel)
  reactions += {
    case ButtonClicked(`butCreate`) =>

      import java.io.File

      val filename = nameField.text.trim + operationCategory.extension
      val path = targetPath + File.separator + filename
      var directory = false
      if (templateName == "GeneralFolder") {
        directory = true
        if (!new File(path).exists) new File(path).mkdir
      } else {
        val source = scala.io.Source.fromFile("templates/" + templateName)
        var text = source.mkString
        source.close()

        val name = nameField.text.trim
        val SEMI_COLON = ";"

        text = if (pkgField.text.trim != "") { text.replace("${packageName}", "package " + pkgField.text + SEMI_COLON) } else text.replace("${packageName}", "")
        text = if (name != "") { text.replace("${className}", name) } else text.replace("${className}", "NewClass")
        text = if (superClassField.text.trim != "") { text.replace("${extends}", "extends " + superClassField.text) } else text.replace("${extends}", "")
        text = if (traitField.text.trim != "") { text.replace("${traits}", "with " + traitField.text) } else text.replace("${traits}", "")
        text = if (interfaceField.text.trim != "") { text.replace("${interfaces}", "implements " + traitField.text) } else text.replace("${interfaces}", "")
        text = text.replace("${date}", (new java.util.Date).toString)
        text = text.replace("${username}", System.getProperty("user.name"))
        text = text.replace("${importStatements}", "")

        val fw = new java.io.FileWriter(path)
        fw.write(text)
        fw.close()

        addTab(filename, path)
        currentScript.text.text = text
      }
      peer.setVisible(false)

      top.fileExplorer.addNewNode(targetNodeRow, filename, path, directory)
    case ButtonClicked(`butCancel`) =>
      peer.setVisible(false)
  }

  def display(template: String, path: String, nodeRow: Int) {
    templateName = template
    targetPath = path
    targetNodeRow = nodeRow

    operationCategory = if (templateName.startsWith("Java")) (new JavaFile) else if (templateName.startsWith("General")) (new GeneralFile) else (new ScalaFile)
    operationCategory.display

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

  trait CreationCategory {
    def extension: String
    def display
  }

  case class ScalaFile() extends CreationCategory {
    def extension = ".scala"
    def display {
      classNameLabel.text = "Class Name"
      packageNameLabel.visible = true
      pkgField.visible = true
      superClassLabel.visible = true
      superClassField.visible = true
      traitLabel.visible = true
      traitField.visible = true
      interfaceLabel.visible = false
      interfaceField.visible = false
    }
  }

  case class JavaFile() extends CreationCategory {
    def extension = ".java"
    def display {
      classNameLabel.text = "Class Name"
      packageNameLabel.visible = true
      pkgField.visible = true
      superClassLabel.visible = true
      superClassField.visible = true
      traitLabel.visible = false
      traitField.visible = false
      interfaceLabel.visible = true
      interfaceField.visible = true
    }
  }

  case class GeneralFile() extends CreationCategory {
    def extension = ""
    def display {
      classNameLabel.text = "Name"
      packageNameLabel.visible = false
      pkgField.visible = false
      superClassLabel.visible = false
      superClassField.visible = false
      traitLabel.visible = false
      traitField.visible = false
      interfaceLabel.visible = false
      interfaceField.visible = false
    }
  }
}