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
 *  Created on: 11th December 2011
 */
package net.slate.editor.completion

/**
 *
 */
object AutoComplete {
  import net.slate.Launch._
  import net.slate.editor.tools.CodeAssist._

  private val templates = Map(
    "def" -> " name(x: Any) : Any = { \n}",
    "try" -> " { \n} catch { \n case (e: Exception) { \n} \n}",
    "class" -> " name(x: Any) { \n}",
    "object" -> " name(x: Any) { \n}",
    "trait" -> " name { \n}")

  def inject = {
    val lookup = getWord._2
    val pane = currentScript.text
    val text = templates.getOrElse(lookup, null)
    val found = text != null
    if (found) { 
      val expectedPosition = pane.caret.position + text.indexOf("}")
      pane.doc.insertString(pane.caret.position, text, null)
      pane.caret.position = expectedPosition
    }
    found
  }
}