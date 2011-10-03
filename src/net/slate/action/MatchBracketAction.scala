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
 *  Created on: 3rd October 2011
 */
package net.slate.action

import java.awt.Color
import javax.swing.{ JTextPane, SwingUtilities }
import javax.swing.event.{ CaretEvent, CaretListener }
import javax.swing.text.{ Style, StyleContext, StyleConstants }

import net.slate.Launch._

/**
 * Matches brackets in a text pane and highlights the pair.
 *
 * @author Aishwarya Singhal
 */
class MatchBracketAction extends CaretListener {
  private val map = Map('{' -> ('}', true), '[' -> (']', true), '(' -> (')', true), '}' -> ('{', false), ']' -> ('[', false), ')' -> ('(', false))
  val sc = StyleContext.getDefaultStyleContext
  val defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE)
  val style = getStyle

  // last index that was styled (that should be reset first!)
  var styledIndex = -1

  /**
   * Capture the caret position change.
   */
  def caretUpdate(e: CaretEvent) {
    val pos = e.getDot
    val textPane = currentScript.text.peer
    val doc = textPane.getDocument;

    /**
     * Find the matching bracket and highlight it.
     */
    def findMatching(bracket: Char, matchingBracket: Char, forward: Boolean) = {
      val text = doc.getText(0, doc.getLength)
      var bracketCount = 1
      var index = if (forward) pos else pos - 1

      while (bracketCount != 0 && ((!forward && index != 0) || (forward && index != (doc.getLength - 1)))) {
        index = if (forward) index + 1 else index - 1

        val c = text.charAt(index)

        // nested bracket found, we'll need one more of the matching brackets
        if (c == bracket) bracketCount += 1
        // found a matching bracket, reduce the count.
        if (c == matchingBracket) bracketCount -= 1
      }

      if (bracketCount == 0) {
        // highlight the match
        setCharacterAttributes(textPane, index, style)
        styledIndex = index
      }
    }

    if (pos != 0) {
      val s = (doc.getText(pos - 1, 1)).charAt(0)

      if (styledIndex != -1 && styledIndex < doc.getLength) {
        // reset the previous highlight
        setCharacterAttributes(textPane, styledIndex, defaultStyle)
        styledIndex = -1
      }

      if (map.contains(s)) {
        // a bracket was found, we'll need to find a match
        val v = map(s)
        findMatching(s, v._1, v._2)
      }
    }
  }

  /**
   * Set the character attributes (aka the style or highlight)
   * 
   * @param textPane
   * @param index
   * @param style
   */
  private def setCharacterAttributes(textPane: JTextPane, index: Int, style: Style) = {
    SwingUtilities.invokeLater(new Runnable {
      def run {
        textPane.getStyledDocument.setCharacterAttributes(index, 1, style, true)
      }
    })
  }

  /**
   * Build a style to be used for highlighting.
   *
   * @return
   */
  private def getStyle = {
    val style = sc.addStyle("bracketMatch", defaultStyle)
    StyleConstants.setBackground(style, Color.decode("0x888888"))
    StyleConstants.setForeground(style, Color.decode("0x000000"))
    StyleConstants.setBold(style, true)

    style
  }
}