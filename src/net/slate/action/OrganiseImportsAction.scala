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
 *  Created on: 26th September 2011
 */
package net.slate.action

import javax.swing.AbstractAction
import java.awt.event.ActionEvent

import net.slate.Launch._

/**
 *
 * @author Aishwarya Singhal
 *
 */
class OrganiseImportsAction extends AbstractAction {
  private val MARKER = "@@@__ORGANISED_IMPORTS__@@@"

  def actionPerformed(e: ActionEvent) = {
    val source = scala.io.Source.fromFile(currentScript.text.path)
    val lines = source.mkString
    source.close()

    organise(lines)
  }

  /**
   * Organizes the imports alphabetically and also rolls up duplicates/ the imports in same package.
   * The input is assumed to be separated by "\n".
   *
   * @param lines
   */
  def organise(lines: String) {
    // get all imports in this file
    val all = getAllImports(lines)
    val imports = all._1
    val text = all._2

    var lastpackage = ""
    var collapsedpackage = ""

    var finalImports = List[String]()

    imports.foreach { p =>
      // extract the package name
      val pack = if (p.contains(".")) p.substring(0, p.lastIndexOf(".")) else ""

      // extract the last part (class name/ wildcard)
      val last = p.substring(p.lastIndexOf(".") + 1).trim

      // if the package is different to the previously recorded one, or is in default package, flush
      if (pack != lastpackage || !p.contains(".")) {
        // note the change of package
        lastpackage = pack

        // flush the package import created thus far
        finalImports ::= collapsedpackage

        // reset the buffer
        collapsedpackage = p
      } else if (p.contains(".")) {
        if (!collapsedpackage.contains("{")) {

          // no roll ups so far, but we'll need one now
          collapsedpackage = p.substring(0, p.lastIndexOf(".")) + ".{ " + collapsedpackage.substring(collapsedpackage.lastIndexOf(".") + 1).trim + ", " + last + " }\n"
        } else {

          // so there's a roll up existing, insert the new class into it.
          collapsedpackage = collapsedpackage.substring(0, collapsedpackage.lastIndexOf("}")).trim + ", " + last + " }\n"
        }
      }
    }

    // lets catch the last one
    finalImports ::= collapsedpackage

    finalImports = finalImports.map { i =>
      if (i.contains("{") && i.contains("}") && i.contains("_")) { i.substring(0, i.lastIndexOf(".")) + "._\n" } else { i }
    }

    val replacement = finalImports.sort { _.toLowerCase < _.toLowerCase }.distinct.mkString

    val replacedText = text.replace(MARKER, replacement)

    currentScript.text.text = replacedText
  }

  /**
   * Gets all imports from top of the file.
   *
   * @param lines
   * @return
   */
  private def getAllImports(lines: String) = {
    val all = lines.split("\n")

    var modifiedText = ""
    var classDef = false

    var i = 0
    var imports = List[String]()
    while (!classDef && i < all.length) {

      val l = all(i)
      val trimmed = l.trim
      if (trimmed.startsWith("class") || trimmed.startsWith("object") || trimmed.startsWith("trait")) {
        classDef = true
        modifiedText += l
      } else if (trimmed.startsWith("import ")) {
        imports ::= (l + "\n")
        if (modifiedText.indexOf(MARKER) == -1) {
          modifiedText += (MARKER + "\n")
        }
      } else {
        modifiedText += (l + "\n")
      }

      i += 1
    }

    for (index <- i to (all.length - 1)) {
      modifiedText += (all(index) + "\n")
    }

    (imports.sort { _ < _ }.distinct, modifiedText)
  }
}