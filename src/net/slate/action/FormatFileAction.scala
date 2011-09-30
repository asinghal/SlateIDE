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
package net.slate.action

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent
import net.slate.Actions._
import net.slate.Launch._

/**
 *
 *
 */
class FormatFileAction extends AbstractAction with IndentText with LineParser {

  def actionPerformed(e: ActionEvent) = {
    saveFile
    val filePath = currentScript.text.path
    val source = scala.io.Source.fromFile(filePath)
    val lines = source.mkString
    source.close()

    var previousLine = ""
    var formattedText = ""
    var previousLineBlank = false

    var previousExtraIndent = 0

    lines.split("\n").foreach { l =>
      val lastOpeningBrace = previousLine.lastIndexOf("{")
      val indent = if (l.trim.startsWith("}")) -2
      else if (previousLine.trim.startsWith("/*") && l.trim.startsWith("* ")) 1
      else if (previousLine.trim.endsWith("*/")) -1
      else if (lastOpeningBrace != -1 && lastOpeningBrace > previousLine.lastIndexOf("}")) 2
      else if (previousLine.trim.startsWith("//") || l.trim.startsWith("//")) 0
      else if (previousLine.trim.endsWith("{")) 2
      else 0

      //      var extraIndent = if (l.trim.startsWith("else ")) previousLine.trim.lastIndexOf(" if ") + 1 else 0
      var adjustment = indent
      //      if (l.trim.startsWith("else if ")) {
      //        previousExtraIndent = 0
      //        if (previousLine.trim.startsWith("else if ")) {
      //          0
      //        } else (indent + extraIndent)
      //      } else (indent + extraIndent - previousExtraIndent)

      val indentation = whitespace(countWhitespace(previousLine) + adjustment)
      var indentedLine = ("\n" + indentation + l.trim.replaceAll("//[ \\t]*", "// "))
      indentedLine = indentedLine.replaceAll("[ \\t]*\\Z", "")
      indentedLine = formatAssignmentArrow(indentedLine)
      indentedLine = formatEqualsSign(indentedLine)
      indentedLine = formatComment(indentedLine)

      if (!previousLineBlank || l.trim != "")
        formattedText += indentedLine
      if (l.trim != "") {
        previousLine = indentedLine.replace("\n", "")
        previousLineBlank = false
      } else previousLineBlank = true

      //      previousExtraIndent = extraIndent
    }

    currentScript.text.text = formattedText.trim
  }

  private def formatAssignmentArrow(s: String) = {

    // handle cases like "for(i<-1 to 10); map += (a->b)"
    s.replaceAll("[ \\t]*(<)?\\-(>)?[ \\t]*", " $1-$2 ")
  }

  private def formatEqualsSign(s: String) = {
    // handle cases like "val x+=10; v::=a; v:::=a; v/=a; v*=a; v-=a; 
    // x += 10; v ::= a; v :::= a; v /= a; v *= a; v -= a; a { a=>b }"
    s.replaceAll("[ \\t]*([:\\+\\-/\\*!><=]*)?=[ \\t]*(>)?[ \\t]*", " $1=$2 ")
  }
  
  
  private def formatComment(s: String) = {
    s.replaceAll("//[ \\t]*", "// ")
  }
}