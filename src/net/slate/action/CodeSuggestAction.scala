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
class CodeSuggestAction extends AbstractAction with LineParser {

  import actors.Actor._

  /**
   * @param ActionEvent
   */
  def actionPerformed(e: ActionEvent) = {
    val textPane = currentScript.text.peer

    val doc = textPane.getDocument;
    val caret = textPane.getCaretPosition;
    val l = line(textPane, caret)

    val pane = currentScript
    var x = 0
    var y = 0
    def setPosition {
      val point = pane.text.peer.getCaret.getMagicCaretPosition
      val editor = pane.peer.getViewport.getViewPosition
      x = if (point != null) (point.getX.asInstanceOf[Int] - editor.getX.asInstanceOf[Int] + 50) else 50
      y = if (point != null) (point.getY.asInstanceOf[Int] - editor.getY.asInstanceOf[Int] + 20) else 20
    }

    if (l.trim.startsWith("import ")) {
      val name = l.trim.substring("import ".length).trim
      val packages = Package.getPackages.filter { p => p.getName.startsWith(name) && (p.getName) != name }.sortWith { _.getName < _.getName }.map { _.getName }
      setPosition
      CodeCompletionPopupMenu.show(pane, x, y, packages.asInstanceOf[Array[AnyRef]])
    } else if (l.trim.startsWith("@")) {
      setPosition
      CodeSuggestionPopupMenu.show(pane, x, y)
    } else if (currentScript.text.path.trim.toLowerCase.endsWith(".scala")) {
      val a = actor { CodeCompletionPopupMenu.show(pane, x, y, suggestMethodsAndVars.toArray, false) }
    }
  }

  private def suggestMethodsAndVars = {

    import scala.tools.nsc.Settings
    import scala.tools.nsc.interactive.{ Global, Response }
    import scala.tools.nsc.util.BatchSourceFile
    import scala.tools.nsc.reporters.StoreReporter

    val settings = new Settings
    settings.classpath.value = ScalaBuilder.getClassPath(ExecutionContext.currentProjectName(currentScript.text.path))

    val reporter = new StoreReporter

    val global = new Global(settings, reporter)

    val completed = new Response[List[global.Member]]
    val typed = new Response[global.Tree]
    val reload = new Response[Unit]
    val textPane = currentScript.text.peer
    val sourceFile = new BatchSourceFile(currentScript.text.path, currentScript.text.text)
    val start = textPane.getCaretPosition

    // HACK!!! can't get one solution that works with both 2.8.1 and 2.9.1
    global.unitOfFile.getOrElse(sourceFile.file,
      global.unitOfFile.put(sourceFile.file, new global.RichCompilationUnit(sourceFile)))
    global.askType(sourceFile, false, typed)

    val cpos = global.rangePos(sourceFile, start, start, start)
    global.askTypeCompletion(cpos, completed)

    val visibleMembers = completed.get.left.toOption match {
      case Some(members) =>
        members.filter { _.accessible } map {
          case m@global.TypeMember(sym, tpe, true, _, _) => {

            def memberName = {
              (tpe.paramss.map { sect =>
                "(" +
                  sect.map { _.tpe.toString }.mkString(", ") +
                  ")"
              }.mkString(" : ")
                + " {{}} " +
                tpe.finalResultType.toString)
            }
            sym.nameString + memberName
          }
        }
      case None => List[String]()
    }

    visibleMembers
  }
}