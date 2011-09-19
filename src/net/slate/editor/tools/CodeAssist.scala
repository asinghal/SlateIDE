package net.slate.editor.tools

import net.slate.Launch._

object CodeAssist {

  def getWord = {
    val caret = currentScript.text.peer.getCaretPosition
    val doc = currentScript.text.peer.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset

    val l = doc.getText(start, caret - start).trim
    val word = if (l.contains(" ")) l.substring(l.lastIndexOf(" ")) else l
    val pos = if (l.contains(" ")) (start + l.lastIndexOf(" ") + 1) else start

    (pos, word)
  }
}