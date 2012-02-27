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

import javax.swing.text.Document

case class Occurrence(pos: Int, length: Int)

abstract class Finder {
  def findNext(doc: Document, startPos: Int, findForward: Boolean): Occurrence

  def replaceAll(doc: Document, replacement: String)
}

class PlainTextFinder(casedTarget: String, caseSensitive: Boolean) extends Finder {
  def findNext(doc: Document, startPos: Int, findForward: Boolean): Occurrence = {
    val docText = doc.getText(0, doc.getLength)
    val text = if (caseSensitive) docText else docText.toLowerCase
    val target = if (caseSensitive) casedTarget else casedTarget.toLowerCase
    val pos =
      if (findForward) {
        if (startPos == text.length)
          -1
        else
          text.indexOf(target, startPos)
      } else {
        //        if (startPos == 0)
        //          -1
        //        else
        text.lastIndexOf(target, /*startPos - */ target.length)
      }
    if (pos >= 0)
      return new Occurrence(pos, target.length)
    else
      return null
  }

  def replaceAll(doc: Document, replacement: String) = {
    var next: Occurrence = findNext(doc, 0, true)

    while (next != null) {
      doc.remove(next.pos, next.length)
      doc.insertString(next.pos, replacement, null)
      next = findNext(doc, 0, true)
    }
  }
}