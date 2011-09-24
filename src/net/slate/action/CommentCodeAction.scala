package net.slate.action

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent

class CommentCodeAction(textPane: JTextPane) extends AbstractAction with LineParser {

  def actionPerformed(e: ActionEvent) = {
    val doc = textPane.getDocument;
    var caret = textPane.getSelectionStart

    while (caret <= textPane.getSelectionEnd) {
      val l = line(textPane, caret)
      val start = startOfLine(textPane, caret)
      doc.remove(start, l.length)
      doc.insertString(start, "// " + l, null)
      caret = (endOfLine(textPane, caret) + 1)
    }
  }
}