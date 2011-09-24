package net.slate.action

import javax.swing.JTextPane

trait LineParser {

  protected def line(textPane: JTextPane, caret: Int) = {
    val doc = textPane.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset

    doc.getText(start, end - start - 1)
  }

  protected def startOfLine(textPane: JTextPane, caret: Int) = {
    val doc = textPane.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    doc.getParagraphElement(caret).getStartOffset
  }
  
  protected def endOfLine(textPane: JTextPane, caret: Int) = {
    val doc = textPane.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    doc.getParagraphElement(caret).getEndOffset
  }
}