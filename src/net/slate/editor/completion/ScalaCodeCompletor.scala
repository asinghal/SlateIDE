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
 *  Created on: 29th October 2011
 */
package net.slate.editor.completion

import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.{ Global, Response }
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.reporters.StoreReporter

/**
 * Code completion utility that hooks up with the Scala compiler to make code suggestions.
 *
 * @author Aishwarya Singhal
 */
object ScalaCodeCompletor {

  /**
   * Get all methods/ attributes of the entity at the given position.
   *
   * @param classpath
   * @param path
   * @param contents
   * @param start
   */
  def suggestMethodsAndVars(classpath: String, path: String, contents: String, start: Int) = {

    val settings = new Settings
    settings.classpath.value = classpath

    val reporter = new StoreReporter

    val global = new Global(settings, reporter)

    val completed = new Response[List[global.Member]]
    val typed = new Response[global.Tree]
    val reload = new Response[Unit]
    val sourceFile = new BatchSourceFile(path, contents)

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
                  sect.map { _.nameString }.mkString(", ") +
                  ")"
              }.mkString(" => ")
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