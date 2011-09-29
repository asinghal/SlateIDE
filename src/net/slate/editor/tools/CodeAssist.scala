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
   * Extract the word at the caret position.
   */
  def getWord = {
    val caret = currentScript.text.peer.getCaretPosition
    val doc = currentScript.text.peer.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset

    val line = doc.getText(start, caret - start).trim
    val word = if (line.contains(" ")) line.substring(line.lastIndexOf(" ", caret - start) + 1) else line
    val w = removeBrackets(word).trim
    val pos = caret - w.length

    (pos, w)
  }

  /**
   * Removes brackets from the words.
   */
  private def removeBrackets(input: String) = {
    input.replaceAll("[\\{\\(\\[\\]\\}\\)]*", "")
  }
}