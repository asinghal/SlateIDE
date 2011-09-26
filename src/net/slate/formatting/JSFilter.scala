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

class JSFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  val SLASH_SLASH_COMMENT = "//.*"
  val IDENT = "[\\w\\$&&[\\D]][\\w\\$]*"

  val KEYWORDS = Seq("function", "do", "else", "false", "for", "if", "new", "null", "object", "return",
    "this", "true", "var", "while").map("\\b" + _ + "\\b")

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(SLASH_STAR_COMMENT, styles('multilineComment)).putStyle(SLASH_SLASH_COMMENT, styles('inlineComment))
//  lexer.putChild(IDENT, new LexerNode().putStyle(KEYWORDS, styles('reserved)))
}