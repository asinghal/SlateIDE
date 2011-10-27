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

import java.awt.FlowLayout
import java.io.File
import javax.swing.{ BoxLayout, DefaultListModel, DefaultListCellRenderer, ImageIcon, JList, JPanel, JScrollPane, JTextField }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

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
  results.setCellRenderer(new LookUpResourceRenderer)

  val resultsPane = new JScrollPane(results)
  resultsPane.setPreferredSize(new Dimension(400, 400))
  pane.add(resultsPane)

  results.addMouseListener(new java.awt.event.MouseAdapter {
    override def mouseClicked(e: java.awt.event.MouseEvent) {
      if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
        val index = results.locationToIndex(e.getPoint());
        open(index)
      }
    }
  })

  results.addListSelectionListener(new ListSelectionListener {
    override def valueChanged(evt: ListSelectionEvent) = {
      if (!evt.getValueIsAdjusting()) {
        val index = evt.getLastIndex
        if (listModel.size > index) label.text = listModel.getElementAt(index).asInstanceOf[String]
      }
    }
  })

  val label = new Label
  pane.add(label.peer)
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
      label.text = if (!l.isEmpty) l(0) else ""
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
    txtFind.text = ""
    txtFind.requestFocus()
    txtFind.selectAll()
    peer.setLocationRelativeTo(frame.peer)

    peer.setVisible(true)
  }

  /**
   * cell renderer for showing code options.
   */
  class LookUpResourceRenderer extends DefaultListCellRenderer {
    val icon = new ImageIcon("images/img_bullet_blue.png");

    /* This is the only method defined by ListCellRenderer.  We just
     * reconfigure the Jlabel each time we're called.
     */
    override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, iss: Boolean, chf: Boolean) = {
      /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
      var text = value.asInstanceOf[String]
      text = text.substring(text.lastIndexOf(File.separator) + 1)
      super.getListCellRendererComponent(list, text, index, iss, chf);

      /* We additionally set the JLabels icon property here.
         */
      setIcon(icon);

      this
    }
  }
}