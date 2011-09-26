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
package net.slate.action;

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent

class AutoIndentAction(textPane: JTextPane) extends AbstractAction with IndentText with LineParser {

  def actionPerformed(e: ActionEvent) = {
    val doc = textPane.getDocument;
    val caret = textPane.getCaretPosition;
    val l = line(textPane, caret)

    val indentation = indentLine(l, true)
    doc.insertString(caret, indentation._1, null)

    if (l.trim.endsWith("{")) {
      val newCaret = textPane.getCaretPosition
      textPane.setCaretPosition(textPane.getCaretPosition - indentation._2)
    }
  }
}