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

import net.slate.builder.ScalaBuilder
import net.slate.ExecutionContext
import net.slate.Launch._
import net.slate.gui.{ CodeCompletionPopupMenu, CodeSuggestionPopupMenu }

/**
 *
 * @author Aishwarya Singhal
 */
class CodeSuggestAction(includeLocal: Boolean = true) extends AbstractAction with LineParser {

  import actors.Actor._

  /**
   * @param ActionEvent
   */
  def actionPerformed(e: ActionEvent) = {
    val textPane = currentScript.text.peer

    val doc = textPane.getDocument;
    val caret = textPane.getCaretPosition;
    val l = line(textPane, caret)

    if (includeLocal && l.trim.startsWith("import ")) {
      val name = l.trim.substring("import ".length).trim
      val packages = Package.getPackages.filter { p => p.getName.startsWith(name) && (p.getName) != name }.sortWith { _.getName < _.getName }.map { _.getName }
      CodeCompletionPopupMenu.show(packages.asInstanceOf[Array[AnyRef]])
    } else if (includeLocal && l.trim.startsWith("@")) {
      CodeSuggestionPopupMenu.show
    } else if (currentScript.text.path.trim.toLowerCase.endsWith(".scala")) {
      val a = actor { CodeCompletionPopupMenu.show(suggestMethodsAndVars.toArray, false) }
    }
  }

  private def suggestMethodsAndVars = {

    import net.slate.editor.completion._
    
    val projectName = ExecutionContext.currentProjectName(currentScript.text.path)

    val cp = ScalaBuilder.getClassPath(projectName)

    val textPane = currentScript.text.peer
    var lastCharPosition = textPane.getCaretPosition - 1
    while (Character.isWhitespace(textPane.getText.charAt(lastCharPosition))) {
      lastCharPosition = lastCharPosition - 1
    }

    ScalaCodeCompletor.suggestMethodsAndVars(projectName, cp, currentScript.text.path, currentScript.text.text, lastCharPosition)
  }
}