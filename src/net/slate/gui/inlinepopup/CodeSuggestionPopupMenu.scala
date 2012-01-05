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
object CodeSuggestionPopupMenu extends CommonPopup {
  import java.awt.event.KeyEvent
  import javax.swing.{ BorderFactory, DefaultListCellRenderer, ImageIcon, JList, JScrollPane, KeyStroke, PopupFactory }
  import net.slate.ExecutionContext
  import net.slate.editor.tools.{ CodeAssist, CodeTemplates, TypeIndexer }
  import scala.actors.Actor._

  def show(owner: Component, x: Int, y: Int) {
    val word = CodeAssist.getWord

    val annotation = word._2.startsWith("@")
    val w = if (annotation) word._2.substring(1) else word._2
    val list = new TypeIndexer(ExecutionContext.currentProjectName).find(w, false)
    def insert(index: Int) = {
      val pane = currentScript.text
      list(index) match {
        case x: String =>
          var text = x.substring(0, x.indexOf("-")).trim
          pane.doc.remove(word._1, pane.caret.position - word._1)
          if (CodeTemplates.map.contains(text)) {
            text = CodeTemplates.map(text)
          }

          text = if (annotation) "@" + text else text
          pane.doc.insertString(word._1, text, null)
          pane.peer.setCaretPosition(word._1 + text.length)
      }
      restoreFocus
    }
    
    showPopup(list) { (i, l) => insert(i) }
  }
}
