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
 *  Created on: 12th December 2011
 */
package net.slate.gui

import javax.swing.{ BoxLayout, DefaultListModel, DefaultListCellRenderer, ImageIcon, JList, JScrollPane }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import scala.swing._
import net.slate.Size

/**
 * 
 */
class APILookupDialog extends Panel {
//  title = "API Docs"
  val SPACING = 5
  val pane = peer
  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS))
  val txtFind = new TextField(30)
  txtFind.editable = false
  pane.add(txtFind.peer)

  val listModel = new DefaultListModel
  val results = new JList(listModel)
  results.setCellRenderer(new APILookupRenderer)

  val resultsPane = new JScrollPane(results)
  resultsPane.setPreferredSize(Size(400, 600))
  pane.add(resultsPane)

  results.addMouseListener(new java.awt.event.MouseAdapter {
    override def mouseClicked(e: java.awt.event.MouseEvent) {
      if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
        val index = results.locationToIndex(e.getPoint());
        select(index)
      }
    }
  })

  val label = new Label
  pane.add(label.peer)

  def display(query: String, list: List[String]) {
    import net.slate.Launch._
    txtFind.text = "Showing Results for : " + query
    build(list)
    top.editorSplitPane.dividerLocation = Size.adjustHeight(700)
    peer.setVisible(true)
  }
  
  def build(list: List[String]) {
    listModel.clear
    list foreach listModel.addElement
  }
  
  def hide = peer.setVisible(false)

  private def select(index: Int) = {
    import net.slate.Launch._

    listModel.getElementAt(index) match {
      case x: String =>

        val text = x.substring(0, x.indexOf("@@@@@@"))

        val pane = currentScript.text
        pane.doc.insertString(pane.caret.position, text, null)
        peer.setVisible(false)
    }
  }

  /**
   * cell renderer for showing code options.
   */
  class APILookupRenderer extends DefaultListCellRenderer {
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
      val part = text.split("@@@@@@")
      
      text = "<html><body style='width:300px' ><font color=green>"+ part(0) + "</font> : <font color=black>"+ 
      part(1) + "</font> - <p><font color=gray>"+ part(2) + "</font></p><hr></body></html>"

      super.getListCellRendererComponent(list, text, index, iss, chf);

      setIcon(icon)

      this
    }
  }
}