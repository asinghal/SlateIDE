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
 *  Created on: 28th December 2011
 */
package net.slate.gui

trait CommonPopup extends InlinePopup {
  import scala.swing.Component
  import java.awt.event.KeyEvent
  import javax.swing.{ BorderFactory, DefaultListCellRenderer, ImageIcon, PopupFactory, JList, JScrollPane, KeyStroke }
  import scala.actors.Actor._

  lazy val popupName = "simplePopup"
    
  protected def showPopup(list: Array[AnyRef])(f: (Int, Array[AnyRef]) => Unit) {
    if (!list.isEmpty) {
      val factory = PopupFactory.getSharedInstance()
      val contents = new JList(list)
      contents.setCellRenderer(new SimplePopupRenderer)

      val scrollpane = new JScrollPane(contents)
      scrollpane.setBackground(java.awt.Color.decode("0xffffff"))
      scrollpane.setViewportBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5))

      hide

      popup = getPopup(scrollpane)
      contents.setSelectedIndex(0)
      contents.addMouseListener(new java.awt.event.MouseAdapter {
        override def mouseClicked(e: java.awt.event.MouseEvent) {
          if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            val index = contents.locationToIndex(e.getPoint());
            f(index, list)
          }
        }
      })

      processor = actor {
        react {
          case _ =>
            f(contents.getSelectedIndex, list)
        }
      }

      contents.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), popupName);
      contents.getActionMap().put(popupName, new javax.swing.AbstractAction {
        def actionPerformed(e: java.awt.event.ActionEvent) {
          restoreFocus
        }
      });
      popup.show()
    } else {
      hide
    }
  }

  def getPopup(scrollpane: JScrollPane) = {
    import net.slate.Launch._

    val pane = currentScript
    val point = pane.text.peer.getCaret.getMagicCaretPosition
    val editor = pane.peer.getViewport.getViewPosition
    val x = if (point != null) (point.getX.asInstanceOf[Int] - editor.getX.asInstanceOf[Int] + 50) else 50
    val y = if (point != null) (point.getY.asInstanceOf[Int] - editor.getY.asInstanceOf[Int] + 10) else 10

    val factory = PopupFactory.getSharedInstance()
    factory.getPopup(pane.peer, scrollpane, 210 + x, 110 + y)
  }

  /**
   * cell renderer for showing code options.
   */
  class SimplePopupRenderer extends DefaultListCellRenderer {
    val icon = new ImageIcon("images/img_bullet_green.png");

    override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, iss: Boolean, chf: Boolean) = {
      val text = value.asInstanceOf[String].replace("{{}}", ":")

      super.getListCellRendererComponent(list, text, index, iss, chf);
      setIcon(icon)
      this
    }
  }
}