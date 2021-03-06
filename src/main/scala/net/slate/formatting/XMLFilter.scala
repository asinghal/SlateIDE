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
package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class XMLFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val COMMENT = "<!--(?s:.)*?(?:-->|\\z)"
  val NODE_START = "<[\\?\\w]*"
  val NODE_CLOSE = "[\\?/]?>"
  val NODE_END = "</[\\w]*>"
  val QUOTES = "(?ms:\"{3}(?!\\\"{1,3}).*?(?:\"{3}|\\z))|(?:\"{1}(?!\\\").*?(?:\"|\\Z))"

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(COMMENT, styles('multilineComment)).putStyle(NODE_START, styles('symbols)).putStyle(NODE_CLOSE, styles('symbols)).putStyle(NODE_END, styles('symbols)).putStyle(QUOTES, styles('quotes))
}