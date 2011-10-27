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
import java.io.File

import javax.swing.{ JLabel, JPanel, JTextArea, JTextField }
import scala.swing._
import scala.swing.event._

import net.slate.Launch._
import net.slate.builder.ProjectConfigurator

class NewProjectDialog(frame: MainFrame) extends Dialog(frame.owner) {
  title = "New"
  val SPACING = 5

  var templateName = ""
  var targetPath = ""
  var targetNodeRow = -1

  val pane = new JPanel(new GridBagLayout)
  pane.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING))
  peer.getContentPane.add(pane)

  val pathField = new TextField("", 30)
  val nameField = new TextField("", 30)
  val srcField = new TextField("", 30)
  val testField = new TextField("", 30)
  val outputField = new TextField("", 30)
  val libField = new TextField("", 30)
  val classpathField = new JTextArea("", 5, 30)

  pane.add(new Label("Path").peer, constraint(0, 0, 1))
  pane.add(pathField.peer, constraint(1, 0, 3))

  pane.add(new Label("Name").peer, constraint(0, 1, 1))
  pane.add(nameField.peer, constraint(1, 1, 3))

  pane.add(new Label("Source code directory").peer, constraint(0, 2, 1))
  pane.add(srcField.peer, constraint(1, 2, 3))

  pane.add(new Label("Test code directory").peer, constraint(0, 3, 1))
  pane.add(testField.peer, constraint(1, 3, 3))

  pane.add(new Label("Output directory").peer, constraint(0, 4, 1))
  pane.add(outputField.peer, constraint(1, 4, 3))

  pane.add(new Label("Lib directory").peer, constraint(0, 5, 1))
  pane.add(libField.peer, constraint(1, 5, 3))

  pane.add(new Label("Classpath").peer, constraint(0, 10, 1))
  pane.add(classpathField, constraint(1, 10, 3))

  val butCreate = new Button("Finish")
  val butCancel = new Button("Cancel")

  pane.add(butCreate.peer, constraint(0, 16, 1))
  pane.add(butCancel.peer, constraint(1, 16, 1))

  listenTo(butCreate, butCancel)
  reactions += {
    case ButtonClicked(`butCreate`) =>
      peer.setVisible(false)

      val path = pathField.text
      val name = nameField.text
      val src = srcField.text
      val test = testField.text
      val lib = libField.text
      val output = outputField.text
      val project = path + File.separator + name

      create(project)
      create(project + File.separator + src)
      create(project + File.separator + test)
      create(project + File.separator + lib)
      create(project + File.separator + output)

      ProjectConfigurator.init(new File(project), src, test, lib, output)

      top.fileExplorer.openProject(new File(project))

    case ButtonClicked(`butCancel`) =>
      peer.setVisible(false)
  }

  def display {
    pack()
    clear

    pathField.requestFocus()
    pathField.selectAll()

    peer.setLocationRelativeTo(frame.peer)
    peer.setVisible(true)
  }

  private def create(path: String) = {
    val dir = new File(path)
    println(path)

    if (!dir.exists) dir.mkdir
  }

  private def clear {
    pathField.text = new File(".").getAbsolutePath
    nameField.text = ""
    srcField.text = "src"
    testField.text = "test"
    libField.text = "lib"
    outputField.text = "bin"
    classpathField.setText("")
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