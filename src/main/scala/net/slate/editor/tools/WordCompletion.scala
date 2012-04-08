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
 *  Created on: 4th October 2011
 */
package net.slate.editor.tools

import net.slate.Launch._

/**
 * Utility for making word suggestion.
 *
 * @author Aishwarya Singhal
 */
object WordCompletion {
  private val SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  private val SLASH_SLASH_COMMENT = "//.*"

  /**
   * Parses the current tab for words and builds a list such that the words closest to the current
   * cursor position appear on top.
   */
  def suggest(prefix: String): Array[AnyRef] = {
    val caret = currentScript.text.peer.getCaretPosition
    val doc = currentScript.text.peer.getDocument

    val words = getWords(prefix, 0, caret).reverse ++ getWords(prefix, caret, doc.getLength())
    // remove duplicate words
    words.reverse.distinct.reverse.toArray.asInstanceOf[Array[AnyRef]]
  }

  /**
   *
   */
  private def getWords(prefix: String, start: Int, end: Int): List[String] = {
    val doc = currentScript.text.peer.getDocument
    // remove all commented code, we don't need it
    val text = doc.getText(start, end - start).replaceAll(SLASH_STAR_COMMENT, "").replaceAll(SLASH_SLASH_COMMENT, "")
    getWordsFromString(text, prefix)
  }

  /**
   *
   */
  private def getWordsFromString(s: String, prefix: String): List[String] = {
    s.split("[^\\w_]+").toList.filter { word => word.startsWith(prefix) } filterNot { _ == prefix }
  }
}