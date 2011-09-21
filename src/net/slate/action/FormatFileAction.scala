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

    lines.split("\n").foreach { l =>
      val lastOpeningBrace = previousLine.lastIndexOf("{")
      val indent = if (previousLine.trim.endsWith("{")) 2
      else if (l.trim.startsWith("}")) -2
      else if (previousLine.trim.startsWith("/*") && l.trim.startsWith("* ")) 1
      else if (previousLine.trim.endsWith("*/")) -1
      else if (lastOpeningBrace != -1 && lastOpeningBrace > previousLine.lastIndexOf("}")) 2
      else 0
      val indentation = whitespace(countWhitespace(previousLine) + indent)
      var indentedLine = ("\n" + indentation + l.trim.replaceAll("//[ \\t]*", "// "))
      indentedLine = indentedLine.replaceAll("[ \\t]*\\Z", "")
      indentedLine = formatAssignmentArrow(indentedLine)
      indentedLine = formatEqualsSign(indentedLine)

      if (!previousLineBlank || l.trim != "")
        formattedText += indentedLine
      if (l.trim != "") {
        previousLine = indentedLine.replace("\n", "")
        previousLineBlank = false
      } else previousLineBlank = true
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
    s.replaceAll("[ \\t]*([:\\+\\-/\\*]*)?=[ \\t]*(>)?", " $1=$2 ")
  }
}