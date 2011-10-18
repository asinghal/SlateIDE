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

import scala.swing.{ Action, Component }

import net.slate.Launch._

/**
 *
 * @author Aishwarya Singhal
 *
 */
object CodeSuggestionPopupMenu {
  import java.awt.event.KeyEvent
  import javax.swing.{ BorderFactory, DefaultListCellRenderer, ImageIcon, JList, JScrollPane, KeyStroke, Popup, PopupFactory }
  import net.slate.ExecutionContext
  import net.slate.editor.tools.{ CodeAssist, CodeTemplates, TypeIndexer }

  var popup: Popup = null

  def show(owner: Component, x: Int, y: Int) {
    val factory = PopupFactory.getSharedInstance()
    val word = CodeAssist.getWord
    
    val annotation = word._2.startsWith("@")
    val w = if (annotation) word._2.substring(1) else word._2
    val list = new TypeIndexer(ExecutionContext.currentProjectName).find(w, annotation)
    val contents = new JList(list)
    contents.setCellRenderer(new CodeSuggestionRenderer)
    val scrollpane = new JScrollPane(contents)
    scrollpane.setBackground(java.awt.Color.decode("0xffffff"))
    scrollpane.setViewportBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5))

    if (popup != null) popup.hide

    popup = factory.getPopup(owner.peer, scrollpane, 210 + x, 110 + y)
    contents.addMouseListener(new java.awt.event.MouseAdapter {
      override def mouseClicked(e: java.awt.event.MouseEvent) {
        if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          val index = contents.locationToIndex(e.getPoint());
          val pane = currentScript.text
          list(index) match {
            case x: String =>
              var text = x.substring(0, x.indexOf("-")).trim
              pane.doc.remove(word._1, pane.caret.position - word._1)
              if (CodeTemplates.map.contains(text)) {
                text = CodeTemplates.map(text)
              }
              
              text = if (annotation) "@" + text else text
              pane.doc.insertString(pane.caret.position, text, null)
          }
          restoreFocus
        }
      }

    })

    contents.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeCodeAssist");
    contents.getActionMap().put("closeCodeAssist", new javax.swing.AbstractAction {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        restoreFocus
      }
    });
    popup.show()
  }

  def restoreFocus = {
    javax.swing.SwingUtilities.invokeLater(new Runnable {
      def run = {
        popup.hide
        currentScript.text.peer.requestFocus
      }
    })
  }

  /**
   * cell renderer for showing code options.
   */
  class CodeSuggestionRenderer extends DefaultListCellRenderer {
    val icon = new ImageIcon("images/img_bullet_blue.png");

    /* This is the only method defined by ListCellRenderer.  We just
     * reconfigure the Jlabel each time we're called.
     */
    override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, iss: Boolean, chf: Boolean) = {
      /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
      super.getListCellRendererComponent(list, value, index, iss, chf);

      /* We additionally set the JLabels icon property here.
         */
      setIcon(icon);

      this
    }
  }
}
