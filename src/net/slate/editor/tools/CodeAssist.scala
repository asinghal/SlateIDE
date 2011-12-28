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
package net.slate.editor.tools

import net.slate.Launch._

/**
 * Utility class that provides coding assistance.
 *
 * @author Aishwarya Singhal
 */
object CodeAssist {

  /**
   * add extra capabilities to java string.
   */
  implicit def StringWrapper(str: String) = {
    new {
      /**
       * Gets a string until it find the character, or the whole string anyways.
       */
      def substr(c: java.lang.Character) = {
        if (str.contains(c)) str.substring(0, str.indexOf(c)) else str
      }
    }
  }

  /**
   * Extract the word at the caret position.
   */
  def getWord: (Int, String) = getWord(true)

  def getWord(ignorePeriods: Boolean = true): (Int, String) = {
    val caret = currentScript.text.peer.getCaretPosition
    val doc = currentScript.text.peer.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset
    
    val caretPos = caret - start 
    
    val line = doc.getText(start, end - start)
    
    def maxIndex(x: Int, y: java.lang.Character) = {
      if (line.contains(y) && (line.indexOf(y, caretPos) <= x || x == -1)) line.indexOf(y, caretPos) else x
    }
    val index = Array(' ', '.').foldLeft(-1){ (x, y) => maxIndex(x, y) }
    val endPos = if (index > -1) index else caretPos

    val word = if (line.contains(" ")) line.substring(line.lastIndexOf(" ", caret - start) + 1) else line
    val w = if (ignorePeriods) removeBrackets(word.substring(word.lastIndexOf(".", caret - start) + 1)) else removeBrackets(word)
    val pos = caret - w.length

    val fullWord = w.substr(' ').substr('.')

    (pos, fullWord)
  }

  /**
   * Removes brackets from the words.
   */
  private[this] def removeBrackets(input: String) = {
    input.substr('[').substr('(').substr('{').substr('}').substr(')').substr(']').trim
  }
}