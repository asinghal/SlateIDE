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
 *  Created on: 3rd October 2011
 */
package net.slate.gui

/**
 * Pop up menu displaying word suggestions.
 *
 * @author Aishwarya Singhal
 */
object WordCompletionPopupMenu extends CommonPopup {
  import java.awt.event.KeyEvent
  import javax.swing.{ BorderFactory, DefaultListCellRenderer, ImageIcon, JList, JScrollPane, KeyStroke, PopupFactory }
  import scala.swing.Component
  import net.slate.Launch._
  import net.slate.editor.tools.{ CodeAssist, WordCompletion }
  import scala.actors.Actor._

  override lazy val popupName = "wordCompletionPopup"

  /**
   * Display the list of words that can be substituted at the current cursor position.
   *
   * @param owner
   * @param x
   * @param y
   */
  def show {
    val factory = PopupFactory.getSharedInstance()
    val textpane = currentScript.text.peer
    val caret = textpane.getCaretPosition
    val currentChar = if (caret == 0) ' ' else (textpane.getDocument.getText(caret - 1, 1)).charAt(0)
    val word = CodeAssist.getWord
    val list = if (currentChar != '.' && Character.isLetterOrDigit(currentChar)) WordCompletion.suggest(word._2) else Array[AnyRef]()
    
    def insert(index: Int) = {
        val pane = currentScript.text
        list(index) match {
          case text: String =>
            pane.doc.remove(word._1, pane.caret.position - word._1)
            pane.doc.insertString(word._1, text, null)
            pane.peer.setCaretPosition(word._1 + text.length)
        }
        restoreFocus
      }
    
    showPopup(list) { (i,l) => insert(i)}
  }
}