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