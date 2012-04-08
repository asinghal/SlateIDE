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
 *  Created on: 27th February 2012
 */
package net.slate.editor.search

object TextSearch {

  import net.slate.gui._
  import net.slate.Launch._

  private val myHighlightPainter = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.decode("0x666666"))

  def findNext(text: String, caseSensitive: Boolean, forward: Boolean): Occurrence = {
    val curFinder = makeFinder(text, caseSensitive)
    val caret = currentScript.text.peer.getCaretPosition

    val lastOcc = curFinder.findNext(currentScript.text.peer.getDocument(), caret, forward)

    if (lastOcc == null) {
      selectNone()
      println("Text not found. Perhaps start at the beginning of this file? ")
    } else {
      selectText(lastOcc.pos, lastOcc.length)
    }

    lastOcc
  }

  def replace(text: String, replacement: String, caseSensitive: Boolean, forward: Boolean) = {
    makeFinder(text, caseSensitive).replaceAll(currentScript.text.peer.getDocument(), replacement)
  }

  def selectText(index: Int, length: Int) {
    val hilite = currentScript.text.peer.getHighlighter()
    hilite.removeAllHighlights
    hilite.addHighlight(index, index + length, myHighlightPainter)
  }

  def selectNone() {
    val hilite = currentScript.text.peer.getHighlighter()
    hilite.removeAllHighlights
    selectText(0, 0)
  }

  def makeFinder(text: String, chkCase: Boolean): Finder = {
    new PlainTextFinder(text, chkCase)
  }
}