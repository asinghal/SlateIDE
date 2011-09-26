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