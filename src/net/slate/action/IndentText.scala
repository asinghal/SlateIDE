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

trait IndentText {

  protected def indentLine(line: String, enableExtra: Boolean) = {
    val spacesCount = countWhitespace(line)

    var indentation = 0

    var trimmed = line.trim

    if (trimmed.endsWith("{")) indentation += 2
    if (trimmed.endsWith("}")) indentation -= 0
    if (trimmed.endsWith("*/")) indentation -= 1

    val space = whitespace(spacesCount + indentation)

    val extra = if (!enableExtra) ""
    else if (line.endsWith("/**") || line.endsWith("/*")) " * "
    else if (trimmed.startsWith("* ") || trimmed == "*") "* "
    else if (trimmed.endsWith("{")) ('\n' + space.substring(2) + "}")
    else ""

    ('\n' + space + extra, space.length)
  }

  protected def countWhitespace(line: String): Int = {
    var count = 0
    for (i <- 0 to line.length - 1) {
      val c = line.charAt(i)
      c match {
        case ' ' => count += 1
        case '\t' => count += 1
        case _ => return count
      }
    }

    count
  }

  protected def whitespace(count: Int) = {

    var space = ""
    for (i <- 1 to count) {
      space += " "
    }

    space
  }

}