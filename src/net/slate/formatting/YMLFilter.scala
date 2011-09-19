package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class YMLFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val COMMENT = "[ \\t]*#.*"
  val NODE_START = "[\\w]*\\:"

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(COMMENT, styles('multilineComment)).putStyle(NODE_START, styles('symbols))
}