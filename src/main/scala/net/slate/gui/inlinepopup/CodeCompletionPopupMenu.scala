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
 *  Created on: 7th October 2011
 */
package net.slate.gui

import scala.swing.{ Action, Component }
import net.slate.Launch._

/**
 * Used to provide completions for import names and make suggestions for method/ attribute names.
 *
 * @author Aishwarya Singhal
 */
object CodeCompletionPopupMenu extends CommonPopup {
  import net.slate.editor.tools.CodeAssist

  def show(list: Array[AnyRef], typeNameCompletion: Boolean = true) {
    def insert(index: Int, list: Array[AnyRef]) = {
      val pane = currentScript.text
      val word = CodeAssist.getWord(false)
      list(index) match {
        case text: String =>
          if (typeNameCompletion) {
            pane.doc.remove(word._1, pane.caret.position - word._1)
            pane.doc.insertString(word._1, text, null)
            pane.peer.setCaretPosition(word._1 + text.length)
          } else {
            val s = text.substring(0, text.indexOf("{{}}")).trim
            val name = if (s.endsWith("()")) s.substring(0, s.length - 2) else s
            pane.doc.insertString(pane.caret.position, name, null)
          }
      }
      restoreFocus
    }
    
    showPopup(list) { (i, l) => insert(i, l) }
  }
}