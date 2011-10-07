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
package net.slate.action

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent

import net.slate.Launch._
import net.slate.gui.CodeCompletionPopupMenu

/**
 *
 * @author Aishwarya Singhal
 */
class CodeSuggestAction extends AbstractAction with LineParser {

  /**
   * @param ActionEvent
   */
  def actionPerformed(e: ActionEvent) = {
    val textPane = currentScript.text.peer

    val doc = textPane.getDocument;
    val caret = textPane.getCaretPosition;
    val l = line(textPane, caret)

    if (l.trim.startsWith("import ")) {
      val name = l.trim.substring("import ".length).trim
      val packages = Package.getPackages.filter { p => p.getName.startsWith(name) && (p.getName) != name }.sortWith { _.getName < _.getName }.map { _.getName }

      val pane = currentScript
      val point = pane.text.peer.getCaret.getMagicCaretPosition
      val editor = pane.peer.getViewport.getViewPosition
      val x = if (point != null) (point.getX.asInstanceOf[Int] - editor.getX.asInstanceOf[Int] + 50) else 50
      val y = if (point != null) (point.getY.asInstanceOf[Int] - editor.getY.asInstanceOf[Int] + 20) else 20

      CodeCompletionPopupMenu.show(pane, x, y, packages.asInstanceOf[Array[AnyRef]])
    }
  }
}