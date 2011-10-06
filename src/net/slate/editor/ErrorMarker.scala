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
 *  Created on: 6th October 2011
 */
package net.slate.editor

import java.awt.{ Color, Graphics, Rectangle, Shape }
import javax.swing.text._

import net.slate.Launch._

/**
 * Highlights errors and warnings in the editor.
 *
 * @author Aishwarya Singhal
 */
object ErrorMarker {
  private lazy val errorStyle = new UnderlineHighlighter(Color.decode("0xFF0000")).setPainter(Color.decode("0xFF0000"))
  private lazy val warningStyle = new UnderlineHighlighter(Color.decode("0xFFFF00")).setPainter(Color.decode("0xFFFF00"))

  /**
   * marks the errors in current script.
   */
  def mark = {

    var linesWithProblems = List[Int]()

    val problems = getProblems.filter { p => p._3 == currentScript.text.path }.sortWith { _._1 < _._1 }
    problems.foreach { p => linesWithProblems ::= p._1 }

    val doc = currentScript.text.peer.getDocument.asInstanceOf[DefaultStyledDocument]
    val highlighter = currentScript.text.peer.getHighlighter

    // Remove any existing highlights for last word
    val highlights = highlighter.getHighlights()
    highlighter.removeAllHighlights

    val text = doc.getText(0, doc.getLength)
    val i = 0
    var pos = 0
    for (n <- 2 to text.split("\n").length) {
      pos = text.indexOf("\n", pos + 1)
      val caret = text.indexOf("\n", pos + 1)

      if (linesWithProblems.contains(n)) {
        problems.filter { p => p._1 == n }.foreach { p =>
          p._2 match {
            case "Error" => markError(doc, caret, highlighter)
            case "Warning" => markWarning(doc, caret, highlighter)
            case _ =>
          }
        }
      }
    }
  }

  private def markError(doc: DefaultStyledDocument, caret: Int, highlighter: Highlighter) = {
    markInEditor(doc, caret, highlighter, errorStyle)
  }

  private def markWarning(doc: DefaultStyledDocument, caret: Int, highlighter: Highlighter) = {
    markInEditor(doc, caret, highlighter, warningStyle)
  }

  private def markInEditor(doc: DefaultStyledDocument, caret: Int, highlighter: Highlighter, painter: Highlighter.HighlightPainter) = {
    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset

    highlighter.addHighlight(start, end, painter)
  }

  /**
   * Extracts problems from the problems tab.
   */
  private def getProblems = {
    val model = bottomTabPane.problems.tableModel
    var problems = List[(Int, String, String)]()

    for (row <- 0 to (model.getRowCount - 1)) {
      val line = model.getValueAt(row, 2).toString.trim.toInt
      val problemType = model.getValueAt(row, 4).toString.trim
      val path = model.getValueAt(row, 5).toString.trim
      problems ::= (line, problemType, path)
    }

    problems
  }

  class UnderlineHighlighter(var c: Color) extends DefaultHighlighter {

    private var painter: Highlighter.HighlightPainter = _

    // Shared painter used for default highlighting
    protected var sharedPainter = new UnderlineHighlightPainter(Color.white)

    def setPainter(c: Color) = {
      painter = if (c == null) sharedPainter else new UnderlineHighlightPainter(c)
      painter
    }

    override def setDrawsLayeredHighlights(newValue: Boolean) = {
      // Illegal if false - we only support layered highlights
      if (!newValue) {
        throw new IllegalArgumentException(
          "UnderlineHighlighter only draws layered highlights");
      }
      super.setDrawsLayeredHighlights(true)
    }

    // Painter for underlined highlights
    class UnderlineHighlightPainter(color: Color) extends LayeredHighlighter.LayerPainter {

      def paint(g: Graphics, offs0: Int, offs1: Int, bounds: Shape,
        c: JTextComponent) {
        // Do nothing: this method will never be called
      }

      def paintLayer(g: Graphics, offs0: Int, offs1: Int, bounds: Shape,
        c: JTextComponent, view: View): Shape = {
        g.setColor(color)

        var alloc: Rectangle = null;
        if (offs0 == view.getStartOffset && offs1 == view.getEndOffset) {
          bounds match {
            case x: Rectangle => alloc = x
            case _ => alloc = bounds.getBounds
          }
        } else {
          try {
            val shape = view.modelToView(offs0,
              Position.Bias.Forward, offs1,
              Position.Bias.Backward, bounds);
            alloc = shape match {
              case x: Rectangle => x
              case _ => shape.getBounds
            }
          } catch {
            case e: BadLocationException =>
              return null;
          }
        }

        val fm = c.getFontMetrics(c.getFont())
        val baseline = alloc.y + alloc.height - fm.getDescent() + 1
        g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline)

        return alloc
      }
    }
  }
}